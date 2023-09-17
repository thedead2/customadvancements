package de.thedead2.customadvancements.advancements.advancementtypes;

import com.google.gson.JsonObject;
import net.minecraft.util.ResourceLocation;

public interface IAdvancement {

    JsonObject getJsonObject();

    String getFileName();

    ResourceLocation getResourceLocation();

    ResourceLocation getParentAdvancement();

    String toString();
}
