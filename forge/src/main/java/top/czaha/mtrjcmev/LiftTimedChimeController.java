package top.czaha.mtrjcmev;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mtr.core.data.Lift;
import org.mtr.core.data.LiftDirection;
import org.mtr.mod.Init;
import org.mtr.mod.block.BlockLiftTrackFloor;
import org.mtr.mod.client.MinecraftClientData;
import org.mtr.mapping.holder.BlockEntity;
import org.mtr.mapping.holder.BlockPos;
import org.mtr.mapping.holder.ClientWorld;
import org.mtr.mapping.holder.MinecraftClient;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Client-only optional chime timing.
 *
 * Deceleration mode samples MTR's own speed field (blocks/ms) rather than
 * recomputing an expensive path to estimate car velocity. Door mode waits for
 * door opening after JCM has confirmed the arrival of this lift.
 *
 * JCM's built-in arrival mode is handled by MixinJcmArrivalChime.
 */
public final class LiftTimedChimeController {
    private static final Logger LOGGER = LogManager.getLogger("MTR-JCM-Elevator-Custom");
    private static final double MIN_MOVING_SPEED = 0.0008D;
    private static final double MIN_DECELERATION_DROP = 0.00035D;
    private static final double DECELERATION_RATIO = 0.82D;
    private static final double AUDIBLE_DISTANCE_SQUARED = 32D * 32D;
    private static final Map<Long, State> STATES = new HashMap<>();
    private static ClientLevel world;
    private static Field speedField;
    private static boolean speedFieldResolved;
    private static long tickNumber;

