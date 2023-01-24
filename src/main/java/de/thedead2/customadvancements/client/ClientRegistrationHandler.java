package de.thedead2.customadvancements.client;

import com.mojang.blaze3d.platform.InputConstants;
import de.thedead2.customadvancements.client.generator.AdvancementGenerator;
import de.thedead2.customadvancements.client.generator.gui.AdvancementGeneratorGUI;
import de.thedead2.customadvancements.commands.ModGameRules;
import de.thedead2.customadvancements.util.ModHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

import static de.thedead2.customadvancements.util.ModHelper.LOGGER;

@Mod.EventBusSubscriber(modid = ModHelper.MOD_ID, value = Dist.CLIENT)
public class ClientRegistrationHandler {

    public static final Lazy<KeyMapping> ADVANCEMENT_GENERATOR_KEY = Lazy.of(() -> new KeyMapping(
            "key.customadvancements.advancement_generator_key",
            KeyConflictContext.IN_GAME,
            KeyModifier.SHIFT,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_G,
            "key.categories.customadvancements.key_category"));


    @SubscribeEvent
    public static void onKeyPressed(final TickEvent.ClientTickEvent event){
        if (event.phase == TickEvent.Phase.END) {
            while (ADVANCEMENT_GENERATOR_KEY.get().consumeClick()) {
                LOGGER.info("Testing!");
                Minecraft minecraft = Minecraft.getInstance();
                if(minecraft.player != null) {
                    LOGGER.info("Testing2!");
                    boolean test = minecraft.level.getGameRules().getBoolean(ModGameRules.ADVANCEMENT_GENERATOR_GAMERULE);
                    LOGGER.info("Value of Gamerule: " + test);
                    if(test) { //rule always returns false
                        LOGGER.info("Testing3!");
                        Screen parent = minecraft.screen;
                        minecraft.setScreen(new AdvancementGeneratorGUI(minecraft.player.connection.getAdvancements(), parent, new AdvancementGenerator(minecraft.player.connection.getLevel().getServer())));
                    }
                    else {
                        LOGGER.info("Testing4!");
                    }
                }
            }
        }
    }

    @Mod.EventBusSubscriber(modid = ModHelper.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ClientModBusEvents {
        @SubscribeEvent
        public static void onKeyMappingRegistration(final RegisterKeyMappingsEvent event){
            event.register(ADVANCEMENT_GENERATOR_KEY.get());
        }
    }
}
