package de.thedead2.customadvancements.util.handler;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.Criterion;
import net.minecraft.resources.ResourceLocation;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class AdvancementHandler extends FileHandler {

    private static final List<String> FOLDER_NAMES = new ArrayList<>();

    public AdvancementHandler(File directory) {
        super(directory);
    }

    public static void writeAdvancementToFile(ResourceLocation advancementId, JsonElement advancementData) throws IOException {
        LOGGER.debug("Generating file: " + advancementId);

        FOLDER_NAMES.clear();
        Path basePath = Path.of(String.valueOf(DIR_PATH), advancementId.getNamespace());

        createDirectory(basePath.toFile());

        writeFile(getInput(advancementData), resolvePath(basePath, advancementId.getPath()));
    }


    public static void writeAdvancementToFile(Advancement advancementIn) throws IOException {
        ResourceLocation advancementId = advancementIn.getId();
        JsonObject advancementData = /*serializeAdvancementToJson(*/advancementIn.deconstruct().serializeToJson();

        writeAdvancementToFile(advancementId, advancementData);
    }

    /*public static JsonObject serializeAdvancementToJson(Advancement.Builder builder){
        if (builder.requirements == null) {
            builder.requirements = builder.requirementsStrategy.createRequirements(builder.criteria.keySet());
        }

        JsonObject jsonobject = new JsonObject();
        if (builder.parentId != null) {
            jsonobject.addProperty("parent", builder.parentId.toString());
        }

        if (builder.display != null) {
            jsonobject.add("display", builder.display.serializeToJson());
        }

        if(builder.rewards != AdvancementRewards.EMPTY){
            jsonobject.add("rewards", builder.rewards.serializeToJson());
            JsonObject jsonobject1 = new JsonObject();

            for(Map.Entry<String, Criterion> entry : builder.criteria.entrySet()) {
                jsonobject1.add(entry.getKey(), entry.getValue().serializeToJson());
            }
            jsonobject.add("criteria", jsonobject1);
            JsonArray jsonarray1 = new JsonArray();
        }



        for(String[] astring : builder.requirements) {
            JsonArray jsonarray = new JsonArray();

            for(String s : astring) {
                jsonarray.add(s);
            }

            jsonarray1.add(jsonarray);
        }

        jsonobject.add("requirements", jsonarray1);
        return jsonobject;
    }*/


    private static void getSubDirectories(String pathIn){
        if (pathIn.contains("/")){
            String temp1 = pathIn.substring(pathIn.indexOf("/"));
            String temp2 = pathIn.replace(temp1 + "/", "");
            String temp3 = temp2.replace(temp2.substring(temp2.indexOf("/")), "");
            FOLDER_NAMES.add(temp3);

            String next = pathIn.replace((temp3 + "/"), "");
            getSubDirectories(next);
        }
    }


    private static Path resolvePath(Path basePath, String advancementPath){
        if(advancementPath.contains("/")){
            String subStringDirectory = advancementPath.replaceAll(advancementPath.substring(advancementPath.indexOf("/")), "");
            FOLDER_NAMES.add(subStringDirectory);

            String nextSubString = advancementPath.replace(subStringDirectory + "/", "");
            getSubDirectories(nextSubString);

            for(String folderName: FOLDER_NAMES){
                basePath = Path.of(String.valueOf(basePath), folderName);
                createDirectory(basePath.toFile());
            }

            return Path.of(String.valueOf(basePath), advancementPath.substring(advancementPath.lastIndexOf("/")) + ".json");
        }
        else {
            return Path.of(String.valueOf(basePath), advancementPath + ".json");
        }
    }


    private static InputStream getInput(JsonElement advancementData){
        StringBuilder stringBuilder = new StringBuilder();

        for (char c : advancementData.toString().toCharArray()){
            if (c == '{' || c == ',' || c == '['){
                stringBuilder.append(c).append('\n');
            }
            else {
                stringBuilder.append(c);
            }
        }
        String temp = stringBuilder.toString();

        return new ByteArrayInputStream(temp.getBytes());
    }
}
