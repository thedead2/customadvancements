package de.thedead2.customadvancements.util.core;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import de.thedead2.customadvancements.advancements.AdvancementTabsSorter;
import de.thedead2.customadvancements.advancements.progression.AdvancementProgressionMode;
import net.minecraft.advancements.Advancement;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.*;
import java.util.stream.Collectors;


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
    public static final ForgeConfigSpec.ConfigValue<AdvancementTabsSorter> ADVANCEMENT_TAB_SORTING_MODE;
    public static final ForgeConfigSpec.ConfigValue<AdvancementProgressionMode> ADVANCEMENT_PROGRESSION_MODE;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> ADVANCEMENT_BLACKLIST;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> ADVANCEMENT_PROGRESSION_MODE_MOD_BLACKLIST;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> CONNECTED_ADVANCEMENTS;

    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> ADVANCEMENT_SORTING_LIST;

    static {
        CONFIG_BUILDER.push("Config for " + ModHelper.MOD_NAME);


        OUT_DATED_MESSAGE = CONFIG_BUILDER.comment("Whether the mod should send a chat message if an update is available").define("warnMessage", true);

        NO_ADVANCEMENTS = CONFIG_BUILDER.comment("Whether the mod should remove all advancements").define("noAdvancements", false);

        NO_RECIPE_ADVANCEMENTS = CONFIG_BUILDER.comment("Whether the mod should remove all recipe advancements").define("noRecipeAdvancements", false);

        ADVANCEMENT_BLACKLIST = CONFIG_BUILDER.comment("Blacklist of Advancements that should be removed by the mod").defineList("advancementsBlacklist", Collections.emptyList(), ConfigManager::isValidAdvancementId);

        BLACKLIST_IS_WHITELIST = CONFIG_BUILDER.comment("Whether the Blacklist of Advancements should be a Whitelist").define("blacklistIsWhitelist", false);

        ADVANCEMENT_PROGRESSION = CONFIG_BUILDER.comment("Changing this to true causes each advancement to only be achievable if it's parent has been achieved. Useful for progression systems!").define("advancementProgression", false);

        CONNECTED_ADVANCEMENTS = CONFIG_BUILDER.comment("A list of connected advancements in the format of parent -> child").defineList("connectedAdvancementsList", Lists.newArrayList("minecraft:story/follow_ender_eye -> minecraft:end/root", "minecraft:story"
                + "/form_obsidian -> minecraft:nether/root"), in -> in instanceof String);

        RESET_ADVANCEMENTS_ON_DEATH = CONFIG_BUILDER.comment("Whether all advancement progress should be reset when the player dies").define("resetAdvancementProgressOnDeath", false);

        ADVANCEMENT_PROGRESSION_MODE = CONFIG_BUILDER.comment("The Advancements that are affected by the progression system").defineEnum("advancementProgressionMode", AdvancementProgressionMode.ALL);

        ADVANCEMENT_PROGRESSION_MODE_MOD_BLACKLIST = CONFIG_BUILDER.comment("Blacklist of Mods that should not be affected by the advancement progression system").defineList("modBlacklist", Collections.emptyList(), ConfigManager::isValidModID);

        ADVANCEMENT_PROGRESSION_MODE_MOD_BLACKLIST_IS_WHITELIST = CONFIG_BUILDER.comment("Whether the Blacklist of Mods that should not be affected by the advancement progression system should be a Whitelist").define("modBlacklistIsWhitelist", false);

        ADVANCEMENT_TAB_SORTING_MODE = CONFIG_BUILDER.comment("In which order the advancement tabs in the advancement screen should be ordered").defineEnum("advancementTabSortingMode", AdvancementTabsSorter.UNSORTED);

        ADVANCEMENT_SORTING_LIST = CONFIG_BUILDER.comment("Order of the advancement tabs when DEFINED_LIST is selected").defineList("advancementSortingList", Collections.emptyList(), ConfigManager::isValidRootAdvancementID);

        DISABLE_STANDARD_ADVANCEMENT_LOAD = CONFIG_BUILDER.comment("Whether the mod should overwrite vanilla advancements with generated ones").define("disableStandardAdvancementLoad", false);

        CONFIG_BUILDER.pop();
        CONFIG_SPEC = CONFIG_BUILDER.build();
    }


    public static ImmutableSet<ResourceLocation> getBlacklistedResourceLocations() {
        Set<ResourceLocation> blacklistedResourceLocations = new HashSet<>();

        ADVANCEMENT_BLACKLIST.get().forEach(String -> blacklistedResourceLocations.add(ResourceLocation.tryCreate(String)));

        return ImmutableSet.copyOf(blacklistedResourceLocations);
    }

    public static ImmutableMap<ResourceLocation, ResourceLocation> getConnectedAdvancements() {
        Map<ResourceLocation, ResourceLocation> connectedAdvancements = new HashMap<>();
        CONNECTED_ADVANCEMENTS.get().forEach(s -> {
            int index = s.indexOf("->");
            String s1 = s.substring(0, index - 1);
            String s2 = s.substring(index + 3);
            connectedAdvancements.put(ResourceLocation.tryCreate(s2), ResourceLocation.tryCreate(s1));
        });
        return ImmutableMap.copyOf(connectedAdvancements);
    }

    public static ImmutableList<ResourceLocation> getSortedAdvancementList(){
        List<ResourceLocation> resourceLocations = new ArrayList<>();
        ADVANCEMENT_SORTING_LIST.get().forEach(s -> resourceLocations.add(ResourceLocation.tryCreate(s)));
        return ImmutableList.copyOf(resourceLocations);
    }

    /*private static boolean bothValidAdvancementIDs(Object in) {
        if(!(in instanceof String s)) return false;
        int index = s.indexOf("->");
        String s1 = s.substring(0, index - 1);
        String s2 = s.substring(index + 3);

        return isValidAdvancementId(s1) && isValidAdvancementId(s2);
    }*/

    private static boolean isValidAdvancementId(Object in) {
        if(!(in instanceof String)) return false;
        ResourceLocation resourceLocation = ResourceLocation.tryCreate((String) in);
        if(resourceLocation != null){
            if(!ModHelper.getServer().isPresent()) return true;
            else return ModHelper.getServer().get().getAdvancementManager().getAllAdvancements().stream().map(Advancement::getId).collect(Collectors.toList()).contains(resourceLocation);
        }
        return false;
    }

    private static boolean isValidRootAdvancementID(Object in){
        if(!(in instanceof String)) return false;
        ResourceLocation resourceLocation = ResourceLocation.tryCreate((String) in);
        if(resourceLocation != null){
            if(!ModHelper.getServer().isPresent()) return true;
            else return ModHelper.getServer().get().getAdvancementManager().getAllAdvancements().stream().filter(advancement -> advancement.getParent() == null).map(Advancement::getId).collect(Collectors.toList()).contains(resourceLocation);
        }
        return false;
    }

    private static boolean isValidModID(Object in) {
        if(!(in instanceof String)) return false;
        if(!ModHelper.getServer().isPresent()) return true;
        else return ModHelper.getServer().get().getAdvancementManager().getAllAdvancements().stream().map(advancement -> advancement.getId().getNamespace()).collect(Collectors.toList()).contains((String) in);
    }
}
