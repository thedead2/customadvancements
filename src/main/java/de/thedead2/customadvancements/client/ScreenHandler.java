package de.thedead2.customadvancements.client;

import betteradvancements.common.gui.BetterAdvancementsScreen;
import de.thedead2.customadvancements.client.screens.InformationScreen;
import de.thedead2.customadvancements.util.core.ConfigManager;
import de.thedead2.customadvancements.util.core.CrashHandler;
import de.thedead2.customadvancements.util.core.ModHelper;
import net.minecraft.advancements.Advancement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.Level;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static de.thedead2.customadvancements.util.core.ModHelper.BA_COMPATIBILITY;
import static de.thedead2.customadvancements.util.core.ModHelper.WARNINGS;


@Mod.EventBusSubscriber(modid = ModHelper.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ScreenHandler {

    @SubscribeEvent
    @SuppressWarnings("unchecked")
    public static void afterScreenInit(ScreenEvent.Init.Post event) {
        Screen screen = event.getScreen();

        if (screen instanceof PauseScreen pauseScreen) {
            if (!pauseScreen.showPauseMenu) {
                return;
            }

            if (ConfigManager.NO_ADVANCEMENTS.get()) {
                findButton(event.getListenersList(), "gui.advancements").ifPresent((button) -> {
                    event.removeListener(button);

                    findButton(event.getListenersList(), "gui.stats").ifPresent(button1 -> {
                        button1.setWidth(204);
                        button1.setX(button1.getX() - (204 / 2 + 4));
                    });
                });
            }
        }
        else if (BA_COMPATIBILITY.get() && screen instanceof BetterAdvancementsScreen betterAdvancementsScreen) {
            try {
                var clazz = betterAdvancementsScreen.getClass();
                Field tabs = clazz.getDeclaredField("tabs");

                tabs.setAccessible(true);

                Object obj = tabs.get(betterAdvancementsScreen);

                ConfigManager.ADVANCEMENT_TAB_SORTING_MODE.get().sortAdvancementTabs((Map<Advancement, Object>) obj);
            }
            catch (NoSuchFieldException | IllegalAccessException e) {
                CrashHandler.getInstance().handleException("Failed to sort advancement tabs of BetterAdvancementsScreen!", "AdvancementTabsSorter", e, Level.ERROR);
            }
        }
    }


    private static Optional<Button> findButton(List<? extends GuiEventListener> listeners, String name) {
        for (GuiEventListener listener : listeners) {
            if (listener instanceof Button button && button.getMessage() instanceof MutableComponent mutableComponent && mutableComponent.getContents() instanceof TranslatableContents translatableContents && translatableContents.getKey().equals(name)) {
                return Optional.of(button);
            }
        }

        return Optional.empty();
    }


    @SubscribeEvent
    public static void beforeScreenInit(ScreenEvent.Init.Pre event) {
        Screen screen = event.getScreen();

        if ((screen instanceof AdvancementsScreen || (BA_COMPATIBILITY.get() && screen instanceof BetterAdvancementsScreen)) && ConfigManager.NO_ADVANCEMENTS.get()) {
            Minecraft.getInstance().setScreen(null);
        }
        else if (screen instanceof TitleScreen && !WARNINGS.isEmpty()) {
            StringBuilder warnMessageBuilder = new StringBuilder("Custom Advancements\n");

            warnMessageBuilder.append("==============================================\n\n\n\n");

            while (!WARNINGS.isEmpty()) {
                warnMessageBuilder.append(WARNINGS.poll()).append("\n\n\n");
            }

            Minecraft.getInstance().setScreen(new InformationScreen(screen, warnMessageBuilder.toString()));
        }
    }
}

