package de.thedead2.customadvancements.advancementsmodifier;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.JsonElement;
import de.thedead2.customadvancements.CustomAdvancement;
import de.thedead2.customadvancements.util.FileHandler;
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
    private static final Multimap<ResourceLocation, ResourceLocation> parentChildrenMap = ArrayListMultimap.create();
    private static final Map<ResourceLocation, ResourceLocation> childrenParentMap = new HashMap<>();


    public static Map<ResourceLocation, JsonElement> modifyData(Map<ResourceLocation, JsonElement> map){
        return removeAllAdvancements(removeBlacklistedAdvancements(removeRecipeAdvancements(injectData(map))));
    }


    private static Map<ResourceLocation, JsonElement> injectData(Map<ResourceLocation, JsonElement> map) {
        if(!CUSTOM_ADVANCEMENTS.isEmpty() && !ConfigManager.NO_ADVANCEMENTS.get()){
            LOGGER.info("Starting to inject custom advancements into Advancement Manager...");

            for(CustomAdvancement customAdvancement:CUSTOM_ADVANCEMENTS){
                ResourceLocation customResourceLocation = customAdvancement.getResourceLocation();
                JsonElement customJsonElement = customAdvancement.getJsonObject();

                try{
                    if(!FileHandler.resourceLocations.contains(customResourceLocation)){
                        ResourceLocation customResourceLocation1 = new ResourceLocation(customResourceLocation.getNamespace(), customResourceLocation.getPath().replace(".json", ""));

                        map.put(customResourceLocation1, customJsonElement);
                        LOGGER.debug("Injected " + customAdvancement.getFileName() + " into Advancement Manager!");
                    }
                    else {
                        LOGGER.error("The Resource Location " + customResourceLocation + " for " + customAdvancement.getFileName() + " already exists!");
                        LOGGER.debug("All registered Resource Locations of advancements: " + FileHandler.resourceLocations);
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

            for (ResourceLocation resourceLocation : FileHandler.resourceLocations){
                if (resourceLocation.toString().contains("/recipes/")){
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

                map = removeChildren(map, blacklistedAdvancement);
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
                if(!ConfigManager.getBlacklistedResourceLocations().contains(advancement) && !childrenParentMap.containsValue(advancement)){
                    map.remove(advancement);
                    LOGGER.debug("Removed advancement: " + advancement);
                    counter++;
                }
            }

            LOGGER.info("Removed {} Advancements", counter);
            counter = 0;
        }

        return map;
    }


    private static void getChildren(Map<ResourceLocation, JsonElement> map){
        for (ResourceLocation resourceLocation:map.keySet()){
            JsonElement parent = map.get(resourceLocation).getAsJsonObject().get("parent");

            if(parent != null){
                parentChildrenMap.put(ResourceLocation.tryCreate(parent.getAsString()), resourceLocation);
            }
        }
    }


    private static void getParents(Map<ResourceLocation, JsonElement> map, ResourceLocation resourceLocation, boolean checkForBlacklist){
        JsonElement parent = map.get(resourceLocation).getAsJsonObject().get("parent");

        if(parent != null && ConfigManager.getBlacklistedResourceLocations().contains(resourceLocation)){
            ResourceLocation parentResourceLocation = ResourceLocation.tryCreate(parent.getAsString());

            childrenParentMap.put(resourceLocation, parentResourceLocation);
            getParents(map, parentResourceLocation, false);
        }
        else if (parent != null && !checkForBlacklist){
            ResourceLocation parentResourceLocation = ResourceLocation.tryCreate(parent.getAsString());

            childrenParentMap.put(resourceLocation, parentResourceLocation);
            getParents(map, parentResourceLocation, false);
        }
    }


    private static Map<ResourceLocation, JsonElement> removeChildren(Map<ResourceLocation, JsonElement> map, ResourceLocation resourceLocationIn){
        for(ResourceLocation childAdvancement:parentChildrenMap.get(resourceLocationIn)){
            if(map.containsKey(childAdvancement)) {
                map.remove(childAdvancement);
                LOGGER.debug("Removed child advancement: " + childAdvancement);
                counter++;
            }
            removeChildren(map, childAdvancement);
        }
        return map;
    }


    private static Map<ResourceLocation, JsonElement> removeAllAdvancements(Map<ResourceLocation, JsonElement> map){
        if(ConfigManager.NO_ADVANCEMENTS.get()){
            LOGGER.info("Starting to remove all advancements...");

            AtomicInteger counter = new AtomicInteger();
            Set<ResourceLocation> mapKeySet = new HashSet<>(map.keySet());

            mapKeySet.forEach(resourceLocation -> {
                map.remove(resourceLocation);
                LOGGER.debug("Removed advancement: " + resourceLocation);
                counter.getAndIncrement();
            });

            LOGGER.info("Removed {} Advancements!", counter.get());
            counter.set(0);
        }
        return map;
    }
}