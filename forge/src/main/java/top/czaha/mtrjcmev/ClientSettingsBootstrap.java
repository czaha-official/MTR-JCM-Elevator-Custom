package top.czaha.mtrjcmev;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Load media settings once on the client thread before rendering any lifts.
 * Arrival detection is handled by the JCM arrival hook, not by scanning lifts.
 */
public final class ClientSettingsBootstrap {
    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        ElevatorMediaStore.preload();
        MinecraftForge.EVENT_BUS.unregister(this);
    }
}
