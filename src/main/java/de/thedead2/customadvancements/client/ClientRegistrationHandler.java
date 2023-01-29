package de.thedead2.customadvancements.client;

import com.mojang.blaze3d.platform.InputConstants;
import de.thedead2.customadvancements.client.gui.AdvancementGeneratorGUI;
import de.thedead2.customadvancements.commands.ModGameRules;
import de.thedead2.customadvancements.util.ModHelper;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.FrameType;
import net.minecraft.advancements.TreeNodePosition;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientAdvancements;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static de.thedead2.customadvancements.util.ModHelper.LOGGER;
import static de.thedead2.customadvancements.util.ModHelper.MOD_ID;

@Mod.EventBusSubscriber(modid = ModHelper.MOD_ID, value = Dist.CLIENT)
public class ClientRegistrationHandler {

    private static final Minecraft MINECRAFT = Minecraft.getInstance();
    public static final Map<ResourceLocation, Advancement.Builder> TEMP = new HashMap<>();
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
                TEMP.clear();
                LOGGER.info("Testing!");
                if(MINECRAFT.player != null) {
                    LOGGER.info("Testing2!");
                    boolean test = MINECRAFT.level.getGameRules().getBoolean(ModGameRules.ADVANCEMENT_GENERATOR_GAMERULE);
                    LOGGER.info("Value of Gamerule: " + test);
                    if(!test) { //rule always returns false
                        LOGGER.info("Testing3!");
                        MINECRAFT.setScreen(new AdvancementGeneratorGUI(getFakeAdvancements(), MINECRAFT.screen, MINECRAFT));
                        }
                    else {
                        LOGGER.info("Testing4!");
                    }
                }
            }
        }
    }

    private static ClientAdvancements getFakeAdvancements(){
        Map<ResourceLocation, Advancement.Builder> temp = new HashMap<>();
        Objects.requireNonNull(MINECRAFT.getSingleplayerServer()).getAdvancements().getAllAdvancements().forEach((advancement -> {
            ResourceLocation id = advancement.getId();
            Advancement.Builder builder1 = advancement.deconstruct();
            temp.put(id, builder1);

        }));

        Advancement.Builder tempAdvancement$Builder = Advancement.Builder.advancement().display(Items.DIAMOND, Component.translatable("display.customadvancements.fake_advancement_title"), Component.translatable("display.customadvancements.fake_advancement_description"), new ResourceLocation("textures/gui/advancements/backgrounds/stone.png"), FrameType.TASK, false, false, false).addCriterion("fake_root_advancement", InventoryChangeTrigger.TriggerInstance.hasItems(Blocks.CRAFTING_TABLE));
        ResourceLocation tempResourceLocation = new ResourceLocation(MOD_ID, "fake_root_advancement");
        temp.put(tempResourceLocation, tempAdvancement$Builder);

        ClientAdvancements clientAdvancements = new ClientAdvancements(MINECRAFT);
        clientAdvancements.getAdvancements().add(temp);

        for(Advancement advancement : clientAdvancements.getAdvancements().getRoots()) {
            if (advancement.getDisplay() != null) {
                TreeNodePosition.run(advancement);
            }
        }

        return clientAdvancements;
    }


    @Mod.EventBusSubscriber(modid = ModHelper.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ClientModBusEvents {
        @SubscribeEvent
        public static void onKeyMappingRegistration(final RegisterKeyMappingsEvent event){
            event.register(ADVANCEMENT_GENERATOR_KEY.get());
        }
    }
}
