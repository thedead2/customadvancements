package de.thedead2.customadvancements.advancements;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.JsonElement;
import de.thedead2.customadvancements.advancements.advancementtypes.IAdvancement;
import de.thedead2.customadvancements.util.core.ConfigManager;
import de.thedead2.customadvancements.util.Timer;
import de.thedead2.customadvancements.util.core.CrashHandler;

import net.minecraft.crash.CrashReport;
import net.minecraft.crash.ReportedException;
import net.minecraft.util.ResourceLocation;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static de.thedead2.customadvancements.util.core.ModHelper.*;


public abstract class CustomAdvancementManager {

    private static long counter = 0;
    private static final Map<ResourceLocation, JsonElement> ADVANCEMENTS = new HashMap<>();
    private static final Multimap<ResourceLocation, ResourceLocation> PARENT_CHILDREN_MAP = ArrayListMultimap.create();
    private static final Map<ResourceLocation, ResourceLocation> CHILDREN_PARENT_MAP = new HashMap<>();
    public static final Collection<ResourceLocation> ALL_ADVANCEMENTS_RESOURCE_LOCATIONS = new HashSet<>();
    private static final Timer TIMER = new Timer();
    private static boolean safeMode = false;

    public static void modifyAdvancementData(Map<ResourceLocation, JsonElement> mapIn) {
        if(!safeMode){
            TIMER.start();
            try {
                if (ADVANCEMENTS.isEmpty()){
                    if(!ConfigManager.DISABLE_STANDARD_ADVANCEMENT_LOAD.get()){
                        ADVANCEMENTS.putAll(mapIn);
                    }
                    ALL_ADVANCEMENTS_RESOURCE_LOCATIONS.addAll(mapIn.keySet());

                    loadAdvancements(CUSTOM_ADVANCEMENTS);
                    loadAdvancements(GAME_ADVANCEMENTS);
                    removeListAdvancements();
                    removeRecipeAdvancements();
                    removeAllAdvancements();

                    mapIn.clear();
                    mapIn.putAll(ADVANCEMENTS);
                }
                else {
                    mapIn.clear();
                    mapIn.putAll(ADVANCEMENTS);
                    ADVANCEMENTS.clear();
                }
                LOGGER.debug("Modifying Advancement data took {} ms.", TIMER.getTime());
            }
            catch (Throwable e){
                CrashReport crashReport = new CrashReport("Error while modifying advancement data!", e);
                CrashHandler.getInstance().printCrashReport(crashReport);
                throw new ReportedException(crashReport);
            }
            finally {
                TIMER.stop(true);
            }
        }
        else {
            LOGGER.warn("Safe Mode is enabled! Skipping advancement load...");
        }
    }

    public static void enableSafeMode(){
        safeMode = true;
    }

    public static void disableSafeMode(){
        safeMode = false;
    }

    private static void loadAdvancements(Map<ResourceLocation, ? extends IAdvancement> advancementsIn){
        if(!advancementsIn.isEmpty() && !ConfigManager.NO_ADVANCEMENTS.get()) {
            String clazz = advancementsIn.values().toArray()[0].getClass().getName();
            clazz = clazz.substring(clazz.lastIndexOf(".") + 1);
            String className = new StringBuilder(clazz).insert(clazz.indexOf("A"), " ").toString();

            LOGGER.info("Starting to load advancements of type: {}", className);
            counter = 0;

            if(ConfigManager.DISABLE_STANDARD_ADVANCEMENT_LOAD.get()){
                getMissingAdvancements(advancementsIn);
            }

            for(ResourceLocation resourceLocation:advancementsIn.keySet()){
                CrashHandler.getInstance().setActiveAdvancement(advancementsIn.get(resourceLocation));
                if(resourceLocation.toString().contains("recipes/") && ConfigManager.NO_RECIPE_ADVANCEMENTS.get()){
                    LOGGER.debug("Skipped recipe advancement: " + resourceLocation);
                    continue;
                }

                IAdvancement advancement = advancementsIn.get(resourceLocation);

                if(!ALL_ADVANCEMENTS_RESOURCE_LOCATIONS.contains(resourceLocation)){
                    ResourceLocation resourceLocation1 = new ResourceLocation(resourceLocation.getNamespace(), resourceLocation.getPath().replace(".json", ""));

                    if(ADVANCEMENTS.containsKey(resourceLocation1)){
                        ADVANCEMENTS.remove(resourceLocation1);
                        LOGGER.debug("Overwriting advancement: " + resourceLocation1);
                    }
                    ADVANCEMENTS.put(resourceLocation1, advancement.getJsonObject());
                    LOGGER.debug("Loaded " + advancement.getResourceLocation() + " into Advancement Manager!");
                    counter++;
                }
                else {
                    LOGGER.error("Duplicate resource location '" + resourceLocation + "' for advancement: " + advancement.getFileName());
                }
            }

            CrashHandler.getInstance().setActiveAdvancement(null);
            LOGGER.info("Loaded {} {} into Advancement Manager!", counter, counter != 1 ? (className + "s") : className);
            counter = 0;
        }
    }


