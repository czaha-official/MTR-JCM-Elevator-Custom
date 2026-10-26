package top.czaha.mtrjcmev.mixin;

import com.lx862.jcm.mod.extra.JCMPatchForMTR;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
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
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.czaha.mtrjcmev.ElevatorMediaStore;
import top.czaha.mtrjcmev.LiftMediaRuntime;
import top.czaha.mtrjcmev.LiftTravelState;
import top.czaha.mtrjcmev.LiftTimedChimeController;

import java.util.HashMap;
import java.util.Map;

/**
 * JCM owns the actual client-side elevator arrival ding:
 * JCMPatchForMTR.liftJustArrived(id, instructions.size()).
 *
 * Instead of guessing from door animation, replace JCM's actual arrival
 * signal for configured lifts. Returning false prevents JCM's default
 * note_block.pling while leaving ordinary lifts completely untouched.
 */
@Mixin(value = JCMPatchForMTR.class, priority = 2000, remap = false)
public abstract class MixinJcmArrivalChime {
    @Unique
    private static final Logger MTR_JCM_EV_LOGGER = LogManager.getLogger("MTR-JCM-Elevator-Custom");
    @Unique
    private static final Map<Long, LiftTravelState> MTR_JCM_EV_TRAVEL = new HashMap<>();
    @Unique
    private static ClientLevel MTR_JCM_EV_WORLD;
    @Unique
    private static long MTR_JCM_EV_CALLS;

    @Inject(method = "liftJustArrived", at = @At("RETURN"), cancellable = true, remap = false)
    private static void mtrJcmEv$replaceJcmChime(long liftId, int instructionCount,
                                                  CallbackInfoReturnable<Boolean> cir) {
        ElevatorMediaStore.preload();
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null || level != MTR_JCM_EV_WORLD) {
            MTR_JCM_EV_TRAVEL.clear();
            MTR_JCM_EV_WORLD = level;
        }
        if (level == null || mc.player == null) return;

        Lift lift = MinecraftClientData.getLift(liftId);
        if (lift == null || lift.getCurrentFloor() == null) return;

        long floorY = lift.getCurrentFloor().getPosition().getY();
        LiftDirection currentDirection = lift.getDirection();
        LiftTravelState history = MTR_JCM_EV_TRAVEL.get(liftId);
        if (history == null) {
            history = new LiftTravelState(floorY);
            MTR_JCM_EV_TRAVEL.put(liftId, history);
        }
        history.update(currentDirection, floorY, lift.hasCoolDown());
        history.markSeen(++MTR_JCM_EV_CALLS);

        // Very lightweight pruning when lifts have been removed/unloaded.
        if ((MTR_JCM_EV_CALLS & 4095) == 0) {
            long expiry = MTR_JCM_EV_CALLS - 8192;
            MTR_JCM_EV_TRAVEL.values().removeIf(state -> state.getLastSeenCall() < expiry);
        }

        if (!cir.getReturnValueZ()) return;
        LiftMediaRuntime selected = ElevatorMediaStore.runtimeFor(liftId);
        if (selected == null || !selected.hasChimeSettings()) return;

        SoundEvent sound = history.getLastDirection() == LiftDirection.DOWN
                ? selected.downChime()
                : history.getLastDirection() == LiftDirection.UP ? selected.upChime() : null;
        // Only replace directions explicitly selected by the user. A null or
        // unknown direction leaves JCM\u0027s original arrival chime intact.
        if (sound == null) return;
        if (mc.getSoundManager().getSoundEvent(sound.getLocation()) == null) {
            // The configured sound cannot resolve; let JCM play its normal
            // chime rather than silently disabling the arrival sound.
            MTR_JCM_EV_LOGGER.warn("Lift {} selected sound {} is not loaded. Enable/reload the media pack or YTE.",
                    liftId, sound.getLocation());
            return;
        }

        ClientWorld mappedWorld = MinecraftClient.getInstance().getWorldMapped();
        if (mappedWorld == null) return;
        BlockPos trackPos = Init.positionToBlockPos(lift.getCurrentFloor().getPosition());
        BlockEntity track = mappedWorld.getBlockEntity(trackPos);
        if (track == null || !(track.data instanceof BlockLiftTrackFloor.BlockEntity)) {
            return;
        }
        if (!((BlockLiftTrackFloor.BlockEntity) track.data).getShouldDing()) {
            return;
        }

        if (selected.chimeTiming() == LiftMediaRuntime.ChimeTiming.DECELERATION
                && LiftTimedChimeController.alreadyPlayed(liftId)) {
            // The chosen sound was already emitted while the lift slowed down.
            cir.setReturnValue(false);
            return;
        }
        if (selected.chimeTiming() == LiftMediaRuntime.ChimeTiming.DOOR_OPEN
                && LiftTimedChimeController.deferUntilDoorOpens(liftId, history.getLastDirection())) {
            // JCM has confirmed arrival. Defer our sound until the actual door movement.
            cir.setReturnValue(false);
            return;
        }

        double x = trackPos.getX() + 0.5;
        double y = trackPos.getY() + 0.5;
        double z = trackPos.getZ() + 0.5;
        // Do not play sounds for elevators inaudible to the local client.
        if (mc.player.distanceToSqr(x, y, z) <= 32d * 32d) {
            level.playLocalSound(x, y, z, sound, SoundSource.BLOCKS, 1.0f, 1.0f, false);
            MTR_JCM_EV_LOGGER.info("Played lift {} configured {} sound {}",
                    liftId, history.getLastDirection(), sound.getLocation());
        }
        // Prevent the early-chime listener from replaying during the same stop.
        if (selected.chimeTiming() == LiftMediaRuntime.ChimeTiming.DECELERATION) {
            LiftTimedChimeController.markArrivalPlayed(liftId);
        }
        // Replaces JCM's default "minecraft:block.note_block.pling" for this lift.
        cir.setReturnValue(false);
    }

}
