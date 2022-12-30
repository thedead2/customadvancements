package de.thedead2.customadvancements.util;

import de.thedead2.customadvancements.CustomAdvancement;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.VersionChecker;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.loading.moddiscovery.ModFile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;


public abstract class ModHelper {

    public static final String MOD_VERSION = "1.16.5-2.1.3";
    public static final String MOD_ID = "customadvancements";
    public static final String MOD_NAME = "Custom Advancements";
    public static final String MOD_UPDATE_LINK = "https://www.curseforge.com/minecraft/mc-mods/custom-advancements/files";

    public static final ModFile THIS_MOD_FILE = ModList.get().getModFileById(MOD_ID).getFile();
    public static final ModContainer THIS_MOD_CONTAINER = ModList.get().getModContainerById(MOD_ID).orElseThrow(RuntimeException::new);

    public static final String GAME_DIR = FMLPaths.GAMEDIR.get().toString();
    public static final String DIR_PATH = GAME_DIR + "/" + MOD_ID;
    public static final String TEXTURES_PATH = DIR_PATH + "/" + "textures";

    public static final FileHandler FILE_HANDLER = new FileHandler();
    public static final JsonHandler JSON_HANDLER = new JsonHandler();
    public static final TextureHandler TEXTURE_HANDLER = new TextureHandler();

    public static final Set<CustomAdvancement> CUSTOM_ADVANCEMENTS = new HashSet<>();
    public static final Map<ResourceLocation, NativeImage> TEXTURES = new HashMap<>();



    /** Inner Class VersionManager
     * handles every Update related action **/
    public abstract static class VersionManager {

        private static final Logger LOGGER = LogManager.getLogger();
        private static final VersionChecker.CheckResult RESULT = VersionChecker.getResult(THIS_MOD_CONTAINER.getModInfo());
        private static final String PREFIX = "[" + MOD_NAME + "]: ";


        public static void sendChatMessage(PlayerEntity player){
            if (RESULT.status.equals(VersionChecker.Status.OUTDATED)){
                player.sendMessage(new StringTextComponent(PREFIX + "Mod is outdated! Please update using the link below:"), Util.DUMMY_UUID);
                player.sendMessage(new StringTextComponent(MOD_UPDATE_LINK), Util.DUMMY_UUID);
            }
            else if (RESULT.status.equals(VersionChecker.Status.BETA)) {
                player.sendMessage(new StringTextComponent(PREFIX + "You're currently using a Beta Version of the mod! Please note that using this beta is at your own risk!"), Util.DUMMY_UUID);
            }
            else if (RESULT.status.equals(VersionChecker.Status.BETA_OUTDATED)) {
                player.sendMessage(new StringTextComponent(PREFIX + "You're currently using a Beta Version of the mod! Please note that using this beta is at your own risk!"), Util.DUMMY_UUID);
                player.sendMessage(new StringTextComponent(PREFIX + "This Beta Version is outdated! Please update using the link below:"), Util.DUMMY_UUID);
                player.sendMessage(new StringTextComponent(MOD_UPDATE_LINK), Util.DUMMY_UUID);
            }
        }

        public static void sendLoggerMessage(){
            if(RESULT.status.equals(VersionChecker.Status.UP_TO_DATE)){
                LOGGER.debug(PREFIX + "Retrieved Info that mod is up to date! Current Version: " + MOD_VERSION + " Latest Version: " + RESULT.target);
            }
            else if (RESULT.status.equals(VersionChecker.Status.OUTDATED)) {
                LOGGER.warn(PREFIX + "Mod is outdated! Current Version: " + MOD_VERSION + " Latest Version: " + RESULT.target);
                LOGGER.warn(PREFIX + "Please update " + MOD_NAME + " using this link: " + MOD_UPDATE_LINK);
            }
            else if (RESULT.status.equals(VersionChecker.Status.FAILED)) {
                LOGGER.error(PREFIX + "Failed to check for updates! Please check your internet connection!");
            }
            else if (RESULT.status.equals(VersionChecker.Status.BETA)) {
                LOGGER.warn(PREFIX + "You're currently using a Beta of " + MOD_NAME + "! Please note that using this beta is at your own risk!");
                LOGGER.info(PREFIX + "Beta Status: " + RESULT.status);
            }
            else if (RESULT.status.equals(VersionChecker.Status.BETA_OUTDATED)) {
                LOGGER.warn(PREFIX + "You're currently using a Beta of " + MOD_NAME + "! Please note that using this beta is at your own risk!");
                LOGGER.warn(PREFIX + "This Beta is outdated! Please update " + MOD_NAME + " using this link: " + MOD_UPDATE_LINK);
                LOGGER.warn(PREFIX + "Beta Status: " + RESULT.status);
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
        private static final ForgeConfigSpec.ConfigValue<List<? extends String>> ADVANCEMENT_BLACKLIST;


        static {
            CONFIG_BUILDER.push("Config for " + MOD_NAME);


            OUT_DATED_MESSAGE = CONFIG_BUILDER.comment("Whether the mod should send a chat message if an update is available:").define("warnMessage", true);

            OPTIFINE_SHADER_COMPATIBILITY = CONFIG_BUILDER.comment("Whether the compatibility mode for Optifine Shaders should be enabled. Note: This disables custom background textures for advancements! (You need to restart your game for the actions to take effect)").worldRestart().define("optifineShaderCompatibility", false);

            NO_ADVANCEMENTS = CONFIG_BUILDER.comment("Whether the mod should remove all advancements (You need to restart your game for the actions to take effect):").worldRestart().define("noAdvancements", false);

            NO_RECIPE_ADVANCEMENTS = CONFIG_BUILDER.comment("Whether the mod should remove all recipe advancements:").define("noRecipeAdvancements", false);

            ADVANCEMENT_BLACKLIST = CONFIG_BUILDER.comment("Blacklist of Advancements that should be removed by the mod:").worldRestart().defineList("advancementsBlacklist" , Collections.emptyList(), it -> it instanceof String);

            BLACKLIST_IS_WHITELIST = CONFIG_BUILDER.comment("Whether the Blacklist of Advancements should be a Whitelist:").define("blacklistIsWhitelist", false);


            CONFIG_BUILDER.pop();
            CONFIG_SPEC = CONFIG_BUILDER.build();
        }


        public static Set<ResourceLocation> getBlacklistedResourceLocations(){
            List<String> list = (List<String>) ADVANCEMENT_BLACKLIST.get();
            Set<ResourceLocation> resourceLocations = new HashSet<>();

            if(!list.isEmpty()){
                list.forEach(inputString -> resourceLocations.add(ResourceLocation.tryCreate(inputString)));
            }

            return resourceLocations;
        }
    }
}
