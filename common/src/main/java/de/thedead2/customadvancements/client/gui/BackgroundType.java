package de.thedead2.customadvancements.client.gui;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.thedead2.customadvancements.util.ModHelper;
import de.thedead2.mc_libs.gui.RenderUtils;
import de.thedead2.mc_libs.gui.misc.Area;
import de.thedead2.mc_libs.gui.misc.GradientColor;
import de.thedead2.mc_libs.gui.textures.TextureInfo;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;


public enum BackgroundType {

    NONE(jsonElement -> (guiGraphics, xMin, xMax, yMin, yMax, scrollX, scrollY) -> {}),

    IMAGE(jsonElement -> {
        TextureInfo textureInfo = TextureInfo.fromJson(jsonElement.getAsJsonObject());

        return (guiGraphics, xMin, xMax, yMin, yMax, scrollX, scrollY) -> RenderUtils.renderImage(guiGraphics, textureInfo, Area.withCorners(xMin, xMax, yMin, yMax, 0));
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

            for (int columns = -1; columns <= (xMax - xMin) / textureSize + 1; columns++) {
                for (int rows = -1; rows <= (yMax - yMin) / textureSize + 1; rows++) {
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

        return (guiGraphics, xMin, xMax, yMin, yMax, scrollX, scrollY) -> RenderUtils.linearGradient(guiGraphics, xMin, xMax, yMin, yMax, 0, degrees, gradientColors);
    }),

    RADIAL_GRADIENT(jsonElement -> {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        JsonArray colors = jsonObject.getAsJsonArray("colors");

        GradientColor[] gradientColors = new GradientColor[colors.size()];

        for (int i = 0; i < colors.size(); i++) {
            gradientColors[i] = GradientColor.fromJson(colors.get(i));
        }

        return (guiGraphics, xMin, xMax, yMin, yMax, scrollX, scrollY) -> RenderUtils.radialGradient(guiGraphics, xMin, xMax, yMin, yMax, 0, gradientColors);
    }),

    COLOR(jsonElement -> {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        JsonElement colorField = jsonObject.get("color");
        int color;

        if(colorField == null) throw new IllegalArgumentException("Missing 'color' field in 'background' object!");

        if (colorField.isJsonPrimitive()) {
            color = getColorFromString(colorField.getAsString());
        }
        else {
            JsonObject colorObj = colorField.getAsJsonObject();
            int red = colorObj.get("red").getAsInt();
            int green = colorObj.get("green").getAsInt();
            int blue = colorObj.get("blue").getAsInt();
            int alpha = colorObj.get("alpha").getAsInt();

            int[] colorData = new int[]{red, green, blue, alpha};

            color = RenderUtils.convertColor(colorData);
        }


        return (guiGraphics, xMin, xMax, yMin, yMax, scrollX, scrollY) -> guiGraphics.fill(xMin, yMin, xMax, yMax, color);
    });


    private final BackgroundRendererFactory rendererFactory;


    BackgroundType(BackgroundRendererFactory rendererFactory) {
        this.rendererFactory = rendererFactory;
    }


    public static @NotNull BackgroundType fromJson(@Nullable JsonElement type, ResourceLocation advancementId) {
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

    private static int getColorFromString(String s) {
        String[] strings = Strings.splitList(s);

        if(strings.length == 4) {
            int[] colors = new int[4];

            for (int i = 0; i < strings.length; i++) {
                colors[i] = Integer.parseInt(strings[i]);
            }

            return RenderUtils.convertColor(colors);
        }

        try {
            return Integer.parseInt(s);
        }
        catch (NumberFormatException e) {
            ModHelper.LOGGER.error("Invalid color format: {}", s);

            return 0;
        }
    }


    private interface BackgroundRendererFactory {

        IBackgroundRenderer create(JsonElement jsonElement);
    }
}