    private static void removeRecipeAdvancements(){
        if(ConfigManager.NO_RECIPE_ADVANCEMENTS.get() || ConfigManager.NO_ADVANCEMENTS.get()){
            LOGGER.info("Starting to remove recipe advancements...");

            for (ResourceLocation resourceLocation : ALL_ADVANCEMENTS_RESOURCE_LOCATIONS){
                if (resourceLocation.toString().contains("recipes/")){
                    /*int jsonExtensionLength = ".json".length();
                    int folderNameLength = "advancements".length() + 1;
                    String resourceLocationPath = resourceLocation.getPath();

                    ResourceLocation resourceLocation1 = new ResourceLocation(resourceLocation.getNamespace(), resourceLocationPath.substring(folderNameLength, resourceLocationPath.length() - jsonExtensionLength));
*/
                    ADVANCEMENTS.remove(resourceLocation);

                    CrashHandler.getInstance().addRemovedAdvancement(resourceLocation);
                    counter++;
                    LOGGER.debug("Removed recipe advancement: " + resourceLocation);
                }
            }

            LOGGER.info("Removed {} Recipe Advancements!", counter);
            counter = 0;
        }
    }


    private static void removeBlacklistedAdvancements(){
        LOGGER.info("Starting to remove blacklisted advancements...");

        getChildren(ADVANCEMENTS);

        for(ResourceLocation blacklistedAdvancement:ConfigManager.getBlacklistedResourceLocations()){
            ADVANCEMENTS.remove(blacklistedAdvancement);
            LOGGER.debug("Removed advancement: " + blacklistedAdvancement);
            counter++;

            CrashHandler.getInstance().addRemovedAdvancement(blacklistedAdvancement);

            removeChildren(ADVANCEMENTS, blacklistedAdvancement);
        }

        LOGGER.info("Removed {} Advancements", counter);
        counter = 0;
    }


    private static void removeNoneWhitelistedAdvancements(){
        LOGGER.info("Starting to remove none whitelisted advancements...");

        for (ResourceLocation resourceLocation: ADVANCEMENTS.keySet()) {
            getParents(resourceLocation, true);
        }

        Set<ResourceLocation> mapKeySet = new HashSet<>(ADVANCEMENTS.keySet());

        for(ResourceLocation advancement: mapKeySet){
            if(!ConfigManager.getBlacklistedResourceLocations().contains(advancement) && !CHILDREN_PARENT_MAP.containsValue(advancement)){
                ADVANCEMENTS.remove(advancement);
                LOGGER.debug("Removed advancement: " + advancement);
                counter++;

                CrashHandler.getInstance().addRemovedAdvancement(advancement);
            }
        }

        LOGGER.info("Removed {} Advancements", counter);
        counter = 0;
    }


    private static void removeListAdvancements(){
        if(!ConfigManager.getBlacklistedResourceLocations().isEmpty() && !ConfigManager.NO_ADVANCEMENTS.get() && !ConfigManager.BLACKLIST_IS_WHITELIST.get()){
            removeBlacklistedAdvancements();
        }
        else if (!ConfigManager.getBlacklistedResourceLocations().isEmpty() && !ConfigManager.NO_ADVANCEMENTS.get() && ConfigManager.BLACKLIST_IS_WHITELIST.get()) {
            removeNoneWhitelistedAdvancements();
        }
        else if(ConfigManager.getBlacklistedResourceLocations().isEmpty() && ConfigManager.BLACKLIST_IS_WHITELIST.get()){
            removeAllAdvancements();
        }
    }


    private static void removeAllAdvancements() {
        if (ConfigManager.NO_ADVANCEMENTS.get() || ConfigManager.getBlacklistedResourceLocations().isEmpty() && ConfigManager.BLACKLIST_IS_WHITELIST.get()){
            LOGGER.info("Starting to remove all advancements...");

            AtomicInteger counter = new AtomicInteger();
            Set<ResourceLocation> mapKeySet = new HashSet<>(ADVANCEMENTS.keySet());

            mapKeySet.forEach(resourceLocation -> {
                if (!resourceLocation.toString().contains("recipes/")) {
                    ADVANCEMENTS.remove(resourceLocation);
                    CrashHandler.getInstance().addRemovedAdvancement(resourceLocation);
                    LOGGER.debug("Removed advancement: " + resourceLocation);
                    counter.getAndIncrement();
                }
            });
            LOGGER.info("Removed {} Advancements!", counter.get());
            counter.set(0);
        }
    }


