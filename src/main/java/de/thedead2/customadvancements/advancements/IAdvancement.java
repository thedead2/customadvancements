package de.thedead2.customadvancements.advancements;

import com.google.gson.JsonObject;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.ResourceLocationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public interface IAdvancement {

    Logger LOGGER = LogManager.getLogger();

    JsonObject getJsonObject();

    String getFileName();

    ResourceLocation getResourceLocation();

    ResourceLocation getParentAdvancement();

    String toString();

    static ResourceLocation createResourceLocation(String id, String fileName, boolean parent){
        ResourceLocation resourceLocation1 = ResourceLocation.tryCreate(id);
        if(!parent){
            LOGGER.debug("Resource Location for " + fileName + ": " + resourceLocation1);
        }
        else {
            LOGGER.debug("Parent Resource Location for " + fileName + ": " + resourceLocation1);
        }

        if(resourceLocation1 == null){
            if(!parent){
                LOGGER.error("Unable to create resource location. Probably the Name of the file contains illegal characters!");
                throw new ResourceLocationException("Could not create resource location for " + fileName + "!");
            }
            else {
                LOGGER.error("Unable to create Parent Resource Location!");
                throw new ResourceLocationException("Could not create parent resource location for " + fileName + "!");
            }
        }
        return resourceLocation1;
    }
}
