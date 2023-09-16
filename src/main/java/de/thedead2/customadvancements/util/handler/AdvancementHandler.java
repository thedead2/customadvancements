package de.thedead2.customadvancements.util.handler;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.advancements.Advancement;
import net.minecraft.resources.ResourceLocation;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static de.thedead2.customadvancements.util.core.FileHandler.createDirectory;
import static de.thedead2.customadvancements.util.core.FileHandler.writeFile;
import static de.thedead2.customadvancements.util.core.ModHelper.DIR_PATH;
import static de.thedead2.customadvancements.util.core.ModHelper.LOGGER;

public abstract class AdvancementHandler {

    private static final List<String> FOLDER_NAMES = new ArrayList<>();
    public static boolean grantingAllAdvancements = false;

    public static void writeAdvancementToFile(ResourceLocation advancementId, JsonElement advancementData) throws IOException {
        LOGGER.debug("Generating file: " + advancementId);

        FOLDER_NAMES.clear();
        Path basePath = Path.of(String.valueOf(DIR_PATH), advancementId.getNamespace());

        createDirectory(basePath.toFile());

        writeFile(new ByteArrayInputStream(JsonHandler.formatJsonObject(advancementData).getBytes()), resolvePath(basePath, advancementId.getPath()));
    }


    public static void writeAdvancementToFile(Advancement advancementIn) throws IOException {
        writeAdvancementToFile(advancementIn.getId(), serializeToJson(advancementIn));
    }

    public static JsonObject serializeToJson(Advancement advancementIn){
        JsonObject jsonObject = advancementIn.deconstruct().serializeToJson();
        JsonHandler.removeNullFields(jsonObject);
        return jsonObject;
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
}
