package de.thedead2.customadvancements.advancements.advancementtypes;

import com.google.gson.JsonObject;
import de.thedead2.customadvancements.util.handler.FileHandler;
import net.minecraft.resources.ResourceLocation;

public class GameAdvancement implements IAdvancement{
    private final JsonObject jsonObject;
    private final String fileName;
    private final ResourceLocation resourceLocation;
    private final ResourceLocation parentAdvancement;


    public GameAdvancement(JsonObject jsonObject, String fileName, String path){
        this.jsonObject = jsonObject;
        this.fileName = fileName;
        this.resourceLocation = FileHandler.getId(path);
        this.parentAdvancement = this.jsonObject.get("parent") != null ? FileHandler.getId(this.jsonObject.get("parent").getAsString() + ".json") : null;
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
    public ResourceLocation getParentAdvancement(){return this.parentAdvancement;}

    @Override
    public String toString(){
        return "Game Advancement: {fileName = " + this.fileName + ", resourceLocation = " + this.resourceLocation + ", parent = " + this.parentAdvancement + "}";
    }
}
