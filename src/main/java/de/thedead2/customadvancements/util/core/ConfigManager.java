package de.thedead2.customadvancements.util.core;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import de.thedead2.customadvancements.advancements.AdvancementTabsSorter;
import de.thedead2.customadvancements.advancements.progression.AdvancementProgressionMode;
import net.minecraft.advancements.Advancement;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.*;
import java.util.function.Predicate;

public abstract class ConfigManager {

    private static final ForgeConfigSpec.Builder CONFIG_BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec CONFIG_SPEC;

    /**
     * All Config fields for Custom Advancements
     **/
    public static final ForgeConfigSpec.BooleanValue OUT_DATED_MESSAGE;
    public static final ForgeConfigSpec.BooleanValue NO_RECIPE_ADVANCEMENTS;
    public static final ForgeConfigSpec.BooleanValue NO_ADVANCEMENTS;
    public static final ForgeConfigSpec.BooleanValue BLACKLIST_IS_WHITELIST;
    public static final ForgeConfigSpec.BooleanValue DISABLE_STANDARD_ADVANCEMENT_LOAD;
    public static final ForgeConfigSpec.BooleanValue ADVANCEMENT_PROGRESSION;
    public static final ForgeConfigSpec.BooleanValue RESET_ADVANCEMENTS_ON_DEATH;
    public static final ForgeConfigSpec.BooleanValue ADVANCEMENT_PROGRESSION_MODE_MOD_BLACKLIST_IS_WHITELIST;
    public static final ForgeConfigSpec.EnumValue<AdvancementTabsSorter> ADVANCEMENT_TAB_SORTING_MODE;
    public static final ForgeConfigSpec.EnumValue<AdvancementProgressionMode> ADVANCEMENT_PROGRESSION_MODE;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> ADVANCEMENT_BLACKLIST;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> ADVANCEMENT_PROGRESSION_MODE_MOD_BLACKLIST;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> CONNECTED_ADVANCEMENTS;

    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> ADVANCEMENT_SORTING_LIST;

    static {
        CONFIG_BUILDER.push("Config for " + ModHelper.MOD_NAME);


        OUT_DATED_MESSAGE = newBoolVal("Whether the mod should send a chat message if an update is available","warnMessage", true);

        NO_ADVANCEMENTS = newBoolVal("Whether the mod should remove all advancements","noAdvancements", false);

        NO_RECIPE_ADVANCEMENTS = newBoolVal("Whether the mod should remove all recipe advancements","noRecipeAdvancements", false);

        ADVANCEMENT_BLACKLIST = newListVal("Blacklist of Advancements that should be removed by the mod","advancementsBlacklist", Collections.emptyList(), ConfigManager::isValidAdvancementId);

        BLACKLIST_IS_WHITELIST = newBoolVal("Whether the Blacklist of Advancements should be a Whitelist","blacklistIsWhitelist", false);

        ADVANCEMENT_PROGRESSION = newBoolVal("Changing this to true causes each advancement to only be achievable if it's parent has been achieved. Useful for progression systems!","advancementProgression", false);

        CONNECTED_ADVANCEMENTS = newListVal("A list of connected advancements in the format of parent -> child","connectedAdvancementsList", List.of("minecraft:story/follow_ender_eye -> minecraft:end/root", "minecraft:story/form_obsidian -> minecraft:nether/root"), in -> in instanceof String);

        RESET_ADVANCEMENTS_ON_DEATH = newBoolVal("Whether all advancement progress should be reset when the player dies","resetAdvancementProgressOnDeath", false);

        ADVANCEMENT_PROGRESSION_MODE = newEnumVal("The Advancements that are affected by the progression system","advancementProgressionMode", AdvancementProgressionMode.ALL);

        ADVANCEMENT_PROGRESSION_MODE_MOD_BLACKLIST = newListVal("Blacklist of Mods that should not be affected by the advancement progression system","modBlacklist", Collections.emptyList(), ConfigManager::isValidModID);

        ADVANCEMENT_PROGRESSION_MODE_MOD_BLACKLIST_IS_WHITELIST = newBoolVal("Whether the Blacklist of Mods that should not be affected by the advancement progression system should be a Whitelist","modBlacklistIsWhitelist", false);

        ADVANCEMENT_TAB_SORTING_MODE = newEnumVal("In which order the advancement tabs in the advancement screen should be ordered","advancementTabSortingMode", AdvancementTabsSorter.UNSORTED);

        ADVANCEMENT_SORTING_LIST = newListVal("Order of the advancement tabs when DEFINED_LIST is selected","advancementSortingList", Collections.emptyList(), ConfigManager::isValidRootAdvancementID);

        DISABLE_STANDARD_ADVANCEMENT_LOAD = newBoolVal("Whether the mod should overwrite vanilla advancements with generated ones","disableStandardAdvancementLoad", false);

        CONFIG_BUILDER.pop();
        CONFIG_SPEC = CONFIG_BUILDER.build();
    }

