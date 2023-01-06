package de.thedead2.customadvancements.advancements;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static de.thedead2.customadvancements.util.IFileHandler.getId;
import static de.thedead2.customadvancements.util.ModHelper.MOD_ID;
import static de.thedead2.customadvancements.util.ModHelper.TEXTURES;

public class CustomAdvancement implements IAdvancement {
    private final JsonObject jsonObject;
    private final String fileName;
    private final ResourceLocation resourceLocation;
    private final ResourceLocation parentAdvancement;
    private final boolean backgroundImage;
    private ResourceLocation textureLocation;

    private static final Logger LOGGER = LogManager.getLogger();


    public CustomAdvancement(JsonObject jsonObject, String fileName, String path){
        this.jsonObject = jsonObject;
        this.fileName = fileName;
        this.resourceLocation = IAdvancement.createResourceLocation(getId(path), this.fileName, false);
        this.backgroundImage = hasBackgroundImage();
        this.parentAdvancement = this.jsonObject.get("parent") != null ? IAdvancement.createResourceLocation((this.jsonObject.get("parent").getAsString() + ".json"), this.fileName, true) : null;
    }


    @Override
    public JsonObject getJsonObject(){
        return this.jsonObject;
    }

    @Override
    public String getFileName(){
        return this.fileName;
    }

    @Override
    public ResourceLocation getResourceLocation(){return this.resourceLocation;}

    @Override
    public ResourceLocation getParentAdvancement() {
        return this.parentAdvancement;
    }

    @Override
    public String toString(){
        return "Custom Advancement: {fileName = " + this.fileName + ", resourceLocation = " + this.resourceLocation + ", parent = " + this.parentAdvancement + ", hasBackgroundImage = " + this.backgroundImage + (this.backgroundImage ? (", textureLocation = " + this.textureLocation) : "") + " }";
    }

    private boolean hasBackgroundImage(){
        if(this.jsonObject.get("display").getAsJsonObject().get("background") != null) {
            ResourceLocation textureLocation = ResourceLocation.tryParse(this.jsonObject.get("display").getAsJsonObject().get("background").getAsString());

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
