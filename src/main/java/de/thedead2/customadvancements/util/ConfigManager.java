package de.thedead2.customadvancements.util;

import de.thedead2.customadvancements.advancements.progression.AdvancementProgressionMode;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.*;

public abstract class ConfigManager {

    private static final ForgeConfigSpec.Builder CONFIG_BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec CONFIG_SPEC;

    /**
     * All Config fields for Custom Advancements
     **/
    public static final ForgeConfigSpec.ConfigValue<Boolean> OUT_DATED_MESSAGE;
    public static final ForgeConfigSpec.ConfigValue<Boolean> NO_RECIPE_ADVANCEMENTS;
    public static final ForgeConfigSpec.ConfigValue<Boolean> NO_ADVANCEMENTS;
    public static final ForgeConfigSpec.ConfigValue<Boolean> BLACKLIST_IS_WHITELIST;
    public static final ForgeConfigSpec.ConfigValue<Boolean> DISABLE_STANDARD_ADVANCEMENT_LOAD;
    public static final ForgeConfigSpec.ConfigValue<Boolean> ADVANCEMENT_PROGRESSION;
    public static final ForgeConfigSpec.ConfigValue<Boolean> RESET_ADVANCEMENTS_ON_DEATH;
    public static final ForgeConfigSpec.ConfigValue<Boolean> ADVANCEMENT_PROGRESSION_MODE_MOD_BLACKLIST_IS_WHITELIST;
    public static final ForgeConfigSpec.ConfigValue<AdvancementProgressionMode> ADVANCEMENT_PROGRESSION_MODE;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> ADVANCEMENT_BLACKLIST;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> ADVANCEMENT_PROGRESSION_MODE_MOD_BLACKLIST;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> CONNECTED_ADVANCEMENTS;

    static {
        CONFIG_BUILDER.push("Config for " + ModHelper.MOD_NAME);


        OUT_DATED_MESSAGE = CONFIG_BUILDER.comment("Whether the mod should send a chat message if an update is available").define("warnMessage", true);

        NO_ADVANCEMENTS = CONFIG_BUILDER.comment("Whether the mod should remove all advancements").define("noAdvancements", false);

        NO_RECIPE_ADVANCEMENTS = CONFIG_BUILDER.comment("Whether the mod should remove all recipe advancements").define("noRecipeAdvancements", false);

        ADVANCEMENT_BLACKLIST = CONFIG_BUILDER.comment("Blacklist of Advancements that should be removed by the mod").defineList("advancementsBlacklist", Collections.emptyList(), ConfigManager::isValidResourceLocation);

        BLACKLIST_IS_WHITELIST = CONFIG_BUILDER.comment("Whether the Blacklist of Advancements should be a Whitelist").define("blacklistIsWhitelist", false);

        ADVANCEMENT_PROGRESSION = CONFIG_BUILDER.comment("Changing this to true causes each advancement to only be achievable if it's parent has been achieved. Useful for progression systems!").define("advancementProgression", false);

        CONNECTED_ADVANCEMENTS = CONFIG_BUILDER.comment("A list of connected advancements in the format of parent, child").defineList("connectedAdvancementsList", List.of("minecraft:story/follow_ender_eye -> minecraft:end/root", "minecraft:story/form_obsidian -> minecraft:nether/root"), ConfigManager::bothValidResourceLocations);

        RESET_ADVANCEMENTS_ON_DEATH = CONFIG_BUILDER.comment("Whether all advancement progress should be reset when the player dies").define("resetAdvancementProgressOnDeath", false);

        ADVANCEMENT_PROGRESSION_MODE = CONFIG_BUILDER.comment("The Advancements that are affected by the progression system").defineEnum("advancementProgressionMode", AdvancementProgressionMode.ALL);

        ADVANCEMENT_PROGRESSION_MODE_MOD_BLACKLIST = CONFIG_BUILDER.comment("Blacklist of Mods that should not be affected by the advancement progression system").defineList("modBlacklist", Collections.emptyList(), ConfigManager::isValidModID);

        ADVANCEMENT_PROGRESSION_MODE_MOD_BLACKLIST_IS_WHITELIST = CONFIG_BUILDER.comment("Whether the Blacklist of Mods that should not be affected by the advancement progression system should be a Whitelist").define("modBlacklistIsWhitelist", false);

        DISABLE_STANDARD_ADVANCEMENT_LOAD = CONFIG_BUILDER.comment("Whether the mod should overwrite vanilla advancements with generated ones").define("disableStandardAdvancementLoad", false);

        CONFIG_BUILDER.pop();
        CONFIG_SPEC = CONFIG_BUILDER.build();
    }


    public static Set<ResourceLocation> getBlacklistedResourceLocations() {
        Set<ResourceLocation> blacklistedResourceLocations = new HashSet<>();

        ADVANCEMENT_BLACKLIST.get().forEach(String -> blacklistedResourceLocations.add(ResourceLocation.tryParse(String)));

        return blacklistedResourceLocations;
    }

    public static Map<ResourceLocation, ResourceLocation> getConnectedAdvancements() {
        Map<ResourceLocation, ResourceLocation> connectedAdvancements = new HashMap<>();
        CONNECTED_ADVANCEMENTS.get().forEach(s -> {
            int index = s.indexOf("->");
            String s1 = s.substring(0, index - 1);
            String s2 = s.substring(index + 3);
            connectedAdvancements.put(ResourceLocation.tryParse(s2), ResourceLocation.tryParse(s1));
        });
        return connectedAdvancements;
    }

    private static boolean bothValidResourceLocations(Object in) {
        /*if(!(in instanceof String s)) return false;
        int index = s.indexOf("->");
        String s1 = s.substring(0, index - 1);
        String s2 = s.substring(index + 3);

        return isValidResourceLocation(s1) && isValidResourceLocation(s2);*/
        return true;
    }

    private static boolean isValidResourceLocation(Object in) {
        /*if(!(in instanceof String s)) return false;
        ResourceLocation resourceLocation = ResourceLocation.tryParse(s);
        if(resourceLocation != null){
            return ALL_ADVANCEMENTS_RESOURCE_LOCATIONS.isEmpty() || ALL_ADVANCEMENTS_RESOURCE_LOCATIONS.contains(resourceLocation);
        }
        return false;*/
        return true;
    }

    private static boolean isValidModID(Object in) {
        /*if(!(in instanceof String s)) return false;
        return ALL_ADVANCEMENTS_RESOURCE_LOCATIONS.isEmpty() || !ALL_ADVANCEMENTS_RESOURCE_LOCATIONS.stream().filter((resourceLocation) -> resourceLocation.getNamespace().equals(s)).toList().isEmpty();
    */
        return true;
    }
}
