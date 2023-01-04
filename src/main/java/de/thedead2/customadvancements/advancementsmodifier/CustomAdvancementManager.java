package de.thedead2.customadvancements.advancementsmodifier;

import com.google.gson.JsonElement;
import de.thedead2.customadvancements.advancements.IAdvancement;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.ResourceLocationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static de.thedead2.customadvancements.util.ModHelper.*;


public class CustomAdvancementManager {

    private static final Logger LOGGER = LogManager.getLogger();
    private static long counter = 0;
    private static final Map<ResourceLocation, JsonElement> TEMP_MAP = new HashMap<>();


    public static Map<ResourceLocation, JsonElement> modifyData(Map<ResourceLocation, JsonElement> mapIn, IResourceManager resourceManager){
        ALL_DETECTED_GAME_ADVANCEMENTS.putAll(mapIn);
        ALL_ADVANCEMENTS_RESOURCE_LOCATIONS.addAll(resourceManager.getAllResourceLocations("advancements", resourceLocation -> resourceLocation.endsWith(".json")));

        TEMP_MAP.putAll(mapIn);

        if(DISABLE_STANDARD_ADVANCEMENT_LOAD){
            removeAllAdvancements();
            loadAdvancements(CUSTOM_ADVANCEMENTS);
            loadAdvancements(GAME_ADVANCEMENTS);
            removeRecipeAdvancements();

            if(ConfigManager.NO_ADVANCEMENTS.get()){
                removeAllAdvancements();
            }
        }
        else {
            loadAdvancements(CUSTOM_ADVANCEMENTS);
            removeRecipeAdvancements();
            removeBlacklistedAdvancements();
            removeAllAdvancements();
        }
        return TEMP_MAP;
    }


    private static void loadAdvancements(Map<ResourceLocation, ? extends IAdvancement> advancementsIn){
        if(!advancementsIn.isEmpty() && !ConfigManager.NO_ADVANCEMENTS.get()) {
            LOGGER.info("Starting to load advancements of type: {}", advancementsIn.values().toArray()[0].getClass().getName());
            counter = 0;

            getMissingAdvancements(advancementsIn);

            for(ResourceLocation resourceLocation:advancementsIn.keySet()){
                IAdvancement advancement = advancementsIn.get(resourceLocation);

                JsonElement jsonElement = advancement.getJsonObject();

                try{
                    if(!ALL_ADVANCEMENTS_RESOURCE_LOCATIONS.contains(resourceLocation)){
                        ResourceLocation resourceLocation1 = new ResourceLocation(resourceLocation.getNamespace(), resourceLocation.getPath().replace(".json", ""));

                        TEMP_MAP.put(resourceLocation1, jsonElement);
                        LOGGER.debug("Loaded " + advancement.getFileName() + " into Advancement Manager!");
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

            LOGGER.info("Loaded {} Advancements into Advancement Manager!", counter);
            counter = 0;
        }
    }


    private static void removeRecipeAdvancements(){
        if(ConfigManager.NO_RECIPE_ADVANCEMENTS.get() || ConfigManager.NO_ADVANCEMENTS.get()){
            LOGGER.info("Starting to remove recipe advancements...");

            for (ResourceLocation resourceLocation : ALL_ADVANCEMENTS_RESOURCE_LOCATIONS){
                if (resourceLocation.toString().contains("recipes/")){
                    int jsonExtensionLength = ".json".length();
                    int folderNameLength = "advancements".length() + 1;
                    String resourceLocationPath = resourceLocation.getPath();

                    ResourceLocation resourceLocation1 = new ResourceLocation(resourceLocation.getNamespace(), resourceLocationPath.substring(folderNameLength, resourceLocationPath.length() - jsonExtensionLength));

                    TEMP_MAP.remove(resourceLocation1);

                    counter++;
                    LOGGER.debug("Removed recipe advancement: " + resourceLocation);
                }
            }

            LOGGER.info("Removed {} Recipe Advancements!", counter);
            counter = 0;
        }
    }


    private static void removeBlacklistedAdvancements(){
        if(!ConfigManager.getBlacklistedResourceLocations().isEmpty() && !ConfigManager.NO_ADVANCEMENTS.get() && !ConfigManager.BLACKLIST_IS_WHITELIST.get()){
            LOGGER.info("Starting to remove blacklisted advancements...");

            getChildren(TEMP_MAP);

            for(ResourceLocation blacklistedAdvancement:ConfigManager.getBlacklistedResourceLocations()){
                TEMP_MAP.remove(blacklistedAdvancement);
                LOGGER.debug("Removed advancement: " + blacklistedAdvancement);
                counter++;

                REMOVED_ADVANCEMENTS_SET.add(blacklistedAdvancement);

                removeChildren(TEMP_MAP, blacklistedAdvancement);
            }

            LOGGER.info("Removed {} Advancements", counter);
            counter = 0;
        }
        else if (!ConfigManager.getBlacklistedResourceLocations().isEmpty() && !ConfigManager.NO_ADVANCEMENTS.get() && ConfigManager.BLACKLIST_IS_WHITELIST.get()) {
            LOGGER.info("Starting to remove none whitelisted advancements...");

            for (ResourceLocation resourceLocation:TEMP_MAP.keySet()) {
                getParents(resourceLocation, true);
            }

            Set<ResourceLocation> mapKeySet = new HashSet<>(TEMP_MAP.keySet());

            for(ResourceLocation advancement: mapKeySet){
                if(!ConfigManager.getBlacklistedResourceLocations().contains(advancement) && !CHILDREN_PARENT_MAP.containsValue(advancement)){
                    TEMP_MAP.remove(advancement);
                    LOGGER.debug("Removed advancement: " + advancement);
                    counter++;

                    REMOVED_ADVANCEMENTS_SET.add(advancement);
                }
            }

            LOGGER.info("Removed {} Advancements", counter);
            counter = 0;
        }
        else if(ConfigManager.getBlacklistedResourceLocations().isEmpty() && ConfigManager.BLACKLIST_IS_WHITELIST.get()){
            removeAllAdvancements();
        }
    }

    private static void removeAllAdvancements(){
        if(ConfigManager.NO_ADVANCEMENTS.get() || DISABLE_STANDARD_ADVANCEMENT_LOAD) {
            LOGGER.info("Starting to remove all advancements...");

            AtomicInteger counter = new AtomicInteger();
            Set<ResourceLocation> mapKeySet = new HashSet<>(TEMP_MAP.keySet());

            mapKeySet.forEach(resourceLocation -> {
                if (!resourceLocation.toString().contains("recipes/")) {
                    TEMP_MAP.remove(resourceLocation);
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
                throw new RuntimeException("Unexpected input: Map<ResourceLocation, " + mapIn.get(resourceLocation).getClass() + ">!");
            }

            if (parent != null) {
                PARENT_CHILDREN_MAP.put(parent, resourceLocation);
            }
        }
    }


    private static void getParents(ResourceLocation resourceLocation, boolean checkForBlacklist){
        JsonElement parent = TEMP_MAP.get(resourceLocation).getAsJsonObject().get("parent");

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
                    throw new RuntimeException("Unexpected input: Map<ResourceLocation, " + mapIn.get(childAdvancement).getClass() + ">!");
                }

                mapIn.remove(childAdvancement);
                LOGGER.debug("Removed child advancement {} with parent {} as it's parent couldn't be loaded!", childAdvancement, parent);
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
    }
}