package de.thedead2.customadvancements;

import com.google.gson.JsonObject;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.ResourceLocationException;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static de.thedead2.customadvancements.util.IFileHandler.getId;
import static de.thedead2.customadvancements.util.ModHelper.MOD_ID;
import static de.thedead2.customadvancements.util.ModHelper.TEXTURES;

public class CustomAdvancement {
    private final JsonObject jsonObject;
    private final String fileName;
    private final ResourceLocation resourceLocation;
    private final boolean backgroundImage;
    private ResourceLocation textureLocation;

    private static final Logger LOGGER = LogManager.getLogger();


    public CustomAdvancement(JsonObject jsonObject, String fileName, String path){
        this.jsonObject = jsonObject;
        this.fileName = fileName;
        this.resourceLocation = createResourceLocation(getId(path));
        this.backgroundImage = hasBackgroundImage();
    }


    public JsonObject getJsonObject(){
        return this.jsonObject;
    }

    public String getFileName(){
        return this.fileName;
    }

    public ResourceLocation getResourceLocation(){return this.resourceLocation;}


    public String toString(){
        return "Custom Advancement: {fileName = " + this.fileName + ", resourceLocation = " + this.resourceLocation + ", hasBackgroundImage = " + this.backgroundImage + (this.backgroundImage ? (", textureLocation = " + this.textureLocation) : "") + " }";
    }


    private ResourceLocation createResourceLocation(String id){
        ResourceLocation resourceLocation1 = ResourceLocation.tryCreate(id);
        LOGGER.debug("Resource Location for " + fileName + ": " + resourceLocation1);
        if(resourceLocation1 == null){
            LOGGER.error("Unable to create Resource Location. Probably the Name of the file contains illegal characters!");
            throw new ResourceLocationException("Could not create Resource Location for " + fileName + "!");
        }
        return resourceLocation1;
    }


    private boolean hasBackgroundImage(){
        if(this.jsonObject.get("display").getAsJsonObject().get("background") != null) {
            ResourceLocation textureLocation = ResourceLocation.tryCreate(this.jsonObject.get("display").getAsJsonObject().get("background").getAsString());

            this.textureLocation = textureLocation;
            assert textureLocation != null;
            boolean backgroundImage_in_map = !textureLocation.getNamespace().equals(MOD_ID) || TEXTURES.get(textureLocation) != null;

            if(backgroundImage_in_map) {
                LOGGER.debug("Found background for " + fileName);
            }
            else if(FMLEnvironment.dist.isClient()) {
                LOGGER.warn("Unable to locate background texture for custom advancement " + this.fileName + " with texture location: " + textureLocation + "! This will cause problems...");
            }
            return true;
        }
        else {
            return false;
        }
    }
}
