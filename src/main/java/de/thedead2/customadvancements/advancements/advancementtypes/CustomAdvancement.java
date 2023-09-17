package de.thedead2.customadvancements.advancements.advancementtypes;

import com.google.gson.JsonObject;
import de.thedead2.customadvancements.util.ResourceManagerExtender;
import de.thedead2.customadvancements.util.core.FileHandler;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.loading.FMLEnvironment;

import static de.thedead2.customadvancements.util.core.ModHelper.LOGGER;
import static de.thedead2.customadvancements.util.core.ModHelper.MOD_ID;

public class CustomAdvancement implements IAdvancement {
    private final JsonObject jsonObject;
    private final String fileName;
    private final ResourceLocation resourceLocation;
    private final ResourceLocation parentAdvancement;
    private final boolean backgroundImage;
    private ResourceLocation textureLocation;


    public CustomAdvancement(JsonObject jsonObject, String fileName, String path){
        this.jsonObject = jsonObject;
        this.fileName = fileName;
        this.resourceLocation = FileHandler.getId(path);
        this.backgroundImage = hasBackgroundImage();
        this.parentAdvancement = this.jsonObject.get("parent") != null ? FileHandler.getId(this.jsonObject.get("parent").getAsString() + ".json", true) : null;
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
        return "Custom Advancement: {fileName = " + this.fileName + ", resourceLocation = " + this.resourceLocation + ", parent = " + this.parentAdvancement + ", hasBackgroundImage = " + this.backgroundImage + (this.backgroundImage ? (", textureLocation = " + this.textureLocation) : "") + "}";
    }

    private boolean hasBackgroundImage(){
        if(this.jsonObject.get("display").getAsJsonObject().get("background") != null) {
            ResourceLocation textureLocation = ResourceLocation.tryParse(this.jsonObject.get("display").getAsJsonObject().get("background").getAsString());

            this.textureLocation = textureLocation;
            assert textureLocation != null;
            boolean backgroundImage_in_map = !textureLocation.getNamespace().equals(MOD_ID) || ResourceManagerExtender.getResource(textureLocation) != null;

            if(!backgroundImage_in_map && FMLEnvironment.dist.isClient()) {
                LOGGER.warn("Unable to locate background texture for advancement " + this.fileName + " with texture location: " + textureLocation + "! This will cause problems...");
            }
            return true;
        }
        else {
            return false;
        }
    }
}
