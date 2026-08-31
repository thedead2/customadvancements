package de.thedead2.customadvancements.events;

import de.thedead2.customadvancements.client.ClientRenderer;
import de.thedead2.customadvancements.client.ClientTranslationManager;
import de.thedead2.customadvancements.util.ConfigManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static de.thedead2.customadvancements.util.ModHelper.LOGGER;


public class ClientEventListeners {

    public static void removeAdvancementsButtonIfNeeded(Screen screen, List<? extends GuiEventListener> listeners, Consumer<Button> buttonRemover) {
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
    }

    private static Optional<Button> findButton(List<? extends GuiEventListener> listeners, String name) {
        for (GuiEventListener listener : listeners) {
            if (listener instanceof Button button && button.getMessage() instanceof MutableComponent mutableComponent && mutableComponent.getContents() instanceof TranslatableContents translatableContents && translatableContents.getKey().equals(name)) {
                return Optional.of(button);
            }
        }

        return Optional.empty();
    }

    public static void preventAdvancementScreenOpeningIfNeeded(Screen screen) {
        if ((screen instanceof AdvancementsScreen) && ConfigManager.NO_ADVANCEMENTS.get()) {
            Minecraft.getInstance().setScreen(null);
        }
    }

    public static void onClientLogout() {
        LOGGER.info("Cleaning up client sync data...");
        ClientTranslationManager.clear();
        ClientRenderer.clear();
    }
}
