package de.thedead2.customadvancements.util.handler;

import de.thedead2.customadvancements.util.core.ConfigManager;
import de.thedead2.customadvancements.util.core.ModHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;

import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.Optional;

@Mod.EventBusSubscriber(modid = ModHelper.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ScreenHandler {
    @SubscribeEvent
    public static void afterScreenInit(ScreenEvent.InitScreenEvent.Post event) {
        Screen screen = event.getScreen();
        if(screen instanceof PauseScreen pauseScreen){
            if(!pauseScreen.showPauseMenu) return;
            if (ConfigManager.NO_ADVANCEMENTS.get()) {
                findButton(event.getListenersList(), "gui.advancements").ifPresent(event::removeListener);
                findButton(event.getListenersList(), "gui.stats").ifPresent(button1 -> {
                    button1.setWidth(204);
                    button1.x = button1.x - (204 / 2 + 4);
                });
            }
        }
    }

    @SubscribeEvent
    public static void beforeScreenInit(ScreenEvent.InitScreenEvent.Pre event){
        if (event.getScreen() instanceof AdvancementsScreen) {
            if(ConfigManager.NO_ADVANCEMENTS.get()){
                Minecraft.getInstance().setScreen(null);
            }
        }
    }

    private static Optional<Button> findButton(List<? extends GuiEventListener> listeners, String name) {
        for (GuiEventListener listener : listeners) {
            if (listener instanceof Button button && button.getMessage() instanceof TranslatableComponent translatableComponent) {
                if (translatableComponent.getKey().equals(name))
                    return Optional.of(button);
            }
        }
        return Optional.empty();
    }
}
