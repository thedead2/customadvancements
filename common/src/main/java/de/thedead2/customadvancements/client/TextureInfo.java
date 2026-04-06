package de.thedead2.customadvancements.client;

import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;


public class TextureInfo {

    /**
     * The location for the texture
     */
    private final ResourceLocation textureLocation;

    private final Component altText;

    /**
     * The u start position inside the texture file in percent
     */
    private final float uMin;

    /**
     * The v start position inside the texture file in percent
     */
    private final float vMin;

    /**
     * The u end position inside the texture file in percent
     */
    private final float uMax;

    /**
     * The v end position inside the texture file in percent
     */
    private final float vMax;

    /**
     * The width of the texture in pixels
     */
    private final float textureWidth;

    /**
     * The height of the texture in pixels
     */
    private final float textureHeight;

    /**
     * The aspect ratio of the texture
     */
    private final float aspectRatio;

    private final ObjectFit objectFit;

    private final float[] colorShift = new float[] {1.0f, 1.0f, 1.0f, 1.0f};


    public TextureInfo(ResourceLocation textureLocation, Component altText, ObjectFit objectFit) {
        this(textureLocation, altText, 0, 0, 1, 1, objectFit);
    }


    public TextureInfo(ResourceLocation textureLocation, Component altText, float uMin, float vMin, float uMax, float vMax, ObjectFit objectFit) {
        this.textureLocation = textureLocation;
        this.altText = altText;
        this.uMin = uMin;
        this.vMin = vMin;
        this.uMax = uMax;
        this.vMax = vMax;
        this.objectFit = objectFit;

        try {
            Resource resource = Minecraft.getInstance().getResourceManager().getResourceOrThrow(this.textureLocation);

            try (InputStream inputstream = resource.open(); NativeImage nativeimage = NativeImage.read(inputstream)) {
                this.textureWidth = nativeimage.getWidth();
                this.textureHeight = nativeimage.getHeight();
            }
        }
        catch (FileNotFoundException e) {
            throw new RuntimeException("Unknown texture with id: " + this.textureLocation, e);
        }
        catch (IOException e) {
            throw new RuntimeException("Couldn't read texture with id: " + this.textureLocation, e);
        }

        this.aspectRatio = textureWidth / textureHeight;
    }


    public static TextureInfo fromJson(JsonObject jsonObject) {
        ResourceLocation textureLocation = Objects.requireNonNullElse(ResourceLocation.tryParse(jsonObject.get("location").getAsString()), TextureManager.INTENTIONAL_MISSING_TEXTURE);
        Component altText = Component.empty();
        float uMin = jsonObject.has("u_min") ? jsonObject.get("u_min").getAsFloat() : 0;
        float vMin = jsonObject.has("v_min") ? jsonObject.get("v_min").getAsFloat() : 0;
        float uMax = jsonObject.has("u_max") ? jsonObject.get("u_max").getAsFloat() : 1;
        float vMax = jsonObject.has("v_max") ? jsonObject.get("v_max").getAsFloat() : 1;
        ObjectFit objectFit = jsonObject.has("object_fit") ? ObjectFit.valueOf(jsonObject.get("object_fit").getAsString()) : ObjectFit.FILL;

        return new TextureInfo(textureLocation, altText, uMin, vMin, uMax, vMax, objectFit);
    }


    public ResourceLocation getTextureLocation() {
        return textureLocation;
    }


    public float getUMin() {
        return uMin;
    }


    public float getVMin() {
        return vMin;
    }


    public float getUMax() {
        return uMax;
    }


    public float getVMax() {
        return vMax;
    }


    public float getTextureWidth() {
        return textureWidth;
    }


    public float getTextureHeight() {
        return textureHeight;
    }


    public float getAspectRatio() {
        return aspectRatio;
    }


    public ObjectFit getObjectFit() {
        return objectFit;
    }


    /**
     * @return the relative width of the texture to the given height
     **/
    public float getRelativeWidth(float height) {
        return height * aspectRatio;
    }


    /**
     * @return the relative height of the texture to the given width
     **/
    public float getRelativeHeight(float width) {
        return width / aspectRatio;
    }


    public float[] getColorShift() {
        return colorShift;
    }


    public Component getAltText() {
        return this.altText;
    }
}
