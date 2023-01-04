package de.thedead2.customadvancements.advancementsmodifier;

import com.google.gson.JsonElement;
import de.thedead2.customadvancements.advancements.CustomAdvancement;
import de.thedead2.customadvancements.advancements.GameAdvancement;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.ResourceLocationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static de.thedead2.customadvancements.util.ModHelper.*;


public class CustomAdvancementManager {

    private static final Logger LOGGER = LogManager.getLogger();
    private static long counter = 0;


    public static Map<ResourceLocation, JsonElement> modifyData(Map<ResourceLocation, JsonElement> map, IResourceManager resourceManager){
        ALL_DETECTED_GAME_ADVANCEMENTS.putAll(map);
        ALL_ADVANCEMENTS_RESOURCE_LOCATIONS.addAll(resourceManager.getAllResourceLocations("advancements", resourceLocation -> resourceLocation.endsWith(".json")));

        if(DISABLE_STANDARD_ADVANCEMENT_LOAD){
            LOGGER.info("Starting to overwrite game advancements...");
            map = removeBlacklistedAdvancements(
                    removeRecipeAdvancements(
                            loadGameAdvancements(
                                    removeAllAdvancements(
                                            injectData(map), MOD_ID))));
            if(ConfigManager.NO_ADVANCEMENTS.get()){
                map = removeAllAdvancements(map, null);
            }
            return map;
        }
        else {
            return removeAllAdvancements(
                    removeBlacklistedAdvancements(
                            removeRecipeAdvancements(
                                    injectData(map))), null);
        }
    }


    private static Map<ResourceLocation, JsonElement> injectData(Map<ResourceLocation, JsonElement> map) {
        if(!CUSTOM_ADVANCEMENTS.isEmpty() && !ConfigManager.NO_ADVANCEMENTS.get()){
            LOGGER.info("Starting to inject custom advancements into Advancement Manager...");

            for(CustomAdvancement customAdvancement:CUSTOM_ADVANCEMENTS){
                ResourceLocation customResourceLocation = customAdvancement.getResourceLocation();
                JsonElement customJsonElement = customAdvancement.getJsonObject();

                try{
                    if(!ALL_ADVANCEMENTS_RESOURCE_LOCATIONS.contains(customResourceLocation)){
                        ResourceLocation customResourceLocation1 = new ResourceLocation(customResourceLocation.getNamespace(), customResourceLocation.getPath().replace(".json", ""));

                        map.put(customResourceLocation1, customJsonElement);
                        LOGGER.debug("Injected " + customAdvancement.getFileName() + " into Advancement Manager!");
                    }
                    else {
                        LOGGER.error("The Resource Location " + customResourceLocation + " for " + customAdvancement.getFileName() + " already exists!");
                        LOGGER.debug("All registered Resource Locations of advancements: " + ALL_ADVANCEMENTS_RESOURCE_LOCATIONS);
                        LOGGER.debug("All registered custom advancements: " + CUSTOM_ADVANCEMENTS);

                        throw new ResourceLocationException("Duplicate Resource Location (" + customResourceLocation + ") for Custom Advancement: " + customAdvancement.getFileName());
                    }
                }
                catch (ResourceLocationException e){
                    LOGGER.error("Unable to register advancement {} with resource location: {}", customAdvancement.getFileName(), customResourceLocation);
                    e.printStackTrace();
                }
            }

            LOGGER.info("Injected {} Custom Advancements into Advancement Manager!", CUSTOM_ADVANCEMENTS.size());
        }

        return map;
    }


    private static Map<ResourceLocation, JsonElement> removeRecipeAdvancements(Map<ResourceLocation, JsonElement> map){
        if(ConfigManager.NO_RECIPE_ADVANCEMENTS.get() && !ConfigManager.NO_ADVANCEMENTS.get() && !ConfigManager.BLACKLIST_IS_WHITELIST.get()){
            LOGGER.info("Starting to remove recipe advancements...");

            for (ResourceLocation resourceLocation : ALL_ADVANCEMENTS_RESOURCE_LOCATIONS){
                if (resourceLocation.toString().contains("recipes/")){
                    int jsonExtensionLength = ".json".length();
                    int folderNameLength = "advancements".length() + 1;
                    String resourceLocationPath = resourceLocation.getPath();

                    ResourceLocation resourceLocation1 = new ResourceLocation(resourceLocation.getNamespace(), resourceLocationPath.substring(folderNameLength, resourceLocationPath.length() - jsonExtensionLength));

                    map.remove(resourceLocation1);
                    counter++;
                    LOGGER.debug("Removed recipe advancement: " + resourceLocation);
                }
            }

            LOGGER.info("Removed {} Recipe Advancements!", counter);
            counter = 0;
        }
        return map;
    }