    /** Called by JCM's arrival hook once the destination and floor are confirmed. */
    public static boolean deferUntilDoorOpens(long id, LiftDirection direction) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) return false;
        checkWorld(minecraft.level);
        State state = STATES.computeIfAbsent(id, key -> new State());
        if (direction == LiftDirection.UP || direction == LiftDirection.DOWN) state.direction = direction;
        if (state.direction == LiftDirection.NONE) return false;
        state.pendingDoor = true;
        state.pendingSince = tickNumber;
        return true;
    }

    /** Prevent JCM's normal arrival chime after this mod already sounded early. */
    public static boolean alreadyPlayed(long id) {
        State state = STATES.get(id);
        return state != null && state.played;
    }

    /** Arrival fallback: a very short trip might not reach deceleration threshold. */
    public static void markArrivalPlayed(long id) {
        State state = STATES.computeIfAbsent(id, key -> new State());
        state.played = true;
        state.pendingDoor = false;
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        ElevatorMediaStore.preload();
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        checkWorld(level);
        if (level == null || minecraft.player == null) return;
        tickNumber++;
        for (MinecraftClientData.LiftWrapper wrapper : MinecraftClientData.getInstance().liftWrapperList.values()) {
            Lift lift = wrapper.getLift();
            if (lift == null || lift.getCurrentFloor() == null) continue;
            long id = lift.getId();
            LiftMediaRuntime media = ElevatorMediaStore.runtimeFor(id);
            if (media == null || !media.hasChimeSettings()
                    || media.chimeTiming() == LiftMediaRuntime.ChimeTiming.ARRIVAL) {
                STATES.remove(id);
                continue;
            }
            State state = STATES.computeIfAbsent(id, key -> new State());
            state.lastSeen = tickNumber;
            LiftDirection direction = lift.getDirection();
            if (direction != LiftDirection.NONE) state.direction = direction;
            if (media.chimeTiming() == LiftMediaRuntime.ChimeTiming.DECELERATION) {
                sampleDeceleration(lift, media, state, level);
            } else if (state.pendingDoor && lift.getDoorValue() >= 0.08f) {
                if (tryPlay(level, lift, media, state.direction)) {
                    state.played = true;
                }
                state.pendingDoor = false;
            }

            // Expire an arrival that never opened its doors (e.g. unload or
            // unexpected JCM state). Never play a stale chime on the next trip.
            if (state.pendingDoor && tickNumber - state.pendingSince > 120) state.pendingDoor = false;
        }

        if ((tickNumber & 63) == 0) {
            Iterator<State> iterator = STATES.values().iterator();
            while (iterator.hasNext()) {
                if (tickNumber - iterator.next().lastSeen > 64) iterator.remove();
            }
        }
    }

    private static void sampleDeceleration(Lift lift, LiftMediaRuntime media, State state, ClientLevel world) {
        double speed = readLiftSpeed(lift);
        if (!Double.isFinite(speed)) return;
        double absoluteSpeed = Math.abs(speed);
        boolean moving = lift.getDirection() != LiftDirection.NONE;

        if (!moving) {
            state.inMotion = false;
            state.peakSpeed = 0;
            return;
        }
        if (!state.inMotion) {
            state.inMotion = true;
            state.played = false;
            state.peakSpeed = 0;
        }

        if (absoluteSpeed > state.peakSpeed) state.peakSpeed = absoluteSpeed;
        if (!state.played
                && state.peakSpeed >= MIN_MOVING_SPEED
                && state.peakSpeed - absoluteSpeed >= MIN_DECELERATION_DROP
                && absoluteSpeed <= state.peakSpeed * DECELERATION_RATIO) {
            if (tryPlay(world, lift, media, state.direction)) state.played = true;
        }
    }

    /**
     * Speed lives in MTR's generated LiftSchema superclass, not in Lift itself.
     * Reflection is resolved once and only used for lifts with early-chime
     * selected; no per-frame track pathfinding or repeated field lookups.
     * If a future MTR moves the field, the regular JCM arrival callback still
     * plays the configured sound instead of crashing Minecraft.
     */
    private static double readLiftSpeed(Lift lift) {
        if (!speedFieldResolved) {
            speedFieldResolved = true;
            try {
                Class<?> type = lift.getClass();
                while (type != null) {
                    try {
                        speedField = type.getDeclaredField("speed");
                        speedField.setAccessible(true);
                        break;
                    } catch (NoSuchFieldException ignored) {
                        type = type.getSuperclass();
                    }
                }
            } catch (SecurityException | RuntimeException exception) {
                LOGGER.warn("Cannot access MTR lift speed; deceleration chime falls back to arrival: {}",
                        exception.toString());
            }
            if (speedField == null) {
                LOGGER.warn("MTR lift speed unavailable; deceleration chime falls back to arrival.");
            }
        }
        if (speedField == null) return Double.NaN;
        try {
            return speedField.getDouble(lift);
        } catch (IllegalAccessException | IllegalArgumentException exception) {
            speedField = null;
            return Double.NaN;
        }
    }

    private static boolean tryPlay(ClientLevel level, Lift lift, LiftMediaRuntime media, LiftDirection direction) {
        SoundEvent sound = direction == LiftDirection.UP ? media.upChime()
                : direction == LiftDirection.DOWN ? media.downChime() : null;
        if (sound == null) return false;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.getSoundManager().getSoundEvent(sound.getLocation()) == null) {
            return false;
        }
        ClientWorld mapped = MinecraftClient.getInstance().getWorldMapped();
        if (mapped == null) return false;
        BlockPos pos = Init.positionToBlockPos(lift.getCurrentFloor().getPosition());
        BlockEntity entity = mapped.getBlockEntity(pos);
        if (entity == null || !(entity.data instanceof BlockLiftTrackFloor.BlockEntity)
                || !((BlockLiftTrackFloor.BlockEntity) entity.data).getShouldDing()) return false;
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.5;
        double z = pos.getZ() + 0.5;
        if (minecraft.player.distanceToSqr(x, y, z) <= AUDIBLE_DISTANCE_SQUARED) {
            level.playLocalSound(x, y, z, sound, SoundSource.BLOCKS, 1f, 1f, false);
            LOGGER.debug("Played {} chime for lift {} at timing {}",
                    sound.getLocation(), lift.getId(), media.chimeTiming());
        }
        // Treat out-of-range audio as handled too, so JCM never plays a second
        // chime later just because the player moved during the same stop.
        return true;
    }

    private static void checkWorld(ClientLevel nextWorld) {
        if (world == nextWorld) return;
        world = nextWorld;
        STATES.clear();
        tickNumber = 0;
    }

    private static final class State {
        private LiftDirection direction = LiftDirection.NONE;
        private boolean inMotion;
        private boolean played;
        private boolean pendingDoor;
        private double peakSpeed;
        private long pendingSince;
        private long lastSeen;
    }
}
