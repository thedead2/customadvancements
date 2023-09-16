package de.thedead2.customadvancements.util.handler;


import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import de.thedead2.customadvancements.advancements.advancementtypes.CustomAdvancement;
import de.thedead2.customadvancements.advancements.advancementtypes.GameAdvancement;
import de.thedead2.customadvancements.util.Timer;
import de.thedead2.customadvancements.util.core.CrashHandler;
import de.thedead2.customadvancements.util.core.FileHandler;
import joptsimple.internal.Strings;
import net.minecraft.util.GsonHelper;
import org.apache.logging.log4j.Level;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static de.thedead2.customadvancements.util.core.ModHelper.*;


public abstract class JsonHandler {
    public static void start() {
        FileHandler.readDirectory(DIR_PATH.toFile(), directory -> {
            Timer timer = new Timer();
            if(directory.getPath().contains(String.valueOf(DATA_PATH))){
                return;
            }

            LOGGER.debug("Starting to read json files in: " + directory.getPath());

            File[] fileList = directory.listFiles();

            assert fileList != null;
            for(File file : fileList) {
                timer.start();
                String fileName = file.getName();
                CrashHandler.getInstance().setActiveFile(file);

                try {
                    if (file.isFile() && fileName.endsWith(".json")) {
                        LOGGER.debug("Found file: " + fileName);

                        JsonObject jsonObject = getJsonObject(file);

                        if (isCorrectJsonFormat(jsonObject, file.toPath())) {
                            if (directory.getPath().contains(String.valueOf(CUSTOM_ADVANCEMENTS_PATH))){
                                CustomAdvancement customadvancement = new CustomAdvancement(jsonObject, fileName, file.getPath());
                                CrashHandler.getInstance().setActiveAdvancement(customadvancement);
                                CUSTOM_ADVANCEMENTS.put(customadvancement.getResourceLocation(), customadvancement);
                            }
                            else {
                                GameAdvancement gameAdvancement = new GameAdvancement(jsonObject, fileName, file.getPath());
                                CrashHandler.getInstance().setActiveAdvancement(gameAdvancement);
                                GAME_ADVANCEMENTS.put(gameAdvancement.getResourceLocation(), gameAdvancement);
                            }
                        }
                        else {
                            LOGGER.error(fileName + " does not match the required '.json' format!");
                        }
                    }
                    else if(file.isFile() && !fileName.equals("resource_locations.txt") && !fileName.endsWith(".png")) {
                        LOGGER.warn("File '" + fileName + "' is not a '.json' file, ignoring it!");
                    }
                }
                catch (NullPointerException e){
                    CrashHandler.getInstance().handleException("Failed to create JsonObject from file: " + fileName, e, Level.WARN);
                }

                if (timer.getTime() >= 500){
                    LOGGER.warn("Reading file {} took {} ms! Max. is 500 ms!",fileName, timer.getTime());
                    throw new RuntimeException("Reading a file took " + timer.getTime() + " ms! Max. is 500 ms!");
                }
                timer.stop();
            }
            CrashHandler.getInstance().setActiveAdvancement(null);
            CrashHandler.getInstance().setActiveFile(null);
        });
    }


    public static JsonObject getJsonObject(File file){
        final String fileName = file.getName();

        try{
            return GsonHelper.parse(new FileReader(file));
        }
        catch (FileNotFoundException e) {
            CrashHandler.getInstance().handleException("Couldn't find file " + fileName + "?!", e, Level.WARN);
        }
        catch (JsonParseException e){
            CrashHandler.getInstance().handleException("Error parsing " + fileName + " to JsonObject! Make sure you have the right syntax for '.json' files!", e, Level.ERROR);
        }
        return new JsonObject();
    }


    private static boolean isCorrectJsonFormat(@NotNull JsonObject json, Path path){
        if(path.toString().contains("recipes" + PATH_SEPARATOR)) {
            return true;
        }
        else {
            if(json.get("parent") != null && json.get("criteria") != null && json.get("display") != null){
                return true;
            }
            else if(json.get("parent") == null && json.get("display") != null){
                return json.get("display").getAsJsonObject().get("background") != null;
            }
            else {
                return false;
            }
        }
    }

    public static void removeNullFields(JsonElement jsonElement){
        if(isJsonNull(jsonElement)) throw new NullPointerException("Can't remove null fields from JsonElement that is null! -> " + jsonElement);

        if(jsonElement.isJsonObject()){
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            List<String> nullFields = new ArrayList<>();

            jsonObject.entrySet().forEach(entry -> {
                if(isJsonNull(entry.getValue())){
                    nullFields.add(entry.getKey());
                }
                else removeNullFields(entry.getValue());
            });

            nullFields.forEach(jsonObject::remove);
        }
        else if(jsonElement.isJsonArray()){
            JsonArray jsonArray = jsonElement.getAsJsonArray();
            jsonArray.forEach(jsonElement1 -> {
                if(isJsonNull(jsonElement1))
                    jsonArray.remove(jsonElement1);
                else removeNullFields(jsonElement1);
            });
        }
    }

    private static boolean isJsonNull(JsonElement jsonElement){
        return jsonElement.isJsonNull() || (jsonElement.isJsonPrimitive() && jsonElement.getAsString().equals("null"));
    }


    public static String formatJsonObject(JsonElement jsonElement) {
        StringBuilder stringBuilder = new StringBuilder();
        char[] chars = jsonElement.toString().toCharArray();
        int i = 0;
        for (int j = 0; j < chars.length; j++) {
            char c = chars[j];
            char previousChar = j - 1 < 0 ? c : chars[j - 1];
            char nextChar = j + 1 >= chars.length ? c : chars[j + 1];

            if (c == '{') {
                stringBuilder.append(c);
                if (nextChar != '}') {
                    i++;
                    stringBuilder.append('\n').append(Strings.repeat('\t', i));
                }
            } else if (c == '}') {
                if (previousChar != '{') {
                    i--;
                    stringBuilder.append("\n").append(Strings.repeat('\t', i));
                }
                stringBuilder.append(c);
                if (nextChar != ',' && nextChar != '\"' && nextChar != '\'' && nextChar != '}' && nextChar != ']') {
                    stringBuilder.append('\n').append(Strings.repeat('\t', i));
                }
            } else if (c == ',') {
                stringBuilder.append(c).append('\n').append(Strings.repeat('\t', i));
            } else if (c == '[' && (nextChar == '\"' || nextChar == '[')) {
                i++;
                stringBuilder.append(c).append('\n').append(Strings.repeat('\t', i));
            } else if (c == ']' && (previousChar == '\"' || previousChar == ']')) {
                i--;
                stringBuilder.append('\n').append(Strings.repeat('\t', i)).append(c);
            } else {
                stringBuilder.append(c);
            }
        }
        return stringBuilder.toString();
    }
}
