package de.thedead2.customadvancements.advancements;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.thedead2.customadvancements.util.ModHelper;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;


public class CustomAdvancement {

    private final JsonObject jsonObject;

    private final ResourceLocation id;

    @Nullable
    private final JsonElement backgroundInfo;


    public CustomAdvancement(ResourceLocation id, JsonObject jsonObject) {
        this.jsonObject = jsonObject;
        this.id = id;
        this.backgroundInfo = extractBackgroundInfo();
    }


    private @Nullable JsonElement extractBackgroundInfo() {
        JsonObject display = this.jsonObject.getAsJsonObject("display");

        if (display == null || !display.has("background")) {
            return null;
        }

        JsonElement jsonElement = display.get("background");
        display.addProperty("background", ResourceLocation.tryBuild(ModHelper.MOD_ID, "fake_texture_location").toString());

        return jsonElement;
    }


    public JsonObject getJsonObject() {
        return this.jsonObject;
    }


    public ResourceLocation getId() {
        return this.id;
    }


    @Nullable
    public JsonElement getBackgroundInfo() {
        return this.backgroundInfo;
    }


    @Override
    public String toString() {
        return "Custom Advancement: {id = " + this.id + "}";
    }
}