    public static ForgeConfigSpec.BooleanValue newBoolVal(String comment, String name, boolean defaultValue){
        return CONFIG_BUILDER.comment(comment).define(name, defaultValue);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Number, V extends ForgeConfigSpec.ConfigValue<T>> V newRangeVal(String comment, String name, T defaultValue, T min, T max){
        CONFIG_BUILDER.comment(comment);
        if(defaultValue instanceof Integer) return (V) CONFIG_BUILDER.defineInRange(name, (int) defaultValue, (int) min,(int) max);
        else if (defaultValue instanceof Double) return (V) CONFIG_BUILDER.defineInRange(name, (double) defaultValue, (double) min, (double) max);
        else if(defaultValue instanceof Long) return (V) CONFIG_BUILDER.defineInRange(name, (long) defaultValue, (long) min, (long) max);
        else throw new IllegalArgumentException("Unsupported number type: " + defaultValue.getClass());
    }

    public static <T> ForgeConfigSpec.ConfigValue<List<? extends T>> newListVal(String comment, String name, Collection<T> list, Predicate<Object> validator){
        return CONFIG_BUILDER.comment(comment).defineList(name, List.copyOf(list), validator);
    }

    @SafeVarargs
    public static <T extends Enum<T>> ForgeConfigSpec.EnumValue<T> newEnumVal(String comment, String name, T defaultValue, T... acceptableValues){
        return CONFIG_BUILDER.comment(comment).defineEnum(name, defaultValue, acceptableValues.length == 0 ? defaultValue.getDeclaringClass().getEnumConstants() : acceptableValues);
    }

    public static ImmutableSet<ResourceLocation> getBlacklistedResourceLocations() {
        Set<ResourceLocation> blacklistedResourceLocations = new HashSet<>();

        ADVANCEMENT_BLACKLIST.get().forEach(String -> blacklistedResourceLocations.add(ResourceLocation.tryParse(String)));

        return ImmutableSet.copyOf(blacklistedResourceLocations);
    }

    public static ImmutableMap<ResourceLocation, ResourceLocation> getConnectedAdvancements() {
        Map<ResourceLocation, ResourceLocation> connectedAdvancements = new HashMap<>();
        CONNECTED_ADVANCEMENTS.get().forEach(s -> {
            int index = s.indexOf("->");
            String s1 = s.substring(0, index - 1);
            String s2 = s.substring(index + 3);
            connectedAdvancements.put(ResourceLocation.tryParse(s2), ResourceLocation.tryParse(s1));
        });
        return ImmutableMap.copyOf(connectedAdvancements);
    }

    public static ImmutableList<ResourceLocation> getSortedAdvancementList(){
        List<ResourceLocation> resourceLocations = new ArrayList<>();
        ADVANCEMENT_SORTING_LIST.get().forEach(s -> resourceLocations.add(ResourceLocation.tryParse(s)));
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
        if(!(in instanceof String s)) return false;
        ResourceLocation resourceLocation = ResourceLocation.tryParse(s);
        if(resourceLocation != null){
            if(ModHelper.getServer().isEmpty()) return true;
            else return ModHelper.getServer().get().getAdvancements().getAllAdvancements().stream().map(Advancement::getId).toList().contains(resourceLocation);
        }
        return false;
    }

    private static boolean isValidRootAdvancementID(Object in){
        if(!(in instanceof String s)) return false;
        ResourceLocation resourceLocation = ResourceLocation.tryParse(s);
        if(resourceLocation != null){
            if(ModHelper.getServer().isEmpty()) return true;
            else return ModHelper.getServer().get().getAdvancements().getAllAdvancements().stream().filter(advancement -> advancement.getParent() == null).map(Advancement::getId).toList().contains(resourceLocation);
        }
        return false;
    }

    private static boolean isValidModID(Object in) {
        if(!(in instanceof String s)) return false;
        if(ModHelper.getServer().isEmpty()) return true;
        else return ModHelper.getServer().get().getAdvancements().getAllAdvancements().stream().map(advancement -> advancement.getId().getNamespace()).toList().contains(s);
    }
}
