package de.thedead2.customadvancements.advancementsmodifier;

import com.google.gson.JsonElement;
import de.thedead2.customadvancements.CustomAdvancement;
import de.thedead2.customadvancements.util.FileHandler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.ResourceLocationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class CustomAdvancementManager {
    private static final Logger LOGGER = LogManager.getLogger();

    public static Map<ResourceLocation, JsonElement> injectData(Map<ResourceLocation, JsonElement> map, IResourceManager resourceManager) {
        Set<CustomAdvancement> customadvancements = FileHandler.customadvancements;

        if(!customadvancements.isEmpty()){
            LOGGER.debug("Starting to inject data of {} Custom Advancements into Advancement Manager!", customadvancements.size());
            LOGGER.debug("All registered Custom Advancements: " + customadvancements);

            try {
                for(CustomAdvancement customAdvancement:customadvancements){
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
                LOGGER.info("Injected {} Custom Advancements into Advancement Manager!", customadvancements.size());
            }
            catch(IndexOutOfBoundsException e){
                LOGGER.error("Something went wrong injecting Custom Advancements into Advancement Manager!");
                LOGGER.debug("Catched IndexOutOfBoundsException: " + e);
                e.printStackTrace();
            }
        }

        return map;
    }

}
