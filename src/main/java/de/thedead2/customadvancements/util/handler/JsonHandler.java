package de.thedead2.customadvancements.util.handler;


import com.google.common.io.ByteStreams;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import de.thedead2.customadvancements.advancements.advancementtypes.CustomAdvancement;
import de.thedead2.customadvancements.advancements.advancementtypes.GameAdvancement;
import de.thedead2.customadvancements.util.Timer;
import de.thedead2.customadvancements.util.exceptions.CrashHandler;
import joptsimple.internal.Strings;
import net.minecraft.ResourceLocationException;
import org.apache.logging.log4j.Level;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;


public class JsonHandler extends FileHandler {

    private static JsonHandler instance;

    private JsonHandler(File directory){
        super(directory);
        instance = this;
    }

    @Override
    public void readFiles(File directory) {
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

                    printFileDataToConsole(file);

                    JsonObject jsonObject = getJsonObject(file);

                    assert jsonObject != null;
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
                        throw new IllegalStateException("File does not match the required '.json' format!");
                    }
                }
                else if(file.isFile() && !fileName.equals("resource_locations.txt") && !fileName.endsWith(".png")) {
                    LOGGER.warn("File '" + fileName + "' is not a '.json' file, ignoring it!");
                }
            }
            catch (NullPointerException e){
                LOGGER.error("Unable to get JsonObject for: " + fileName);
                e.printStackTrace();
                CrashHandler.getInstance().addCrashDetails("Failed to create JsonObject from File!", Level.WARN , e);
            }
            catch (IllegalStateException e){
                LOGGER.error("Unable to create Advancement for: " + fileName);
                e.printStackTrace();
                CrashHandler.getInstance().addCrashDetails("File did not match the required '.json' format!", Level.DEBUG , e);
            }
            catch (ResourceLocationException e) {
                LOGGER.error("Unable to create Resource Location for: " + fileName);
                CrashHandler.getInstance().addCrashDetails("Unable to create resource location for file!", Level.WARN, e);
                e.printStackTrace();
            }

            if (timer.getTime() >= 500){
                LOGGER.warn("Reading file {} took {} ms! Max. is 500 ms!",fileName, timer.getTime());
                throw new RuntimeException("Reading a file took " + timer.getTime() + " ms! Max. is 500 ms!");
            }
            timer.stop();
        }
        CrashHandler.getInstance().setActiveAdvancement(null);
        CrashHandler.getInstance().setActiveFile(null);
    }


    private void printFileDataToConsole(File file){
        try {
            InputStream fileInput = Files.newInputStream(file.toPath());
            String file_data = new String(ByteStreams.toByteArray(fileInput), StandardCharsets.UTF_8);
            LOGGER.debug("\n" + file_data);
            fileInput.close();
        }
        catch (IOException e) {
            LOGGER.warn("Unable to read File by InputStream!");
            CrashHandler.getInstance().addCrashDetails("Unable to read File by InputStream!", Level.WARN, e);
            e.printStackTrace();
        }
    }


    public JsonObject getJsonObject(File file){
        final String fileName = file.getName();

        try{
            return (JsonObject) JsonParser.parseReader(new FileReader(file));
        }
        catch (FileNotFoundException e) {
            LOGGER.error("Unable to parse " + fileName + " to JsonObject: " + e);
            CrashHandler.getInstance().addCrashDetails("Couldn't find file " + fileName + "?!", Level.WARN, e);
            e.printStackTrace();
            return null;
        }
        catch (JsonParseException e){
            LOGGER.error("Error parsing {} to JsonObject! Make sure you have the right syntax for '.json' files!", fileName);
            CrashHandler.getInstance().addCrashDetails("Couldn't parse file to JsonElement!", Level.ERROR, e);
            e.printStackTrace();
            return null;
        }
        catch (ClassCastException e){
            LOGGER.error("Unable to get JsonObject for: " + fileName);
            e.printStackTrace();
            CrashHandler.getInstance().addCrashDetails("Failed to create JsonObject from File!", Level.WARN , e);
            return null;
        }
    }


    private boolean isCorrectJsonFormat(@NotNull JsonObject json, Path path){
        if(path.toString().contains("recipes" + PATH_SEPARATOR)) {
            LOGGER.debug("Ignored '.json' format for recipe advancement: " + path.getFileName());
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

    public static String formatJsonObject(JsonElement jsonElement){
        StringBuilder stringBuilder = new StringBuilder();
        char[] chars = jsonElement.toString().toCharArray();
        int i = 0;
        for (int j = 0; j < chars.length; j++) {
            char c = chars[j];
            char previousChar = j - 1 < 0 ? c : chars[j - 1];
            char nextChar = j + 1 >= chars.length ? c : chars[j + 1];

            if(c == '{'){
                stringBuilder.append(c);
                if(nextChar != '}'){
                    i++;
                    stringBuilder.append('\n').append(Strings.repeat('\t', i));
                }
            }
            else if (c == '}') {
                if(previousChar != '{'){
                    i--;
                    stringBuilder.append("\n").append(Strings.repeat('\t', i));
                }
                stringBuilder.append(c);
                if(nextChar != ',' && nextChar != '\"' && nextChar != '\'' && nextChar != '}' && nextChar != ']'){
                    stringBuilder.append('\n').append(Strings.repeat('\t', i));
                }
            }
            else if (c == ',') {
                stringBuilder.append(c).append('\n').append(Strings.repeat('\t', i));
            }
            else if (c == '[' && (nextChar == '\"' || nextChar == '[')) {
                i++;
                stringBuilder.append(c).append('\n').append(Strings.repeat('\t', i));
            }
            else if (c == ']' && (previousChar == '\"' || previousChar == ']')) {
                i--;
                stringBuilder.append('\n').append(Strings.repeat('\t', i)).append(c);
            }
            else {
                stringBuilder.append(c);
            }
        }
        return stringBuilder.toString();
    }

    public static String formatString(String string){
        StringBuilder stringBuilder = new StringBuilder();
        char[] chars = string.toCharArray();
        int i = 0;
        for (int j = 0; j < chars.length; j++) {
            char c = chars[j];
            char previousChar = j - 1 < 0 ? c : chars[j - 1];
            char nextChar = j + 1 >= chars.length ? c : chars[j + 1];

            if(c == '{'){
                stringBuilder.append(c);
                if(nextChar != '}'){
                    i++;
                    stringBuilder.append('\n').append(Strings.repeat('\t', i));
                }
            }
            else if (c == '}') {
                if(previousChar != '{'){
                    i--;
                    stringBuilder.append("\n").append(Strings.repeat('\t', i));
                }
                stringBuilder.append(c);
                if(nextChar != ',' && nextChar != '\"' && nextChar != '\'' && nextChar != '}' && nextChar != ']'){
                    stringBuilder.append('\n').append(Strings.repeat('\t', i));
                }
            }
            else if (c == ',') {
                stringBuilder.append(c).append('\n').append(Strings.repeat('\t', i));
            }
            else if (c == '[' && (nextChar == '\"' || nextChar == '[')) {
                i++;
                stringBuilder.append(c).append('\n').append(Strings.repeat('\t', i));
            }
            else if (c == ']' && (previousChar == '\"' || previousChar == ']')) {
                i--;
                stringBuilder.append('\n').append(Strings.repeat('\t', i)).append(c);
            }
            else {
                stringBuilder.append(c);
            }
        }
        return stringBuilder.toString();
    }

    public static JsonHandler getInstance(){return Objects.requireNonNullElseGet(instance, () -> new JsonHandler(DIR_PATH.toFile()));}
}
