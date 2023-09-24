package de.thedead2.customadvancements.advancements.advancementtypes;

import com.google.gson.JsonObject;
import de.thedead2.customadvancements.util.core.FileHandler;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;


public class GameAdvancement implements IAdvancement{
    private final JsonObject jsonObject;
    private final String fileName;
    private final ResourceLocation resourceLocation;
    @Nullable
    private final ResourceLocation parentAdvancement;

    public GameAdvancement(JsonObject jsonObject, String fileName, String path){
        this.jsonObject = jsonObject;
        this.fileName = fileName;
        this.resourceLocation = FileHandler.getId(path);
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
    @Nullable
    public ResourceLocation getParentAdvancement(){return this.parentAdvancement;}


    @Override
    public boolean hasLargeBackground() {
        JsonObject display = jsonObject.get("display").getAsJsonObject();
        if(display.has("largeBackground")){
            return display.get("largeBackground").getAsBoolean();
        }
        return false;
    }


    @Override
    public boolean shouldBackgroundClip() {
        JsonObject display = this.jsonObject.get("display").getAsJsonObject();
        if(display.has("shouldBgClip")){
            return display.get("shouldBgClip").getAsBoolean();
        }
        return false;
    }


    @Override
    public float getBackgroundAspectRatio() {
        JsonObject display = this.jsonObject.get("display").getAsJsonObject();
        if(display.has("bgRatio")){
            return display.get("bgRatio").getAsFloat();
        }
        return 1f;
    }


    @Override
    public String toString(){
        return "Game Advancement: {fileName = " + this.fileName + ", resourceLocation = " + this.resourceLocation + ", parent = " + this.parentAdvancement + "}";
    }
}
