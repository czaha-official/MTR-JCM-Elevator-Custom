package top.czaha.mtrjcmev;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.mtr.core.data.Lift;
import org.mtr.core.data.LiftDirection;
import org.mtr.mod.client.MinecraftClientData;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Client-only elevator arrival sounds.
 * A direction change from UP/DOWN to NONE is treated as an arrival.
 * On joining a world, initial states are memorized without triggering sounds.
 */
public final class LiftArrivalSoundController {
    private static final double MAX_DISTANCE_SQUARED = 24 * 24;
    private final Map<Long, LiftDirection> previousDirection = new HashMap<>();
    private ResourceKey<Level> currentDimension;
    private ClientLevel currentLevel;

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null || minecraft.player == null) {
            previousDirection.clear();
            currentLevel = null;
            currentDimension = null;
            return;
        }
        if (level != currentLevel || !level.dimension().equals(currentDimension)) {
            currentLevel = level;
            currentDimension = level.dimension();
            previousDirection.clear();
        }

        Set<Long> seen = new HashSet<>();
        for (MinecraftClientData.LiftWrapper wrapper : MinecraftClientData.getInstance().liftWrapperList.values()) {
            Lift lift = wrapper.getLift();
            if (lift == null || lift.getCurrentFloor() == null) continue;
            long id = lift.getId();
            seen.add(id);
            LiftDirection now = lift.getDirection();
            LiftDirection previous = previousDirection.put(id, now);
            if (previous == null || previous == LiftDirection.NONE || now != LiftDirection.NONE) continue;

            String liftId = Long.toString(id);
            if (!ElevatorMediaStore.hasChimeSettings(liftId)) continue;
            String key = previous == LiftDirection.UP ? "chime_up" : "chime_down";
            String resource = ElevatorMediaStore.get(liftId, key);
            if (resource == null || resource.isBlank()) {
                resource = previous == LiftDirection.UP
                        ? "yte:otis_series_3_lantern_1_up"
                        : "yte:otis_series_3_lantern_1_down";
            }

            ResourceLocation eventId = ResourceLocation.tryParse(resource);
            if (eventId == null) continue;
            var position = lift.getCurrentFloor().getPosition();
            double x = position.getX() + 0.5, y = position.getY() + 0.5, z = position.getZ() + 0.5;
            if (minecraft.player.distanceToSqr(x, y, z) > MAX_DISTANCE_SQUARED) continue;
            level.playLocalSound(x, y, z, SoundEvent.createVariableRangeEvent(eventId),
                    SoundSource.BLOCKS, 1f, 1f, false);
        }
        previousDirection.keySet().retainAll(seen);
    }
}
