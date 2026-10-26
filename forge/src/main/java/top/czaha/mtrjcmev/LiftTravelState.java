package top.czaha.mtrjcmev;

import org.mtr.core.data.LiftDirection;

/**
 * Independent state holder for JCM's arrival Mixin.
 * Public methods avoid Java 17 nestmate access failures when the injected
 * code is merged into JCMPatchForMTR at runtime.
 */
public final class LiftTravelState {
    private long lastFloorY;
    private LiftDirection lastDirection = LiftDirection.NONE;
    private long lastSeenCall;

    public LiftTravelState(long firstFloorY) {
        this.lastFloorY = firstFloorY;
    }

    public void update(LiftDirection now, long currentFloorY, boolean stopping) {
        // The next queued instruction can already point in the opposite
        // direction while doors are opening at an intermediate stop.
        // Prefer the most recent actual floor movement over that next leg.
        if (currentFloorY != lastFloorY) {
            lastDirection = currentFloorY > lastFloorY ? LiftDirection.UP : LiftDirection.DOWN;
        } else if (!stopping && (now == LiftDirection.UP || now == LiftDirection.DOWN)) {
            lastDirection = now;
        }
        lastFloorY = currentFloorY;
    }

    public LiftDirection getLastDirection() {
        return lastDirection;
    }

    public long getLastSeenCall() {
        return lastSeenCall;
    }

    public void markSeen(long call) {
        lastSeenCall = call;
    }
}