    private static Map<ResourceLocation, JsonElement> removeBlacklistedAdvancements(Map<ResourceLocation, JsonElement> map){
        if(!ConfigManager.getBlacklistedResourceLocations().isEmpty() && !ConfigManager.NO_ADVANCEMENTS.get() && !ConfigManager.BLACKLIST_IS_WHITELIST.get()){
            LOGGER.info("Starting to remove blacklisted advancements...");

            getChildren(map);

            for(ResourceLocation blacklistedAdvancement:ConfigManager.getBlacklistedResourceLocations()){
                map.remove(blacklistedAdvancement);
                LOGGER.debug("Removed advancement: " + blacklistedAdvancement);
                counter++;

                REMOVED_ADVANCEMENTS_SET.add(blacklistedAdvancement);

                map = (Map<ResourceLocation, JsonElement>) removeChildren(map, blacklistedAdvancement);
            }

            LOGGER.info("Removed {} Advancements", counter);
            counter = 0;
        }
        else if (!ConfigManager.getBlacklistedResourceLocations().isEmpty() && !ConfigManager.NO_ADVANCEMENTS.get() && ConfigManager.BLACKLIST_IS_WHITELIST.get()) {
            LOGGER.info("Starting to remove blacklisted advancements...");

            for (ResourceLocation resourceLocation:map.keySet()) {
                getParents(map ,resourceLocation, true);
            }

            Set<ResourceLocation> mapKeySet = new HashSet<>(map.keySet());

            for(ResourceLocation advancement: mapKeySet){
                if(!ConfigManager.getBlacklistedResourceLocations().contains(advancement) && !CHILDREN_PARENT_MAP.containsValue(advancement)){
                    map.remove(advancement);
                    LOGGER.debug("Removed advancement: " + advancement);
                    counter++;

                    REMOVED_ADVANCEMENTS_SET.add(advancement);
                }
            }

            LOGGER.info("Removed {} Advancements", counter);
            counter = 0;
        }
        else if(ConfigManager.getBlacklistedResourceLocations().isEmpty() && ConfigManager.BLACKLIST_IS_WHITELIST.get()){
            map = removeAllAdvancements(map, null);
        }
        return map;
    }


    private static void getChildren(Map<ResourceLocation, JsonElement> map){
        PARENT_CHILDREN_MAP.clear();

        for (ResourceLocation resourceLocation:map.keySet()){
            JsonElement parent = map.get(resourceLocation).getAsJsonObject().get("parent");

            if(parent != null){
                PARENT_CHILDREN_MAP.put(ResourceLocation.tryCreate(parent.getAsString()), resourceLocation);
            }
        }
    }

    private static void getChildren(){
        PARENT_CHILDREN_MAP.clear();

        for (ResourceLocation resourceLocation:GAME_ADVANCEMENTS.keySet()){
            ResourceLocation parent = GAME_ADVANCEMENTS.get(resourceLocation).getParentAdvancement();

            if(parent != null){
                PARENT_CHILDREN_MAP.put(parent, resourceLocation);
            }
        }
    }


    private static void getParents(Map<ResourceLocation, JsonElement> map, ResourceLocation resourceLocation, boolean checkForBlacklist){
        JsonElement parent = map.get(resourceLocation).getAsJsonObject().get("parent");

        if(parent != null && ConfigManager.getBlacklistedResourceLocations().contains(resourceLocation)){
            ResourceLocation parentResourceLocation = ResourceLocation.tryCreate(parent.getAsString());

            CHILDREN_PARENT_MAP.put(resourceLocation, parentResourceLocation);
            getParents(map, parentResourceLocation, false);
        }
        else if (parent != null && !checkForBlacklist){
            ResourceLocation parentResourceLocation = ResourceLocation.tryCreate(parent.getAsString());

            CHILDREN_PARENT_MAP.put(resourceLocation, parentResourceLocation);
            getParents(map, parentResourceLocation, false);
        }
    }


    private static Map<ResourceLocation, ?> removeChildren(Map<ResourceLocation, ?> mapIn, ResourceLocation resourceLocationIn){
        for(ResourceLocation childAdvancement: PARENT_CHILDREN_MAP.get(resourceLocationIn)){
            if(mapIn.containsKey(childAdvancement) && !DISABLE_STANDARD_ADVANCEMENT_LOAD) {
                mapIn.remove(childAdvancement);
                LOGGER.debug("Removed child advancement: " + childAdvancement);
                counter++;

                REMOVED_ADVANCEMENTS_SET.add(childAdvancement);
            }
            else if (DISABLE_STANDARD_ADVANCEMENT_LOAD && mapIn.containsKey(childAdvancement)){
                mapIn.remove(childAdvancement);
                LOGGER.debug("Skipped child advancement {} as it's parent couldn't be loaded!", childAdvancement);
                counter++;
            }
            removeChildren(mapIn, childAdvancement);
        }
        return mapIn;
    }


