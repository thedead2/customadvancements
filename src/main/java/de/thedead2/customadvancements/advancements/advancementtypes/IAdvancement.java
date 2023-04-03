package de.thedead2.customadvancements.advancements.advancementtypes;

import com.google.gson.JsonObject;
import de.thedead2.customadvancements.util.handler.FileHandler;
import net.minecraft.resources.ResourceLocation;

import static de.thedead2.customadvancements.util.ModHelper.LOGGER;

public interface IAdvancement {

    JsonObject getJsonObject();

    String getFileName();

    ResourceLocation getResourceLocation();

    ResourceLocation getParentAdvancement();

    String toString();


    static ResourceLocation createResourceLocation(String id, String fileName, boolean parent){
        ResourceLocation resourceLocation1 = FileHandler.getId(id);
        if(!parent){
            LOGGER.debug("Resource Location for " + fileName + ": " + resourceLocation1);
        }
        else {
            LOGGER.debug("Parent Resource Location for " + fileName + ": " + resourceLocation1);
        }

        return resourceLocation1;
    }
}
