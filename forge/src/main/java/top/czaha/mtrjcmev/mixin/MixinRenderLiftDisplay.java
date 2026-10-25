package top.czaha.mtrjcmev.mixin;

import org.mtr.core.data.Lift;
import org.mtr.core.data.LiftDirection;
import org.mtr.libraries.it.unimi.dsi.fastutil.objects.ObjectObjectImmutablePair;
import org.mtr.mapping.holder.Direction;
import org.mtr.mapping.holder.Identifier;
import org.mtr.mapping.holder.World;
import org.mtr.mapping.mapper.GraphicsHolder;
import org.mtr.mod.Init;
import org.mtr.mod.client.IDrawing;
import org.mtr.mod.data.IGui;
import org.mtr.mod.render.MainRenderer;
import org.mtr.mod.render.QueuedRenderLayer;
import org.mtr.mod.render.RenderLifts;
import org.mtr.mod.render.StoredMatrixTransformations;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.czaha.mtrjcmev.ElevatorMediaStore;
import top.czaha.mtrjcmev.LiftMediaRuntime;

@Mixin(value = RenderLifts.class, remap = false)
public abstract class MixinRenderLiftDisplay {
    private static final int DISPLAY_COLOR = 0xFFFF0000;

    @Inject(method = "renderLiftDisplay", at = @At("HEAD"), cancellable = true, remap = false)
    private static void mtrJcmEv$renderConfiguredDisplay(StoredMatrixTransformations transformations,
                                                          World world, Lift lift, float width, float height,
                                                          CallbackInfo ci) {
        if (lift == null || lift.getCurrentFloor() == null || width <= 0 || height <= 0) return;
        LiftMediaRuntime runtime = ElevatorMediaStore.runtimeFor(lift.getId());
        if (runtime == null || !runtime.hasVisualSettings()) return;

        ObjectObjectImmutablePair<LiftDirection, ObjectObjectImmutablePair<String, String>> details =
                RenderLifts.getLiftDetails(world, lift, Init.positionToBlockPos(lift.getCurrentFloor().getPosition()));
        String number = details.right().left();
        LiftDirection direction = details.left();

        if (runtime.floorEnabled() && number != null && !number.isEmpty()) {
            MainRenderer.scheduleRender(QueuedRenderLayer.TEXT, (graphicsHolder, offset) -> {
                transformations.transform(graphicsHolder, offset);
                if (runtime.customFont()) {
                    mtrJcmEv$drawStyledFloor(graphicsHolder, runtime, number, width, height);
                } else {
                    IDrawing.drawStringWithFont(graphicsHolder, number, IGui.HorizontalAlignment.CENTER,
                            IGui.VerticalAlignment.BOTTOM, 0, height, width, -1, 18 / width,
                            DISPLAY_COLOR, false, GraphicsHolder.getDefaultLight(), null);
                }
                graphicsHolder.pop();
            });
        }

        if (direction != LiftDirection.NONE) {
            Identifier texture = direction == LiftDirection.UP ? runtime.upArrow() : runtime.downArrow();
            if (texture != null) {
                MainRenderer.scheduleRender(texture, false, QueuedRenderLayer.LIGHT_TRANSLUCENT,
                        (graphicsHolder, offset) -> {
                            transformations.transform(graphicsHolder, offset);
                            IDrawing.drawTexture(graphicsHolder, -width / 6, 0, width / 3, width / 3,
                                    0, 0, 1, 1, Direction.UP, 0xFFFFFFFF,
                                    GraphicsHolder.getDefaultLight());
                            graphicsHolder.pop();
                        });
            }
        }
        ci.cancel();
    }

    private static void mtrJcmEv$drawStyledFloor(GraphicsHolder holder, LiftMediaRuntime runtime,
                                                   String number, float width, float height) {
        LiftMediaRuntime.FloorGlyph glyph = runtime.glyph(number);
        if (glyph == null) return;
        float pixelsPerBlock = Math.max(18f / width, Math.max(glyph.width() / width, 9f / height));
        holder.push();
        holder.scale(1f / pixelsPerBlock, 1f / pixelsPerBlock, 1f / pixelsPerBlock);
        holder.drawText(glyph.text(), -glyph.width() / 2, Math.round(height * pixelsPerBlock - 9),
                DISPLAY_COLOR, false, GraphicsHolder.getDefaultLight());
        holder.pop();
    }
}
