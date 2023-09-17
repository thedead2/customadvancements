package de.thedead2.customadvancements.util.handler;

import de.thedead2.customadvancements.util.core.ConfigManager;
import de.thedead2.customadvancements.util.core.ModHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.advancements.AdvancementsScreen;
import net.minecraft.client.gui.screen.IngameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.Optional;

@Mod.EventBusSubscriber(modid = ModHelper.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ScreenHandler {
    @SubscribeEvent
    public static void afterScreenInit(GuiScreenEvent.InitGuiEvent.Post event) {
        Screen screen = event.getGui();
        if(screen instanceof IngameMenuScreen){
            if(!screen.isPauseScreen()) return;
            if (ConfigManager.NO_ADVANCEMENTS.get()) {
                findButton(event.getWidgetList(), "gui.advancements").ifPresent(event::removeWidget);
                findButton(event.getWidgetList(), "gui.stats").ifPresent(button1 -> {
                    button1.setWidth(204);
                    button1.x = button1.x - (204 / 2 + 4);
                });
            }
        }
    }

    @SubscribeEvent
    public static void beforeScreenInit(GuiScreenEvent.InitGuiEvent.Pre event){
        if (event.getGui() instanceof AdvancementsScreen) {
            if(ConfigManager.NO_ADVANCEMENTS.get()){
                Minecraft.getInstance().displayGuiScreen(null);
            }
        }
    }

    private static Optional<Button> findButton(List<Widget> widgets, String name) {
        for (Widget widget : widgets) {
            if (widget instanceof Button && widget.getMessage() instanceof TranslationTextComponent) {
                if (((TranslationTextComponent) widget.getMessage()).getKey().equals(name))
                    return Optional.of((Button) widget);
            }
        }
        return Optional.empty();
    }
}