    private static void getChildren(Map<ResourceLocation, ?> mapIn){
        PARENT_CHILDREN_MAP.clear();

        for (ResourceLocation resourceLocation:mapIn.keySet()) {
            ResourceLocation parent;

            if (mapIn.get(resourceLocation) instanceof JsonElement) {
                JsonElement parentField = ((JsonElement) mapIn.get(resourceLocation)).getAsJsonObject().get("parent");
                parent = parentField != null ? ResourceLocation.tryCreate(parentField.getAsString()) : null;
            }
            else if (mapIn.get(resourceLocation) instanceof IAdvancement) {
                parent = ((IAdvancement) mapIn.get(resourceLocation)).getParentAdvancement();
            }
            else {
                throw new RuntimeException("Unexpected input: Map<ResourceLocation, " + mapIn.get(resourceLocation).getClass().getName() + ">!");
            }

            if (parent != null) {
                PARENT_CHILDREN_MAP.put(parent, resourceLocation);
            }
        }
    }


    private static void getParents(ResourceLocation resourceLocation, boolean checkForBlacklist){
        JsonElement parent = ADVANCEMENTS.get(resourceLocation).getAsJsonObject().get("parent");

        if(parent != null && (ConfigManager.getBlacklistedResourceLocations().contains(resourceLocation) || !checkForBlacklist)){
            ResourceLocation parentResourceLocation = ResourceLocation.tryCreate(parent.getAsString());

            CHILDREN_PARENT_MAP.put(resourceLocation, parentResourceLocation);

            getParents(parentResourceLocation, false);
        }
    }


    private static void removeChildren(Map<ResourceLocation, ?> mapIn, ResourceLocation resourceLocationIn){
        for(ResourceLocation childAdvancement: PARENT_CHILDREN_MAP.get(resourceLocationIn)){
            if (mapIn.containsKey(childAdvancement)){
                ResourceLocation parent;

                if (mapIn.get(childAdvancement) instanceof IAdvancement){
                    parent = ((IAdvancement) mapIn.get(childAdvancement)).getParentAdvancement();
                }
                else if(mapIn.get(childAdvancement) instanceof JsonElement){
                    JsonElement parentField = ((JsonElement) mapIn.get(childAdvancement)).getAsJsonObject().get("parent");
                    parent = parentField != null ? ResourceLocation.tryCreate(parentField.getAsString()) : null;
                }
                else {
                    throw new RuntimeException("Unexpected input: Map<ResourceLocation, " + mapIn.get(childAdvancement).getClass().getName() + ">!");
                }

                mapIn.remove(childAdvancement);
                LOGGER.debug("Removed child advancement {} with parent {} as it's parent wasn't loaded!", childAdvancement, parent);
                counter++;
            }
            removeChildren(mapIn, childAdvancement);
        }
    }


    private static void getMissingAdvancements(Map<ResourceLocation, ? extends IAdvancement> mapIn){
        Set<ResourceLocation> parentResourceLocations = new HashSet<>();
        Set<ResourceLocation> missingAdvancements = new HashSet<>();

        mapIn.forEach((resourceLocation, IAdvancement) -> parentResourceLocations.add(IAdvancement.getParentAdvancement()));

        if(!mapIn.keySet().containsAll(parentResourceLocations)){
            mapIn.forEach((resourceLocation, IAdvancement) -> {
                if(!mapIn.containsKey(IAdvancement.getParentAdvancement()) && IAdvancement.getParentAdvancement() != null){
                    missingAdvancements.add(resourceLocation);
                }
            });
        }
        parentResourceLocations.clear();

        if(!missingAdvancements.isEmpty()){

            getChildren(mapIn);

            for(ResourceLocation advancement:missingAdvancements){
                ResourceLocation parent = mapIn.get(advancement) != null ? mapIn.get(advancement).getParentAdvancement() : null;
                mapIn.remove(advancement);
                LOGGER.debug("Skipped advancement {} with parent {} as it's parent wasn't found!", advancement, parent);
                counter++;

                CrashHandler.getInstance().addRemovedAdvancement(advancement);

                removeChildren(mapIn, advancement);
            }
        }
        missingAdvancements.clear();
    }

    public static void clearAll() {
        PARENT_CHILDREN_MAP.clear();
        CHILDREN_PARENT_MAP.clear();
        ADVANCEMENTS.clear();
        ALL_ADVANCEMENTS_RESOURCE_LOCATIONS.clear();
    }
}