    private static Map<ResourceLocation, JsonElement> removeAllAdvancements(Map<ResourceLocation, JsonElement> map, String filter){
        if(ConfigManager.NO_ADVANCEMENTS.get() || DISABLE_STANDARD_ADVANCEMENT_LOAD) {
            LOGGER.info("Starting to remove all advancements...");

            AtomicInteger counter = new AtomicInteger();
            Set<ResourceLocation> mapKeySet = new HashSet<>(map.keySet());

            if (filter == null){
                map.clear();
                LOGGER.info("Removed all advancements!");
            }
            else {
                mapKeySet.forEach(resourceLocation -> {
                    if (!resourceLocation.toString().contains(filter) && !resourceLocation.toString().contains("recipes/")) {
                        map.remove(resourceLocation);
                        LOGGER.debug("Removed advancement: " + resourceLocation);
                        counter.getAndIncrement();
                    }
                });
                LOGGER.info("Removed {} Advancements!", counter.get());
                counter.set(0);
            }
        }
        return map;
    }


    private static Map<ResourceLocation, JsonElement> loadGameAdvancements(Map<ResourceLocation, JsonElement> map){
        if(!GAME_ADVANCEMENTS.isEmpty()) {
            LOGGER.info("Starting to load game advancements...");

            Set<ResourceLocation> parentResourceLocations = new HashSet<>();
            Set<ResourceLocation> temp = new HashSet<>();
            GAME_ADVANCEMENTS.forEach((resourceLocation, gameAdvancement) -> parentResourceLocations.add(gameAdvancement.getParentAdvancement()));

            if(!GAME_ADVANCEMENTS.keySet().containsAll(parentResourceLocations)){
                GAME_ADVANCEMENTS.forEach((resourceLocation, gameAdvancement) -> {
                    if(!GAME_ADVANCEMENTS.containsKey(gameAdvancement.getParentAdvancement()) && gameAdvancement.getParentAdvancement() != null){
                        temp.add(resourceLocation);
                    }
                });
                BLACKLISTED_GAME_ADVANCEMENTS.addAll(temp);
                temp.clear();
            }
            parentResourceLocations.clear();

            if(!BLACKLISTED_GAME_ADVANCEMENTS.isEmpty()){

                getChildren();

                for(ResourceLocation blacklistedAdvancement:BLACKLISTED_GAME_ADVANCEMENTS){
                    GAME_ADVANCEMENTS.remove(blacklistedAdvancement);
                    LOGGER.debug("Skipped advancement {} as it's parent wasn't found!", blacklistedAdvancement);
                    counter++;

                    REMOVED_ADVANCEMENTS_SET.add(blacklistedAdvancement);

                    GAME_ADVANCEMENTS = (Map<ResourceLocation, GameAdvancement>) removeChildren(GAME_ADVANCEMENTS, blacklistedAdvancement);
                }
            }

            for(ResourceLocation resourceLocation:GAME_ADVANCEMENTS.keySet()){
                GameAdvancement gameAdvancement = GAME_ADVANCEMENTS.get(resourceLocation);

                JsonElement customJsonElement = gameAdvancement.getJsonObject();

                try{
                    if(!ALL_ADVANCEMENTS_RESOURCE_LOCATIONS.contains(resourceLocation)){
                        ResourceLocation customResourceLocation1 = new ResourceLocation(resourceLocation.getNamespace(), resourceLocation.getPath().replace(".json", ""));

                        map.put(customResourceLocation1, customJsonElement);
                        LOGGER.debug("Loaded " + gameAdvancement.getFileName() + " into Advancement Manager!");
                    }
                    else {
                        LOGGER.error("The Resource Location " + resourceLocation + " for " + gameAdvancement.getFileName() + " already exists!");
                        LOGGER.debug("All registered Resource Locations of advancements: " + ALL_ADVANCEMENTS_RESOURCE_LOCATIONS);
                        LOGGER.debug("All registered custom advancements: " + GAME_ADVANCEMENTS);

                        throw new ResourceLocationException("Duplicate Resource Location (" + resourceLocation + ") for Custom Advancement: " + gameAdvancement.getFileName());
                    }
                }
                catch (ResourceLocationException e){
                    LOGGER.error("Unable to register advancement {} with resource location: {}", gameAdvancement.getFileName(), resourceLocation);
                    e.printStackTrace();
                }
            }

            LOGGER.info("Loaded {} Game Advancements into Advancement Manager!", GAME_ADVANCEMENTS.size());

            if(!BLACKLISTED_GAME_ADVANCEMENTS.isEmpty()){
                LOGGER.warn("Skipped {} Advancements as their parents couldn't be loaded!", counter);
                LOGGER.warn("Couldn't find parents for: {}", BLACKLISTED_GAME_ADVANCEMENTS);
                counter = 0;
            }
        }

        return map;
    }
}