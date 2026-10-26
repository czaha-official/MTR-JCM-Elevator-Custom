package top.czaha.mtrjcmev;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;

@Mod(MtrJcmEvForge.MOD_ID)
public final class MtrJcmEvForge {
    public static final String MOD_ID = "mtr_jcm_ev";

    public MtrJcmEvForge() {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                {
                    MinecraftForge.EVENT_BUS.register(new ClientSettingsBootstrap());
                    MinecraftForge.EVENT_BUS.register(new LiftTimedChimeController());
                });
    }
}
