package de.thedead2.customadvancements.advancements.advancementtypes;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;

public interface IAdvancement {

    JsonObject getJsonObject();

    String getFileName();

    ResourceLocation getResourceLocation();

    ResourceLocation getParentAdvancement();

    String toString();
}
