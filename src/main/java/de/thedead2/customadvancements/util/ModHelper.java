package de.thedead2.customadvancements.util;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.gson.JsonElement;
import com.mojang.blaze3d.platform.NativeImage;
import de.thedead2.customadvancements.advancements.advancementtypes.CustomAdvancement;
import de.thedead2.customadvancements.advancements.advancementtypes.GameAdvancement;
import de.thedead2.customadvancements.util.handler.FileHandler;
import de.thedead2.customadvancements.util.handler.JsonHandler;
import de.thedead2.customadvancements.util.handler.TextureHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.WorldData;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.VersionChecker;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.forgespi.locating.IModFile;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.nio.file.Path;
import java.util.*;

import static de.thedead2.customadvancements.advancements.CustomAdvancementManager.ADVANCEMENTS;


public abstract class ModHelper {

    public static final String MOD_VERSION = "1.19.2-5.3.3";
    public static final String MOD_ID = "customadvancements";
    public static final String MOD_NAME = "Custom Advancements";
    public static final String MOD_UPDATE_LINK = "https://www.curseforge.com/minecraft/mc-mods/custom-advancements/files";

    public static final IModFile THIS_MOD_FILE = ModList.get().getModFileById(MOD_ID).getFile();
    public static final ModContainer THIS_MOD_CONTAINER = ModList.get().getModContainerById(MOD_ID).orElseThrow(() -> new RuntimeException("Unable to retrieve ModContainer for id: " + MOD_ID));

    public static final Path GAME_DIR = FMLPaths.GAMEDIR.get();
    public static final char PATH_SEPARATOR = File.separatorChar;
    public static final Path DIR_PATH = GAME_DIR.resolve(MOD_ID);
    public static final Path CUSTOM_ADVANCEMENTS_PATH = DIR_PATH.resolve(MOD_ID);
    public static final Path TEXTURES_PATH = DIR_PATH.resolve("textures");

    public static final Map<ResourceLocation, CustomAdvancement> CUSTOM_ADVANCEMENTS = new HashMap<>();
    public static final Map<ResourceLocation, GameAdvancement> GAME_ADVANCEMENTS = new HashMap<>();
    public static final Map<ResourceLocation, NativeImage> TEXTURES = new HashMap<>();
    public static final Set<ResourceLocation> REMOVED_ADVANCEMENTS_SET = new HashSet<>();
    public static final Map<ResourceLocation, JsonElement> ALL_DETECTED_GAME_ADVANCEMENTS = new HashMap<>();

