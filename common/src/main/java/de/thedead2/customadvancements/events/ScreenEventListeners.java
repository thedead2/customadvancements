package de.thedead2.customadvancements.events;

import de.thedead2.customadvancements.util.core.ConfigManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

@OnlyIn(Dist.CLIENT)
public class ScreenEventListeners {

    public static void afterScreenInit(Screen screen, List<? extends GuiEventListener> listeners, Consumer<Button> buttonRemover) {
        if (screen instanceof PauseScreen pauseScreen) {
            if (!pauseScreen.showsPauseMenu()) {
                return;
            }

            if (ConfigManager.NO_ADVANCEMENTS.get()) {
                findButton(listeners, "gui.advancements").ifPresent((button) -> {
                    buttonRemover.accept(button);

                    findButton(listeners, "gui.stats").ifPresent(button1 -> {
                        button1.setWidth(204);
                        button1.setX(button1.getX() - (204 / 2 + 4));
                    });
                });
            }
        }
        /*else if (BA_COMPATIBILITY.get() && screen instanceof BetterAdvancementsScreen betterAdvancementsScreen) {
            try {
                var clazz = betterAdvancementsScreen.getClass();
                Field tabs = clazz.getDeclaredField("tabs");

                tabs.setAccessible(true);

                Object obj = tabs.get(betterAdvancementsScreen);

                ConfigManager.ADVANCEMENT_TAB_SORTING_MODE.get().sortAdvancementTabs((Map<AdvancementHolder, Object>) obj);
            }
            catch (NoSuchFieldException | IllegalAccessException e) {
                CrashHandler.getInstance().handleException("Failed to sort advancement tabs of BetterAdvancementsScreen!", "AdvancementTabsSorter", e, Level.ERROR);
            }
        }*/
    }

    private static Optional<Button> findButton(List<? extends GuiEventListener> listeners, String name) {
        for (GuiEventListener listener : listeners) {
            if (listener instanceof Button button && button.getMessage() instanceof MutableComponent mutableComponent && mutableComponent.getContents() instanceof TranslatableContents translatableContents && translatableContents.getKey().equals(name)) {
                return Optional.of(button);
            }
        }

        return Optional.empty();
    }

    public static void beforeScreenInit(Screen screen) {
        if ((screen instanceof AdvancementsScreen /*|| (BA_COMPATIBILITY.get() && screen instanceof BetterAdvancementsScreen)*/) && ConfigManager.NO_ADVANCEMENTS.get()) {
            Minecraft.getInstance().setScreen(null);
        }
    }
}
