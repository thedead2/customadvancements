package de.thedead2.customadvancements.advancements;

import com.google.gson.JsonElement;
import net.minecraft.ResourceLocationException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static de.thedead2.customadvancements.util.ModHelper.*;


public class CustomAdvancementManager {

    private static long counter = 0;


    public static Map<ResourceLocation, JsonElement> modifyData(Map<ResourceLocation, JsonElement> mapIn, ResourceManager resourceManager){
        if(ADVANCEMENTS_TEMP_MAP.isEmpty()){
            ALL_DETECTED_GAME_ADVANCEMENTS.putAll(mapIn);
            ALL_ADVANCEMENTS_RESOURCE_LOCATIONS.addAll(resourceManager.listResources("advancements", resourceLocation -> resourceLocation.toString().endsWith(".json")).keySet());
            ADVANCEMENTS_TEMP_MAP.putAll(mapIn);

            mapIn.clear();

            removeAllAdvancements();
            removeRecipeAdvancements();
            loadAdvancements(CUSTOM_ADVANCEMENTS);
            loadAdvancements(GAME_ADVANCEMENTS);
            removeListAdvancements();


            mapIn.putAll(ADVANCEMENTS_TEMP_MAP);
        }
        else {
            mapIn.clear();
            mapIn.putAll(ADVANCEMENTS_TEMP_MAP);
            ADVANCEMENTS_TEMP_MAP.clear();
        }
        return mapIn;
    }


    private static void loadAdvancements(Map<ResourceLocation, ? extends IAdvancement> advancementsIn){
        if(!advancementsIn.isEmpty() && !ConfigManager.NO_ADVANCEMENTS.get()) {
            String clazz = advancementsIn.values().toArray()[0].getClass().getName();
            String className = clazz.substring(clazz.lastIndexOf(".") + 1);

            LOGGER.info("Starting to load advancements of type: {}", className);
            counter = 0;

            getMissingAdvancements(advancementsIn);

            for(ResourceLocation resourceLocation:advancementsIn.keySet()){
                if(resourceLocation.toString().contains("recipes/") && ConfigManager.NO_RECIPE_ADVANCEMENTS.get()){
                    LOGGER.debug("Skipped recipe advancement: " + resourceLocation);
                    continue;
                }

                IAdvancement advancement = advancementsIn.get(resourceLocation);

                try{
                    if(!ALL_ADVANCEMENTS_RESOURCE_LOCATIONS.contains(resourceLocation)){
                        ResourceLocation resourceLocation1 = new ResourceLocation(resourceLocation.getNamespace(), resourceLocation.getPath().replace(".json", ""));

                        ADVANCEMENTS_TEMP_MAP.put(resourceLocation1, advancement.getJsonObject());
                        LOGGER.debug("Loaded " + advancement.getResourceLocation() + " into Advancement Manager!");
                        counter++;
                    }
                    else {
                        LOGGER.error("Duplicate Resource Location (" + resourceLocation + ") for Advancement: " + advancement.getFileName());
                        LOGGER.debug("All registered advancements: " + advancementsIn);

                        throw new ResourceLocationException("Duplicate Resource Location (" + resourceLocation + ") for Advancement: " + advancement.getFileName());
                    }
                }
                catch (ResourceLocationException e){
                    LOGGER.error("Unable to register advancement {} with resource location: {}", advancement.getFileName(), resourceLocation);
                    e.printStackTrace();
                }
            }

            LOGGER.info("Loaded {} {} into Advancement Manager!", counter, counter != 1 ? (className + "s") : className);
            counter = 0;
        }
    }


    private static void removeRecipeAdvancements(){
        if(ConfigManager.NO_RECIPE_ADVANCEMENTS.get() || ConfigManager.NO_ADVANCEMENTS.get() || DISABLE_STANDARD_ADVANCEMENT_LOAD){
            LOGGER.info("Starting to remove recipe advancements...");

            for (ResourceLocation resourceLocation : ALL_ADVANCEMENTS_RESOURCE_LOCATIONS){
                if (resourceLocation.toString().contains("recipes/")){
                    int jsonExtensionLength = ".json".length();
                    int folderNameLength = "advancements".length() + 1;
                    String resourceLocationPath = resourceLocation.getPath();

                    ResourceLocation resourceLocation1 = new ResourceLocation(resourceLocation.getNamespace(), resourceLocationPath.substring(folderNameLength, resourceLocationPath.length() - jsonExtensionLength));

                    ADVANCEMENTS_TEMP_MAP.remove(resourceLocation1);

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

        getChildren(ADVANCEMENTS_TEMP_MAP);

        for(ResourceLocation blacklistedAdvancement:ConfigManager.getBlacklistedResourceLocations()){
            ADVANCEMENTS_TEMP_MAP.remove(blacklistedAdvancement);
            LOGGER.debug("Removed advancement: " + blacklistedAdvancement);
            counter++;

            REMOVED_ADVANCEMENTS_SET.add(blacklistedAdvancement);

            removeChildren(ADVANCEMENTS_TEMP_MAP, blacklistedAdvancement);
        }

        LOGGER.info("Removed {} Advancements", counter);
        counter = 0;
    }


    private static void removeNoneWhitelistedAdvancements(){
        LOGGER.info("Starting to remove none whitelisted advancements...");

        for (ResourceLocation resourceLocation: ADVANCEMENTS_TEMP_MAP.keySet()) {
            getParents(resourceLocation, true);
        }

        Set<ResourceLocation> mapKeySet = new HashSet<>(ADVANCEMENTS_TEMP_MAP.keySet());

        for(ResourceLocation advancement: mapKeySet){
            if(!ConfigManager.getBlacklistedResourceLocations().contains(advancement) && !CHILDREN_PARENT_MAP.containsValue(advancement)){
                ADVANCEMENTS_TEMP_MAP.remove(advancement);
                LOGGER.debug("Removed advancement: " + advancement);
                counter++;

                REMOVED_ADVANCEMENTS_SET.add(advancement);
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
        if (ConfigManager.NO_ADVANCEMENTS.get() || DISABLE_STANDARD_ADVANCEMENT_LOAD){
            LOGGER.info("Starting to remove all advancements...");

            AtomicInteger counter = new AtomicInteger();
            Set<ResourceLocation> mapKeySet = new HashSet<>(ADVANCEMENTS_TEMP_MAP.keySet());

            mapKeySet.forEach(resourceLocation -> {
                if (!resourceLocation.toString().contains("recipes/")) {
                    ADVANCEMENTS_TEMP_MAP.remove(resourceLocation);
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
                parent = parentField != null ? ResourceLocation.tryParse(parentField.getAsString()) : null;
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
        JsonElement parent = ADVANCEMENTS_TEMP_MAP.get(resourceLocation).getAsJsonObject().get("parent");

        if(parent != null && (ConfigManager.getBlacklistedResourceLocations().contains(resourceLocation) || !checkForBlacklist)){
            ResourceLocation parentResourceLocation = ResourceLocation.tryParse(parent.getAsString());

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
                    parent = parentField != null ? ResourceLocation.tryParse(parentField.getAsString()) : null;
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

                REMOVED_ADVANCEMENTS_SET.add(advancement);

                removeChildren(mapIn, advancement);
            }
        }
        missingAdvancements.clear();
    }
}