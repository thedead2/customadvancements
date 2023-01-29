package de.thedead2.customadvancements.util.handler;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.advancements.Advancement;
import net.minecraft.resources.ResourceLocation;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

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
        JsonObject advancementData = advancementIn.deconstruct().serializeToJson();

        writeAdvancementToFile(advancementId, advancementData);
    }


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
