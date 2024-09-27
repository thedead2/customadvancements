package de.thedead2.customadvancements.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.thedead2.customadvancements.util.core.ModHelper;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.util.Strings;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;


public enum BackgroundType {

    NONE(jsonElement -> (guiGraphics, xMin, xMax, yMin, yMax, scrollX, scrollY) -> {}),

    IMAGE(jsonElement -> {
        TextureInfo textureInfo = TextureInfo.fromJson(jsonElement.getAsJsonObject());

        return (guiGraphics, xMin, xMax, yMin, yMax, scrollX, scrollY) -> RenderUtil.renderImage(guiGraphics, textureInfo, Area.withCorners(xMin, xMax, yMin, yMax, 0), new float[] {1, 1, 1, 1});
    }),

    TEXTURE(jsonElement -> {
        ResourceLocation resourcelocation;

        if (jsonElement.isJsonObject()) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            resourcelocation = Objects.requireNonNullElse(ResourceLocation.tryParse(jsonObject.get("location").getAsString()), TextureManager.INTENTIONAL_MISSING_TEXTURE);
        }
        else {
            resourcelocation = Objects.requireNonNullElse(ResourceLocation.tryParse(jsonElement.getAsString()), TextureManager.INTENTIONAL_MISSING_TEXTURE);
        }

        return (guiGraphics, xMin, xMax, yMin, yMax, scrollX, scrollY) -> {
            final int textureSize = 16;
            int k = scrollX % textureSize;
            int l = scrollY % textureSize;

            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate((float) xMin, (float) yMin, 0.0F);

            for (int columns = -1; columns < (xMax - xMin) / textureSize; ++columns) {
                for (int rows = 0; rows <= (yMax - yMin) / textureSize; ++rows) {
                    guiGraphics.blit(resourcelocation, k + textureSize * columns, l + textureSize * rows, 0.0F, 0.0F, textureSize, textureSize, textureSize, textureSize);
                }
            }

            guiGraphics.pose().popPose();
        };
    }),

    LINEAR_GRADIENT(jsonElement -> {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        float degrees = jsonObject.has("degrees") ? jsonObject.get("degrees").getAsFloat() : 0;
        JsonArray colors = jsonObject.getAsJsonArray("colors");

        GradientColor[] gradientColors = new GradientColor[colors.size()];

        for (int i = 0; i < colors.size(); i++) {
            gradientColors[i] = GradientColor.fromJson(colors.get(i));
        }

        return (guiGraphics, xMin, xMax, yMin, yMax, scrollX, scrollY) -> RenderUtil.linearGradient(guiGraphics, xMin, xMax, yMin, yMax, 0, degrees, gradientColors);
    }),

    RADIAL_GRADIENT(jsonElement -> {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        JsonArray colors = jsonObject.getAsJsonArray("colors");

        GradientColor[] gradientColors = new GradientColor[colors.size()];

        for (int i = 0; i < colors.size(); i++) {
            gradientColors[i] = GradientColor.fromJson(colors.get(i));
        }

        return (guiGraphics, xMin, xMax, yMin, yMax, scrollX, scrollY) -> RenderUtil.radialGradient(guiGraphics, xMin, xMax, yMin, yMax, 0, gradientColors);
    }),

    COLOR(jsonElement -> {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        String s = jsonObject.get("color").getAsString();
        String[] strings = Strings.splitList(s);
        int[] colors = new int[4];

        for (int i = 0; i < strings.length; i++) {
            colors[i] = Integer.parseInt(strings[i]);
        }

        int color = RenderUtil.convertColor(colors);

        return (guiGraphics, xMin, xMax, yMin, yMax, scrollX, scrollY) -> guiGraphics.fill(xMin, yMin, xMax, yMax, color);
    });


    private final BackgroundRendererFactory rendererFactory;


    BackgroundType(BackgroundRendererFactory rendererFactory) {
        this.rendererFactory = rendererFactory;
    }


    public static @Nonnull BackgroundType fromJson(@Nullable JsonElement type, ResourceLocation advancementId) {
        if(type == null || type.isJsonNull()) {
            ModHelper.LOGGER.error("'background' field of advancement '{}' is missing the 'type' field for specifying the background type! Possible values are: {}", advancementId, values());

            return NONE;
        }
        else if (!type.isJsonPrimitive()) {
            ModHelper.LOGGER.error("Expected 'type' field of advancement '{}' to be a String got a {}", advancementId, type.getClass().getName());

            return NONE;
        }
        else {
            String typeName = type.getAsString();

            try {
                return valueOf(typeName);
            }
            catch (IllegalArgumentException ignored) {
                ModHelper.LOGGER.error("Unknown background type '{}' for advancement '{}'!", typeName, advancementId);

                return NONE;
            }
        }
    }


    public IBackgroundRenderer createRenderer(JsonElement jsonElement) {
        return rendererFactory.create(jsonElement);
    }


    private interface BackgroundRendererFactory {

        IBackgroundRenderer create(JsonElement jsonElement);
    }
}
