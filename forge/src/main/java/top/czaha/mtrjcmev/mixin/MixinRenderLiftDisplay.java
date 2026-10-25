package top.czaha.mtrjcmev.mixin;

import org.mtr.core.data.Lift;
import org.mtr.core.data.LiftDirection;
import org.mtr.libraries.it.unimi.dsi.fastutil.objects.ObjectObjectImmutablePair;
import org.mtr.mapping.holder.BlockPos;
import org.mtr.mapping.holder.Direction;
import org.mtr.mapping.mapper.GraphicsHolder;
import org.mtr.mapping.holder.Identifier;
import org.mtr.mapping.holder.OrderedText;
import org.mtr.mapping.holder.Style;
import org.mtr.mapping.mapper.TextHelper;
import org.mtr.mapping.holder.World;
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

@Mixin(value = RenderLifts.class, remap = false)
public abstract class MixinRenderLiftDisplay {
    private static final String DEFAULT_UP = "yte:block/otis_gen3_arrow_up";
    private static final String DEFAULT_DOWN = "yte:block/otis_gen3_arrow_down";
    private static final int DISPLAY_COLOR = 0xFFFF0000;

    @Inject(method = "renderLiftDisplay", at = @At("HEAD"), cancellable = true, remap = false)
    private static void mtrJcmEv$renderConfiguredDisplay(StoredMatrixTransformations transformations,
                                                          World world, Lift lift, float width, float height,
                                                          CallbackInfo ci) {
        if (lift == null || lift.getCurrentFloor() == null || width <= 0 || height <= 0) return;
        String id = Long.toString(lift.getId());
        if (!ElevatorMediaStore.hasVisualSettings(id)) return;

        ObjectObjectImmutablePair<LiftDirection, ObjectObjectImmutablePair<String, String>> details =
                RenderLifts.getLiftDetails(world, lift, Init.positionToBlockPos(lift.getCurrentFloor().getPosition()));
        String number = details.right().left();
        LiftDirection direction = details.left();

        if (!"false".equals(ElevatorMediaStore.get(id, "floor_enabled")) && number != null && !number.isEmpty()) {
            boolean useSelectedFont = ElevatorMediaStore.get(id, "font_custom") != null
                    || ElevatorMediaStore.get(id, "floor_font") != null;
            MainRenderer.scheduleRender(QueuedRenderLayer.TEXT, (graphicsHolder, offset) -> {
                transformations.transform(graphicsHolder, offset);
                if (useSelectedFont) {
                    mtrJcmEv$drawStyledFloor(graphicsHolder, id, number, width, height);
                } else {
                    IDrawing.drawStringWithFont(graphicsHolder, number, IGui.HorizontalAlignment.CENTER,
                            IGui.VerticalAlignment.BOTTOM, 0, height, width, -1, 18 / width,
                            DISPLAY_COLOR, false, GraphicsHolder.getDefaultLight(), null);
                }
                graphicsHolder.pop();
            });
        }

        if (direction != LiftDirection.NONE) {
            String selected = ElevatorMediaStore.get(id, direction == LiftDirection.UP ? "arrow_up" : "arrow_down");
            if (selected == null) selected = ElevatorMediaStore.get(id,
                    direction == LiftDirection.UP ? "yte_up" : "yte_down");
            if (selected == null) selected = direction == LiftDirection.UP ? DEFAULT_UP : DEFAULT_DOWN;
            Identifier texture = mtrJcmEv$resolveTexture(selected);
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

    private static Identifier mtrJcmEv$resolveTexture(String resource) {
        if (resource == null) return null;
        int separator = resource.indexOf(':');
        if (separator <= 0 || separator == resource.length() - 1) return null;
        String namespace = resource.substring(0, separator);
        String path = resource.substring(separator + 1);
        if (!path.matches("[a-z0-9_./-]+") || !namespace.matches("[a-z0-9_.-]+")) return null;
        if (path.startsWith("textures/")) return new Identifier(namespace, path.endsWith(".png") ? path : path + ".png");
        if (path.startsWith("block/") || path.startsWith("lift/")) {
            return new Identifier(namespace, "textures/" + path + ".png");
        }
        return null;
    }

    private static void mtrJcmEv$drawStyledFloor(GraphicsHolder holder, String liftId,
                                                   String number, float width, float height) {
        OrderedText styled = TextHelper.mutableTextToOrderedText(
                TextHelper.setStyle(TextHelper.literal(number), Style.getEmptyMapped()
                        .withFont(new Identifier("mtr_jcm_ev", "lift_" + liftId))));
        int textWidth = Math.max(1, GraphicsHolder.getTextWidth(styled));
        float pixelsPerBlock = Math.max(18f / width, Math.max(textWidth / width, 9f / height));
        holder.push();
        holder.scale(1f / pixelsPerBlock, 1f / pixelsPerBlock, 1f / pixelsPerBlock);
        holder.drawText(styled, -textWidth / 2, Math.round(height * pixelsPerBlock - 9),
                DISPLAY_COLOR, false, GraphicsHolder.getDefaultLight());
        holder.pop();
    }
}
