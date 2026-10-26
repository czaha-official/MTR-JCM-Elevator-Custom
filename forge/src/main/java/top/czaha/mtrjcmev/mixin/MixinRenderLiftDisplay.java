package top.czaha.mtrjcmev.mixin;

import com.lx862.mtrscripting.mod.resource.MtrScriptingResourceManager;
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
 * Default MTR lifts use their normal built-in display geometry.
 * JCM script-controlled lifts own both the geometry and the screen texture.
 * We MUST NOT unconditionally draw MTR's tiny red floor number over a custom
 * script-controlled display (this caused the red square/zero in black screens).
 * A scripted lift may explicitly opt in to an arrow-only overlay. It never
 * gets an unwanted floor number or a second backing panel.
 */
@Mixin(value = RenderLifts.class, priority = 2000, remap = false)
public abstract class MixinRenderLiftDisplay {
    private static final int DISPLAY_COLOR = 0xFFFF0000;
    private static final Identifier MTR_ARROW = new Identifier(Init.MOD_ID, "textures/block/sign/lift_arrow.png");
    private static final Logger LOGGER = LogManager.getLogger("MTR-JCM-Elevator-Custom");
    private static boolean loggedScriptOverlay;

    /**
     * Render only on opted-in script-controlled lifts; normal lift styles are
     * handled solely by renderLiftDisplay() below with the native geometry.
     */
    @Inject(method = "lambda$render$6", at = @At(value = "INVOKE", target =
            "Lorg/mtr/mod/model/ModelLift1;render(Lorg/mtr/mod/render/StoredMatrixTransformations;Lorg/mtr/core/data/NameColorDataBase;Lorg/mtr/mapping/holder/Identifier;IFFZIIZZZZZ)V"),
            locals = LocalCapture.CAPTURE_FAILSOFT, require = 0, remap = false)
    private static void mtrJcmEv$drawScriptedArrow(boolean isHoldingRefresher, ClientWorld clientWorld,
            ObjectArrayList cullingTasks, Vec3d camera, Vector3d cameraShakeOffset, boolean canRide,
            ClientPlayerEntity player, long millisElapsed, MinecraftClientData.LiftWrapper wrapper,
            CallbackInfo ci, Lift lift, PositionAndRotation absolutePositionAndRotation,
            IntObjectImmutablePair ridingVehicleCarNumberAndOffset,
            PositionAndRotation ridingCarPositionAndRotation, Vector3d offsetVector, Double offsetRotation,
            PositionAndRotation renderingPositionAndRotation, ObjectArrayList floorsAndDoorways,
            ObjectArrayList openDoorways, Box doorway1, Box doorway2, boolean doorway1Open,
            boolean doorway2Open, StoredMatrixTransformations matrices) {
        if (OptimizedRenderer.renderingShadows() || lift == null || lift.getCurrentFloor() == null) return;
        LiftMediaRuntime media = ElevatorMediaStore.runtimeFor(lift.getId());
        if (media == null || !media.scriptedOverlay() || !media.hasArrowSettings()
                || !MtrScriptingResourceManager.lift.isScriptControlledRendering(lift.getStyle())) return;

        LiftDirection direction = lift.getDirection();
        if (direction == LiftDirection.NONE || !media.hasArrowFor(direction)) return;
        int sides = lift.getIsDoubleSided() ? 2 : 1;
        for (int i = 0; i < sides; i++) {
            StoredMatrixTransformations display = matrices.copy();
            boolean rotate = i == 0;
            display.add(graphics -> {
                if (rotate) graphics.rotateYDegrees(180);
                graphics.translate(0.875F, -1.5, lift.getDepth() / 2 - 0.25F - 0.0005F);
            });
            mtrJcmEv$drawArrow(display, media, direction, 0.1875F);
        }
        if (!loggedScriptOverlay) {
            loggedScriptOverlay = true;
            LOGGER.info("Scripted lift arrow overlay enabled by user (lift id: {}).",
                    lift.getId());
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
        // JCM scripted styles manage their own display. Never overwrite their
        // screen texture by pretending it is a standard MTR model.
        if (MtrScriptingResourceManager.lift.isScriptControlledRendering(lift.getStyle())) return;

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
        // Only a direction explicitly configured by the user gets a custom
        // texture. Otherwise retain MTR's built-in animated UP/DOWN arrow.
        if (media.hasArrowFor(direction)) {
            mtrJcmEv$drawArrow(matrices, media, direction, width);
        } else {
            mtrJcmEv$drawVanillaArrow(matrices, direction, width);
        }
        ci.cancel();
    }

    private static void mtrJcmEv$drawVanillaArrow(StoredMatrixTransformations matrices,
                                                  LiftDirection direction, float width) {
        if (direction == LiftDirection.NONE) return;
        MainRenderer.scheduleRender(MTR_ARROW, false, QueuedRenderLayer.LIGHT_TRANSLUCENT,
                (graphicsHolder, offset) -> {
                    matrices.transform(graphicsHolder, offset);
                    IDrawing.drawTexture(graphicsHolder, -width / 6, 0, width / 3, width / 3,
                            0, direction == LiftDirection.UP ? 0 : 1, 1,
                            direction == LiftDirection.UP ? 1 : 0, Direction.UP,
                            DISPLAY_COLOR, GraphicsHolder.getDefaultLight());
                    graphicsHolder.pop();
                });
    }

    private static void mtrJcmEv$drawArrow(StoredMatrixTransformations matrices, LiftMediaRuntime media,
                                           LiftDirection direction, float width) {
        if (direction == LiftDirection.NONE) return;
        Identifier texture = direction == LiftDirection.UP ? media.upArrow() : media.downArrow();
        if (texture == null) {
            mtrJcmEv$drawVanillaArrow(matrices, direction, width);
            return;
        }
        MainRenderer.scheduleRender(texture, false, QueuedRenderLayer.LIGHT_TRANSLUCENT,
                (graphicsHolder, offset) -> {
                    matrices.transform(graphicsHolder, offset);
                    IDrawing.drawTexture(graphicsHolder, -width / 6, 0, width / 3, width / 3,
                            0, 0, 1, 1, Direction.UP, 0xFFFFFFFF, GraphicsHolder.getDefaultLight());
                    graphicsHolder.pop();
                });
    }

    private static void mtrJcmEv$drawStyledFloor(GraphicsHolder holder, LiftMediaRuntime media,
                                                   String number, float width, float height) {
        LiftMediaRuntime.FloorGlyph glyph = media.glyph(number);
        if (glyph == null) return;
        float pixelsPerBlock = Math.max(18f / width, Math.max(glyph.width() / width, 9f / height));
        holder.push();
        holder.scale(1f / pixelsPerBlock, 1f / pixelsPerBlock, 1f / pixelsPerBlock);
        holder.drawText(glyph.text(), -glyph.width() / 2, Math.round(height * pixelsPerBlock - 9),
                DISPLAY_COLOR, false, GraphicsHolder.getDefaultLight());
        holder.pop();
    }
}
