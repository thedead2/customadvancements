package de.thedead2.customadvancements;

import com.google.gson.JsonObject;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.ResourceLocationException;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static de.thedead2.customadvancements.util.ModHelper.MOD_ID;
import static de.thedead2.customadvancements.util.ModHelper.TEXTURES;

public class CustomAdvancement {
    private final JsonObject jsonObject;
    private final String fileName;
    private final ResourceLocation resourceLocation;
    private final String filePath;

    private static final Logger LOGGER = LogManager.getLogger();


    public CustomAdvancement(JsonObject jsonObject, String fileName, String filePath){
        this.jsonObject = jsonObject;
        this.fileName = fileName;
        this.filePath = filePath;
        this.resourceLocation = createResourceLocation(getId(this));
        hasBackgroundImage();
    }


    public JsonObject getJsonObject(){
        return this.jsonObject;
    }

    public String getFileName(){
        return this.fileName;
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

    public ResourceLocation getResourceLocation(){return this.resourceLocation;}

    public String getFilePath(){return this.filePath;}

    private String getId(CustomAdvancement customAdvancement) {
        return customAdvancement.getFilePath().substring(customAdvancement.getFilePath().lastIndexOf(MOD_ID)).replaceFirst("/", ":");
    }

    private void hasBackgroundImage(){
        boolean background;

        if(this.jsonObject.get("display").getAsJsonObject().get("background") != null) {
            ResourceLocation textureLocation = ResourceLocation.tryCreate(this.jsonObject.get("display").getAsJsonObject().get("background").getAsString());

            background = TEXTURES.get(textureLocation) != null;

            if(background) {
                LOGGER.debug("Found background for " + fileName);
            }
            else if(FMLEnvironment.dist.isClient()) {
                LOGGER.warn("Unable to locate background texture for custom advancement " + getFileName() + " with texture location: " + textureLocation + "! This will cause problems...");
            }
        }
    }
}
