package de.thedead2.customadvancements;

import de.thedead2.customadvancements.events.ClientEventListeners;
import de.thedead2.customadvancements.util.core.ModHelper;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;

@EventBusSubscriber(value = Dist.CLIENT, modid = ModHelper.MOD_ID)
public class ClientListeners {

    @SubscribeEvent
    public static void beforeScreenInit(final ScreenEvent.Init.Pre event) {
        ClientEventListeners.beforeScreenInit(event.getScreen());
    }

    @SubscribeEvent
    public static void afterScreenInit(final ScreenEvent.Init.Post event) {
        ClientEventListeners.afterScreenInit(event.getScreen(), event.getListenersList(), event::removeListener);
    }

    @SubscribeEvent
    public static void onClientLogout(final ClientPlayerNetworkEvent.LoggingOut event) {
        ClientEventListeners.onClientLogout();
    }
}
