package top.czaha.mtrjcmev.mixin;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mtr.core.data.Lift;
import org.mtr.core.data.LiftDirection;
import org.mtr.libraries.com.logisticscraft.occlusionculling.util.Vec3d;
import org.mtr.libraries.it.unimi.dsi.fastutil.ints.IntObjectImmutablePair;
import org.mtr.libraries.it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.mtr.libraries.it.unimi.dsi.fastutil.objects.ObjectObjectImmutablePair;
import org.mtr.mapping.holder.Box;
import org.mtr.mapping.holder.ClientPlayerEntity;
import org.mtr.mapping.holder.ClientWorld;
import org.mtr.mapping.holder.Direction;
import org.mtr.mapping.holder.Identifier;
import org.mtr.mapping.holder.Vector3d;
import org.mtr.mapping.holder.World;
import org.mtr.mapping.mapper.GraphicsHolder;
import org.mtr.mapping.mapper.OptimizedRenderer;
import org.mtr.mod.Init;
import org.mtr.mod.client.IDrawing;
import org.mtr.mod.client.MinecraftClientData;
import org.mtr.mod.data.IGui;
import org.mtr.mod.render.MainRenderer;
import org.mtr.mod.render.PositionAndRotation;
import org.mtr.mod.render.QueuedRenderLayer;
import org.mtr.mod.render.RenderLifts;
import org.mtr.mod.render.StoredMatrixTransformations;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import top.czaha.mtrjcmev.ElevatorMediaStore;
import top.czaha.mtrjcmev.LiftMediaRuntime;

/**
 * Priority > JCM's own RenderLiftsMixin, so our displays can be scheduled even
 * if JCM cancels the default lift renderer for a script-controlled lift style.
 */
@Mixin(value = RenderLifts.class, priority = 2000, remap = false)
public abstract class MixinRenderLiftDisplay {
    private static final int DISPLAY_COLOR = 0xFFFF0000;
    private static final Logger LOGGER = LogManager.getLogger("MTR-JCM-Elevator-Custom");
    // id and remaining calls; reusable per-thread, no per-frame allocation or private nested class.
    private static final ThreadLocal<long[]> PRE_RENDERED = new ThreadLocal<>();
    private static boolean loggedOverlay;

    private static long[] mtrJcmEv$suppression() {
        long[] state = PRE_RENDERED.get();
        if (state == null) {
            state = new long[2];
            PRE_RENDERED.set(state);
        }
        return state;
    }

    /**
     * JCM can cancel the render lambda before the vanilla in-cab display call.
     * Never carry a suppression marker into another lift or frame.
     */
    @Inject(method = "lambda$render$6", at = @At("HEAD"), require = 0, remap = false)
    private static void mtrJcmEv$beginLift(boolean isHoldingRefresher, ClientWorld clientWorld,
            ObjectArrayList cullingTasks, Vec3d camera, Vector3d cameraShakeOffset, boolean canRide,
            ClientPlayerEntity player, long millisElapsed, MinecraftClientData.LiftWrapper wrapper,
            CallbackInfo ci) {
        mtrJcmEv$suppression()[1] = 0;
    }

    @Inject(method = "lambda$render$6", at = @At(value = "INVOKE", target =
            "Lorg/mtr/mod/model/ModelLift1;render(Lorg/mtr/mod/render/StoredMatrixTransformations;Lorg/mtr/core/data/NameColorDataBase;Lorg/mtr/mapping/holder/Identifier;IFFZIIZZZZZ)V"),
            locals = LocalCapture.CAPTURE_FAILSOFT, require = 0, remap = false)
    private static void mtrJcmEv$beforeBuiltinLift(boolean isHoldingRefresher, ClientWorld clientWorld,
            ObjectArrayList cullingTasks, Vec3d camera, Vector3d cameraShakeOffset, boolean canRide,
            ClientPlayerEntity player, long millisElapsed, MinecraftClientData.LiftWrapper wrapper,
            CallbackInfo ci, Lift lift, PositionAndRotation absolutePositionAndRotation,
            IntObjectImmutablePair ridingVehicleCarNumberAndOffset,
            PositionAndRotation ridingCarPositionAndRotation, Vector3d offsetVector, Double offsetRotation,
            PositionAndRotation renderingPositionAndRotation, ObjectArrayList floorsAndDoorways,
            ObjectArrayList openDoorways, Box doorway1, Box doorway2, boolean doorway1Open,
            boolean doorway2Open, StoredMatrixTransformations matrices) {
        if (OptimizedRenderer.renderingShadows() || clientWorld == null || lift == null
                || lift.getCurrentFloor() == null) return;

        LiftMediaRuntime media = ElevatorMediaStore.runtimeFor(lift.getId());
        if (media == null || !media.hasVisualSettings()) {
            mtrJcmEv$suppression()[1] = 0;
            return;
        }

        World world = new World(clientWorld.data);
        int sides = lift.getIsDoubleSided() ? 2 : 1;
        for (int i = 0; i < sides; i++) {
            StoredMatrixTransformations display = matrices.copy();
            boolean rotate = i == 0;
            display.add(graphics -> {
                if (rotate) graphics.rotateYDegrees(180);
                graphics.translate(0.875F, -1.5, lift.getDepth() / 2 - 0.25F - 0.0005F);
            });
            mtrJcmEv$drawConfigured(display, world, lift, media, 0.1875F, 0.3125F);
        }
        long[] suppression = mtrJcmEv$suppression();
        suppression[0] = lift.getId();
        suppression[1] = sides;
        if (!loggedOverlay) {
            loggedOverlay = true;
            LOGGER.info("MTR in-car lift media overlay hooked (lift id: {})", lift.getId());
        }
    }

