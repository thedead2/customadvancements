package de.thedead2.customadvancements.advancementsmodifier;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.JsonElement;
import de.thedead2.customadvancements.CustomAdvancement;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.ResourceLocationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

import static de.thedead2.customadvancements.util.ModHelper.*;


public class CustomAdvancementManager {

    public static final Logger LOGGER = LogManager.getLogger();
    private static long counter = 0;
    private static final Multimap<ResourceLocation, ResourceLocation> parentChildrenMap = ArrayListMultimap.create();


    public static Map<ResourceLocation, JsonElement> injectData(Map<ResourceLocation, JsonElement> map, IResourceManager resourceManager) {

        if(!CUSTOM_ADVANCEMENTS.isEmpty()){
            LOGGER.debug("Starting to inject data of {} Custom Advancements into Advancement Manager!", CUSTOM_ADVANCEMENTS.size());
            LOGGER.debug("All registered Custom Advancements: " + CUSTOM_ADVANCEMENTS);

            try {
                for(CustomAdvancement customAdvancement:CUSTOM_ADVANCEMENTS){
                    ResourceLocation customResourceLocation = customAdvancement.getResourceLocation();
                    JsonElement customJsonElement = customAdvancement.getJsonObject();

                    try{
                        Collection<ResourceLocation> resourceLocations = resourceManager.getAllResourceLocations("advancements", (filename) -> filename.endsWith(".json"));

                        if(!resourceLocations.contains(customResourceLocation)){
                            ResourceLocation customResourceLocation1 = new ResourceLocation(customResourceLocation.getNamespace(), customResourceLocation.getPath().replace(".json", ""));

                            map.put(customResourceLocation1, customJsonElement);

                            LOGGER.debug("Injected " + customAdvancement.getFileName() + " into AdvancementManager!");
                        }
                        else {
                            LOGGER.error("The Resource Location " + customResourceLocation + " for " + customAdvancement.getFileName() + " already exists!");
                            LOGGER.debug("All registered Resource Locations of advancements: " + resourceLocations);

                            throw new ResourceLocationException("Duplicate Resource Location (" + customResourceLocation + ") for Custom Advancement: " + customAdvancement.getFileName());
                        }
                    }
                    catch (ResourceLocationException e){
                        LOGGER.error("Unable to register advancement {}! There is already an advancement with this Resource Location: " + customResourceLocation, customAdvancement.getFileName());
                        e.printStackTrace();
                    }

                }
                LOGGER.info("Injected {} Custom Advancements into Advancement Manager!", CUSTOM_ADVANCEMENTS.size());
            }
            catch(IndexOutOfBoundsException e){
                LOGGER.error("Something went wrong injecting Custom Advancements into Advancement Manager!");
                LOGGER.debug("Caught IndexOutOfBoundsException: " + e);
                e.printStackTrace();
            }
        }

        return map;
    }


    public static Map<ResourceLocation, JsonElement> removeRecipeAdvancements(Map<ResourceLocation, JsonElement> map, IResourceManager resourceManager){
        if(ConfigManager.NO_RECIPE_ADVANCEMENTS.get()){
            Collection<ResourceLocation> resourceLocations = resourceManager.getAllResourceLocations("advancements", (filename) -> filename.endsWith(".json"));

            LOGGER.info("Starting to remove recipe advancements...");
            int counter = 0;
            for (ResourceLocation resourceLocation:resourceLocations){
                if (resourceLocation.toString().contains("recipes")){
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
        }
        return map;
    }


    public static Map<ResourceLocation, JsonElement> removeBlacklistedAdvancements(Map<ResourceLocation, JsonElement> map){
        if(ADVANCEMENTS_BLACKLIST != null){
            LOGGER.info("Starting to remove blacklisted advancements...");

            getChildren(map);

            for(ResourceLocation blacklistedAdvancement:ADVANCEMENTS_BLACKLIST){
                map.remove(blacklistedAdvancement);
                LOGGER.debug("Removed advancement: " + blacklistedAdvancement);
                counter++;

                map = removeChildren(map, blacklistedAdvancement);
            }

            LOGGER.info("Removed {} Advancements", counter);
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


    private static Map<ResourceLocation, JsonElement> removeChildren(Map<ResourceLocation, JsonElement> map, ResourceLocation resourceLocationIn){
        for(ResourceLocation childAdvancement:parentChildrenMap.get(resourceLocationIn)){
            map.remove(childAdvancement);
            LOGGER.debug("Removed child advancement: " + childAdvancement);
            counter++;

            removeChildren(map, childAdvancement);
        }
        return map;
    }
}