    public static final Multimap<ResourceLocation, ResourceLocation> PARENT_CHILDREN_MAP = ArrayListMultimap.create();
    public static final Map<ResourceLocation, ResourceLocation> CHILDREN_PARENT_MAP = new HashMap<>();
    public static final Collection<ResourceLocation> ALL_ADVANCEMENTS_RESOURCE_LOCATIONS = new HashSet<>();

    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);


    public static void reloadAll(MinecraftServer server){
        StopWatch timer = new StopWatch();
        timer.start();
        LOGGER.info("Reloading...");

        init();
        reloadGameData(server);

        LOGGER.info("Reload completed in {} ms!", timer.getTime());
        timer.stop();
        timer.reset();
    }


    public static void init(){
        clearAll();
        FileHandler.checkForMainDirectories();

        if(TextureHandler.getInstance() != null){
            TextureHandler.getInstance().start();
        }
        else {
            new TextureHandler(TEXTURES_PATH.toFile());
        }
        if (JsonHandler.getInstance() != null){
            JsonHandler.getInstance().start();
        }
        else {
            new JsonHandler(DIR_PATH.toFile());
        }

        LOGGER.info("Loaded " + TEXTURES.size() + (TEXTURES.size() != 1 ? " Textures!" : " Texture!"));
        LOGGER.info("Loaded " + CUSTOM_ADVANCEMENTS.size() + (CUSTOM_ADVANCEMENTS.size() != 1 ? " Custom Advancements!" : " Custom Advancement!"));
        LOGGER.info("Loaded " + GAME_ADVANCEMENTS.size() + (GAME_ADVANCEMENTS.size() != 1 ? " Game Advancements!" : " Game Advancement!"));
    }


    private static void clearAll(){
        CUSTOM_ADVANCEMENTS.clear();
        GAME_ADVANCEMENTS.clear();
        TEXTURES.clear();
        REMOVED_ADVANCEMENTS_SET.clear();
        ALL_DETECTED_GAME_ADVANCEMENTS.clear();
        PARENT_CHILDREN_MAP.clear();
        CHILDREN_PARENT_MAP.clear();
        ALL_ADVANCEMENTS_RESOURCE_LOCATIONS.clear();
        ADVANCEMENTS.clear();
    }


    private static void reloadGameData(MinecraftServer server){
        PackRepository packRepository = server.getPackRepository();
        WorldData worldData = server.getWorldData();

        packRepository.reload();
        Collection<String> selectedIds = Lists.newArrayList(packRepository.getSelectedIds());
        Collection<String> disabledPacks = worldData.getDataPackConfig().getDisabled();

        for(String ids : packRepository.getAvailableIds()) {
            if (!disabledPacks.contains(ids) && !selectedIds.contains(ids)) {
                selectedIds.add(ids);
            }
        }

        server.reloadResources(selectedIds).exceptionally((e) -> {
            LOGGER.error("Failed to execute reload!", e);
            server.sendSystemMessage(Component.translatable("chat.customadvancements.reload_failed_massage"));
            e.printStackTrace();
            return null;
        });
    }


    /** Inner Class VersionManager
     * handles every Update related action **/
    public abstract static class VersionManager {

        private static final VersionChecker.CheckResult RESULT = VersionChecker.getResult(THIS_MOD_CONTAINER.getModInfo());
        private static final String PREFIX = "[" + MOD_NAME + "]: ";


        public static void sendChatMessage(Player player){
            if (RESULT.status().equals(VersionChecker.Status.OUTDATED)){
                player.sendSystemMessage(Component.literal("§c" + PREFIX + "Mod is outdated! Please update using the link below:"));
                player.sendSystemMessage(Component.literal("§c" + MOD_UPDATE_LINK));
            }
            else if (RESULT.status().equals(VersionChecker.Status.BETA)) {
                player.sendSystemMessage(Component.literal("§6" + PREFIX + "You're currently using a Beta Version of the mod! Please note that using this beta is at your own risk!"));
            }
            else if (RESULT.status().equals(VersionChecker.Status.BETA_OUTDATED)) {
                player.sendSystemMessage(Component.literal("§6" + PREFIX + "You're currently using a Beta Version of the mod! Please note that using this beta is at your own risk!"));
                player.sendSystemMessage(Component.literal("§c" + PREFIX + "This Beta Version is outdated! Please update using the link below:"));
                player.sendSystemMessage(Component.literal("§c" + MOD_UPDATE_LINK));
            }
        }

        public static void sendLoggerMessage(){
            if (RESULT.status().equals(VersionChecker.Status.OUTDATED)) {
                LOGGER.warn("Mod is outdated! Current Version: " + MOD_VERSION + " Latest Version: " + RESULT.target());
                LOGGER.warn("Please update " + MOD_NAME + " using this link: " + MOD_UPDATE_LINK);
            }
            else if (RESULT.status().equals(VersionChecker.Status.FAILED)) {
                LOGGER.error("Failed to check for updates! Please check your internet connection!");
            }
            else if (RESULT.status().equals(VersionChecker.Status.BETA)) {
                LOGGER.warn("You're currently using a Beta of " + MOD_NAME + "! Please note that using this beta is at your own risk!");
                LOGGER.info("Beta Status: " + RESULT.status());
            }
            else if (RESULT.status().equals(VersionChecker.Status.BETA_OUTDATED)) {
                LOGGER.warn("You're currently using a Beta of " + MOD_NAME + "! Please note that using this beta is at your own risk!");
                LOGGER.warn("This Beta is outdated! Please update " + MOD_NAME + " using this link: " + MOD_UPDATE_LINK);
                LOGGER.warn("Beta Status: " + RESULT.status());
            }
        }
    }


    /** Inner Class ConfigManager
     * handles every Config related action **/
    public abstract static class ConfigManager {

        private static final ForgeConfigSpec.Builder CONFIG_BUILDER = new ForgeConfigSpec.Builder();
        public static final ForgeConfigSpec CONFIG_SPEC;

        /** All Config fields for Custom Advancements **/
        public static final ForgeConfigSpec.ConfigValue<Boolean> OUT_DATED_MESSAGE;
        public static final ForgeConfigSpec.ConfigValue<Boolean> OPTIFINE_SHADER_COMPATIBILITY;
        public static final ForgeConfigSpec.ConfigValue<Boolean> NO_RECIPE_ADVANCEMENTS;
        public static final ForgeConfigSpec.ConfigValue<Boolean> NO_ADVANCEMENTS;
        public static final ForgeConfigSpec.ConfigValue<Boolean> BLACKLIST_IS_WHITELIST;
        public static final ForgeConfigSpec.ConfigValue<Boolean> DISABLE_STANDARD_ADVANCEMENT_LOAD;
        private static final ForgeConfigSpec.ConfigValue<List<? extends String>> ADVANCEMENT_BLACKLIST;


        static {
            CONFIG_BUILDER.push("Config for " + MOD_NAME);


            OUT_DATED_MESSAGE = CONFIG_BUILDER.comment("Whether the mod should send a chat message if an update is available").define("warnMessage", true);

            OPTIFINE_SHADER_COMPATIBILITY = CONFIG_BUILDER.comment("Whether the compatibility mode for Optifine Shaders should be enabled. Note: This disables custom background textures for advancements! (You need to restart your game for the actions to take effect)").worldRestart().define("optifineShaderCompatibility", false);

            NO_ADVANCEMENTS = CONFIG_BUILDER.comment("Whether the mod should remove all advancements").worldRestart().define("noAdvancements", false);

            NO_RECIPE_ADVANCEMENTS = CONFIG_BUILDER.comment("Whether the mod should remove all recipe advancements").define("noRecipeAdvancements", false);

            ADVANCEMENT_BLACKLIST = CONFIG_BUILDER.comment("Blacklist of Advancements that should be removed by the mod").worldRestart().defineList("advancementsBlacklist" , Collections.emptyList(), it -> it instanceof String);

            BLACKLIST_IS_WHITELIST = CONFIG_BUILDER.comment("Whether the Blacklist of Advancements should be a Whitelist").define("blacklistIsWhitelist", false);

            DISABLE_STANDARD_ADVANCEMENT_LOAD = CONFIG_BUILDER.comment("Whether the mod should overwrite vanilla advancements with generated ones").define("disableStandardAdvancementLoad", false);

            CONFIG_BUILDER.pop();
            CONFIG_SPEC = CONFIG_BUILDER.build();
        }


        public static Set<ResourceLocation> getBlacklistedResourceLocations(){
            List<String> list;
            if(!ADVANCEMENT_BLACKLIST.get().isEmpty() && ADVANCEMENT_BLACKLIST.get().get(0) != null){
                list = Collections.unmodifiableList(ADVANCEMENT_BLACKLIST.get());
            }
            else {
                list = new ArrayList<>();
            }

            Set<ResourceLocation> blacklistedResourceLocations = new HashSet<>();

            if(!list.isEmpty()){
                list.forEach(String -> blacklistedResourceLocations.add(ResourceLocation.tryParse(String)));
            }

            return blacklistedResourceLocations;
        }
    }
}
