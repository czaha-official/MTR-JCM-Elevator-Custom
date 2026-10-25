package top.czaha.mtrjcmev.mixin;

import org.mtr.core.data.Lift;
import org.mtr.mapping.mapper.ButtonWidgetExtension;
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
        int buttonWidth = Math.min(buttonLiftStyle.getWidth2(), Math.max(0, this.width - 10));
        if (buttonWidth <= 0 || this.height < 25) return;
        int x = Math.max(5, Math.min(buttonLiftStyle.getX2(), this.width - buttonWidth - 5));
        int y = buttonLiftStyle.getY2() + buttonLiftStyle.getHeight2() + 4;
        if (y + 20 > this.height - 5) {
            y = Math.max(5, buttonLiftStyle.getY2() - 24);
        }
        y = Math.max(5, Math.min(y, this.height - 25));
        addRenderableWidget(new ButtonWidgetExtension(x, y, buttonWidth, 20, "Elevator Media Settings", button ->
                ElevatorMediaStore.openSettings(String.valueOf(lift.getId()))));
    }
}
