package top.czaha.mtrjcmev;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.mtr.core.data.Lift;
import org.mtr.core.data.LiftDirection;
import org.mtr.mod.client.MinecraftClientData;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Detect arrivals once per client tick without building a temporary set each tick.
 * Only explicitly configured lifts have tracking entries. Inactive entries are
 * removed periodically to prevent state retention when lifts unload.
 */
public final class LiftArrivalSoundController {
    private static final double MAX_DISTANCE_SQUARED = 24 * 24;
    private static final int MIN_CHIME_TICKS = 12;
    private final Map<Long, TrackedLift> tracked = new HashMap<>();
    private ResourceKey<Level> currentDimension;
    private ClientLevel currentLevel;
    private long tick;

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        // Ensure the config is loaded on the client tick, never during a render callback.
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
            if (media == null) continue;

            LiftDirection now = lift.getDirection();
            TrackedLift previous = tracked.get(id);
            if (previous == null) {
                tracked.put(id, new TrackedLift(now, tick));
                continue;
            }
            LiftDirection was = previous.direction;
            previous.direction = now;
            previous.lastSeen = tick;

            if (was == LiftDirection.NONE || now != LiftDirection.NONE || was == null) continue;
            if (tick - previous.lastChime < MIN_CHIME_TICKS) continue;

            var position = lift.getCurrentFloor().getPosition();
            double x = position.getX() + 0.5;
            double y = position.getY() + 0.5;
            double z = position.getZ() + 0.5;
            if (minecraft.player.distanceToSqr(x, y, z) > MAX_DISTANCE_SQUARED) continue;

            SoundEvent chime = was == LiftDirection.UP ? media.upChime() : media.downChime();
            if (chime != null) {
                level.playLocalSound(x, y, z, chime, SoundSource.BLOCKS, 1f, 1f, false);
                previous.lastChime = tick;
            }
        }

        // Sweep every 32 ticks instead of allocating a HashSet and retaining keys on every tick.
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
        private long lastSeen;
        private long lastChime;

        private TrackedLift(LiftDirection initial, long tick) {
            direction = initial;
            lastSeen = tick;
            lastChime = Long.MIN_VALUE / 2;
        }
    }
}
