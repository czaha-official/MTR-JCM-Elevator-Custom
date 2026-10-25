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
        int x = buttonLiftStyle.getX2();
        int y = buttonLiftStyle.getY2() + buttonLiftStyle.getHeight2() + 4;
        int width = buttonLiftStyle.getWidth2();
        addRenderableWidget(new ButtonWidgetExtension(x, y, width, 20, "Elevator Media Settings", button ->
                ElevatorMediaStore.openSettings(String.valueOf(lift.getId()))));
    }
}