    @Inject(method = "renderLiftDisplay", at = @At("HEAD"), cancellable = true, remap = false)
    private static void mtrJcmEv$renderConfiguredDisplay(StoredMatrixTransformations matrices,
                                                          World world, Lift lift, float width, float height,
                                                          CallbackInfo ci) {
        if (lift == null || lift.getCurrentFloor() == null || width <= 0 || height <= 0
                || OptimizedRenderer.renderingShadows()) return;
        LiftMediaRuntime media = ElevatorMediaStore.runtimeFor(lift.getId());
        if (media == null || !media.hasVisualSettings()) return;

        // Skip duplicate display calls for the same lift only. The fallback
        // above still renders normally if the pre-model hook is unavailable.
        long[] suppression = mtrJcmEv$suppression();
        if (suppression[0] == lift.getId() && suppression[1] > 0) {
            suppression[1]--;
            ci.cancel();
            return;
        }

        mtrJcmEv$drawConfigured(matrices, world, lift, media, width, height);
        ci.cancel();
    }

    private static void mtrJcmEv$drawConfigured(StoredMatrixTransformations matrices, World world, Lift lift,
                                                LiftMediaRuntime media, float width, float height) {
        ObjectObjectImmutablePair<LiftDirection, ObjectObjectImmutablePair<String, String>> details =
                RenderLifts.getLiftDetails(world, lift, Init.positionToBlockPos(lift.getCurrentFloor().getPosition()));
        String number = details.right().left();
        LiftDirection direction = details.left();

        if (media.floorEnabled() && number != null && !number.isEmpty()) {
            MainRenderer.scheduleRender(QueuedRenderLayer.TEXT, (graphicsHolder, offset) -> {
                matrices.transform(graphicsHolder, offset);
                if (media.customFont()) {
                    mtrJcmEv$drawStyledFloor(graphicsHolder, media, number, width, height);
                } else {
                    IDrawing.drawStringWithFont(graphicsHolder, number, IGui.HorizontalAlignment.CENTER,
                            IGui.VerticalAlignment.BOTTOM, 0, height, width, -1, 18 / width,
                            DISPLAY_COLOR, false, GraphicsHolder.getDefaultLight(), null);
                }
                graphicsHolder.pop();
            });
        }

        if (direction != LiftDirection.NONE) {
            Identifier texture = direction == LiftDirection.UP ? media.upArrow() : media.downArrow();
            if (texture != null) {
                MainRenderer.scheduleRender(texture, false, QueuedRenderLayer.LIGHT_TRANSLUCENT,
                        (graphicsHolder, offset) -> {
                            matrices.transform(graphicsHolder, offset);
                            IDrawing.drawTexture(graphicsHolder, -width / 6, 0, width / 3, width / 3,
                                    0, 0, 1, 1, Direction.UP, 0xFFFFFFFF, GraphicsHolder.getDefaultLight());
                            graphicsHolder.pop();
                        });
            }
        }
    }

    private static void mtrJcmEv$drawStyledFloor(GraphicsHolder holder, LiftMediaRuntime media,
                                                   String number, float width, float height) {
        LiftMediaRuntime.FloorGlyph glyph = media.glyph(number);
        if (glyph == null) return;
        float scale = Math.max(18f / width, Math.max(glyph.width() / width, 9f / height));
        holder.push();
        holder.scale(1f / scale, 1f / scale, 1f / scale);
        holder.drawText(glyph.text(), -glyph.width() / 2, Math.round(height * scale - 9),
                DISPLAY_COLOR, false, GraphicsHolder.getDefaultLight());
        holder.pop();
    }

}
