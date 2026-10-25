package top.czaha.mtrjcmev;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mtr.core.data.Lift;
import org.mtr.core.data.LiftDirection;
import org.mtr.mod.client.MinecraftClientData;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Client-side per-lift sound trigger.
 *
 * The original implementation only detected UP/DOWN -> NONE, which some
 * versions of MTR/YTE do not emit at an arrival. Detect door opening as well;
 * retain the last movement direction until a chime has played for that stop.
 * Trigger at most once per stop, and never on initial world join.
 */
public final class LiftArrivalSoundController {
    private static final Logger LOGGER = LogManager.getLogger("MTR-JCM-Elevator-Custom");
    private static final double MAX_DISTANCE_SQUARED = 32 * 32;
    private static final int MIN_CHIME_TICKS = 12;
    private static final float DOOR_OPEN_THRESHOLD = 0.08f;

    private final Map<Long, TrackedLift> tracked = new HashMap<>();
    private ResourceKey<Level> currentDimension;
    private ClientLevel currentLevel;
    private long tick;

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        ElevatorMediaStore.preload();

        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null || minecraft.player == null) {
            reset();
            return;
        }
        if (level != currentLevel || !level.dimension().equals(currentDimension)) {
            reset();
            currentLevel = level;
            currentDimension = level.dimension();
        }
        tick++;

        for (MinecraftClientData.LiftWrapper wrapper : MinecraftClientData.getInstance().liftWrapperList.values()) {
            Lift lift = wrapper.getLift();
            if (lift == null || lift.getCurrentFloor() == null) continue;

            long id = lift.getId();
            LiftMediaRuntime media = ElevatorMediaStore.runtimeFor(id);
            if (media == null || !media.hasChimeSettings()) {
                tracked.remove(id);
                continue;
            }

            LiftDirection now = lift.getDirection();
            float doorValue = lift.getDoorValue();
            long floorY = lift.getCurrentFloor().getPosition().getY();
            TrackedLift state = tracked.get(id);
            if (state == null) {
                tracked.put(id, new TrackedLift(now, doorValue, floorY, tick));
                continue;
            }

            LiftDirection was = state.direction;
            float beforeDoor = state.doorValue;
            long priorFloor = state.floorY;
            state.direction = now;
            state.doorValue = doorValue;
            state.floorY = floorY;
            state.lastSeen = tick;

            boolean moving = now == LiftDirection.UP || now == LiftDirection.DOWN;
            boolean newTrip = moving && (was == LiftDirection.NONE
                    || (!state.armed && !state.playedForStop));
            if (newTrip || floorY != priorFloor) {
                state.playedForStop = false;
                state.armed = true;
            }
            if (moving) {
                state.movementDirection = now;
            } else if (floorY != priorFloor) {
                state.movementDirection = floorY > priorFloor ? LiftDirection.UP : LiftDirection.DOWN;
            }

            boolean stopped = was != LiftDirection.NONE && now == LiftDirection.NONE;
            boolean opened = beforeDoor < DOOR_OPEN_THRESHOLD && doorValue >= DOOR_OPEN_THRESHOLD;
            if (!state.armed || state.playedForStop || (!stopped && !opened)
                    || tick - state.lastChime < MIN_CHIME_TICKS) {
                continue;
            }
            state.armed = false;
            state.playedForStop = true;

            var floor = lift.getCurrentFloor().getPosition();
            double x = floor.getX() + 0.5;
            double y = floor.getY() + 0.5;
            double z = floor.getZ() + 0.5;
            if (minecraft.player.distanceToSqr(x, y, z) > MAX_DISTANCE_SQUARED) continue;

            SoundEvent sound = state.movementDirection == LiftDirection.UP
                    ? media.upChime() : media.downChime();
            if (sound == null) continue;

            level.playLocalSound(x, y, z, sound, SoundSource.BLOCKS, 1f, 1f, false);
            state.lastChime = tick;
            LOGGER.debug("Played configured lift {} arrival {} sound {} (door={}, stopped={})",
                    id, state.movementDirection, sound.getLocation(), opened, stopped);
        }

        if ((tick & 31) == 0 && !tracked.isEmpty()) {
            Iterator<TrackedLift> iterator = tracked.values().iterator();
            while (iterator.hasNext()) {
                if (iterator.next().lastSeen != tick) iterator.remove();
            }
        }
    }

    private void reset() {
        tracked.clear();
        tick = 0;
        currentDimension = null;
        currentLevel = null;
    }

    private static final class TrackedLift {
        private LiftDirection direction;
        private LiftDirection movementDirection;
        private float doorValue;
        private long floorY;
        private long lastSeen;
        private long lastChime = Long.MIN_VALUE / 2;
        private boolean armed;
        private boolean playedForStop;

        private TrackedLift(LiftDirection direction, float doorValue, long floorY, long tick) {
            this.direction = direction;
            this.movementDirection = direction;
            this.doorValue = doorValue;
            this.floorY = floorY;
            this.lastSeen = tick;
            // First observation must never emit a chime.
            this.armed = false;
        }
    }
}
