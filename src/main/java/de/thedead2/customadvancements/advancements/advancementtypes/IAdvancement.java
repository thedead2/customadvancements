package de.thedead2.customadvancements.advancements.advancementtypes;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;


public interface IAdvancement {

    JsonObject getJsonObject();

    String getFileName();

    ResourceLocation getResourceLocation();

    @Nullable
    ResourceLocation getParentAdvancement();

    boolean hasLargeBackground();
    boolean shouldBackgroundClip();
    float getBackgroundAspectRatio();

    String toString();
}
