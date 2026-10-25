package top.czaha.mtrjcmev.mixin;

import org.mtr.core.data.Lift;
import org.mtr.mapping.mapper.ButtonWidgetExtension;
import org.mtr.mapping.holder.ClickableWidget;
import org.mtr.mod.screen.LiftCustomizationScreen;
import org.mtr.mod.screen.MTRScreenBase;
import top.czaha.mtrjcmev.ElevatorMediaStore;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = LiftCustomizationScreen.class, remap = false)
public abstract class MixinLiftCustomizationScreen extends MTRScreenBase {
    @Shadow @Final private Lift lift;
    @Shadow @Final private ButtonWidgetExtension buttonLiftStyle;

    @Inject(method = "init2", at = @At("TAIL"), remap = false)
    private void mtrJcmEv$addMediaButtons(CallbackInfo ci) {
        if (lift == null || buttonLiftStyle == null) return;
        // MTR reserves the row between offset Z (row 5) and double-sided (row 7).
        // Placing our button below Lift Style would overlap the rotation buttons.
        int buttonHeight = Math.max(16, buttonLiftStyle.getHeight2() - 4);
        int buttonWidth = Math.min(buttonLiftStyle.getWidth2() - 4, Math.max(0, this.width - 10));
        if (buttonWidth <= 0 || this.height < 25) return;
        int x = Math.max(5, Math.min(buttonLiftStyle.getX2() + 2, this.width - buttonWidth - 5));
        int y = buttonLiftStyle.getY2() - 2 * buttonLiftStyle.getHeight2() + 2;
        if (y < 5 || y + buttonHeight > this.height - 5) return;
        addChild(new ClickableWidget(new ButtonWidgetExtension(x, y, buttonWidth, buttonHeight,
                "Media Settings", button ->
                ElevatorMediaStore.openSettings(String.valueOf(lift.getId())))));
    }
}
