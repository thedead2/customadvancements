package de.thedead2.customadvancements.advancements;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.thedead2.customadvancements.client.BackgroundType;
import de.thedead2.customadvancements.client.IBackgroundRenderer;
import de.thedead2.customadvancements.util.ResourceLocationHelper;
import de.thedead2.customadvancements.util.core.ModHelper;
import de.thedead2.customadvancements.util.io.LegacyConverter;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.io.IOException;


public class CustomAdvancement {

    private final JsonObject jsonObject;

    private final String fileName;

    private final ResourceLocation resourceLocation;

    @Nullable
    private final ResourceLocation parentAdvancement;

    @Nullable
    private final IBackgroundRenderer backgroundRenderer;


    public CustomAdvancement(JsonObject jsonObject, String fileName, String path) throws IOException {
        this.jsonObject = jsonObject;
        this.fileName = fileName;
        this.resourceLocation = ResourceLocationHelper.createIdFromPath(path);
        this.parentAdvancement = this.jsonObject.has("parent") ? new ResourceLocation(this.jsonObject.get("parent").getAsString() + ".json") : null;

        LegacyConverter.checkAndUpdate(this.resourceLocation, this.jsonObject);

        this.backgroundRenderer = createBackgroundRenderer();
    }


    private @Nullable IBackgroundRenderer createBackgroundRenderer() {
        final @Nullable IBackgroundRenderer backgroundRenderer;
        JsonObject display = this.jsonObject.getAsJsonObject("display");

        if (display == null || !display.has("background")) {
            return null;
        }

        JsonElement jsonElement = display.get("background");

        if (jsonElement.isJsonPrimitive()) {
            backgroundRenderer = BackgroundType.TEXTURE.createRenderer(jsonElement);
        }
        else {
            JsonObject background = jsonElement.getAsJsonObject();

            backgroundRenderer = BackgroundType.fromJson(background.get("type"), this.resourceLocation).createRenderer(background);

            //display.remove("background");
            display.addProperty("background", new ResourceLocation(ModHelper.MOD_ID, "fake_texture_location").toString());
        }

        return backgroundRenderer;
    }


    public JsonObject getJsonObject() {
        return this.jsonObject;
    }


    public String getFileName() {
        return this.fileName;
    }


    public ResourceLocation getResourceLocation() {
        return this.resourceLocation;
    }


    @Nullable
    public ResourceLocation getParent() {
        return this.parentAdvancement;
    }


    @Nullable
    public IBackgroundRenderer getBackgroundRenderer() {
        return backgroundRenderer;
    }


    @Override
    public String toString() {
        return "Custom Advancement: {fileName = " + this.fileName + ", resourceLocation = " + this.resourceLocation + ", parent = " + this.parentAdvancement + "}";
    }
}
