package de.thedead2.customadvancements.util.handler;


import com.google.common.io.ByteStreams;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import de.thedead2.customadvancements.advancements.advancementtypes.CustomAdvancement;
import de.thedead2.customadvancements.advancements.advancementtypes.GameAdvancement;
import net.minecraft.ResourceLocationException;
import org.apache.commons.lang3.time.StopWatch;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;


public class JsonHandler extends FileHandler {

    public JsonHandler(){
        this.init(new File(DIR_PATH));
    }

    @Override
    public void readFiles(File directory) {
        StopWatch timer = new StopWatch();
        if(directory.getPath().contains(TEXTURES_PATH)){
            return;
        }

        LOGGER.debug("Starting to read json files in: " + directory.getPath());

        File[] fileList = directory.listFiles();

        assert fileList != null;
        for(File file : fileList) {
            timer.start();
            String fileName = file.getName();

            try {
                if (file.isFile() && fileName.endsWith(".json")) {
                    LOGGER.debug("Found file: " + fileName);

                    printFileDataToConsole(file);

                    JsonObject jsonObject = (JsonObject) getJsonObject(file);

                    assert jsonObject != null;
                    if (isCorrectJsonFormat(jsonObject, file.toPath())) {
                        if (directory.getPath().contains(CUSTOM_ADVANCEMENTS_PATH)){
                            CustomAdvancement customadvancement = new CustomAdvancement(jsonObject, fileName, file.getPath());

                            CUSTOM_ADVANCEMENTS.put(customadvancement.getResourceLocation(), customadvancement);
                        }
                        else {
                            GameAdvancement gameAdvancement = new GameAdvancement(jsonObject, fileName, file.getPath());

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
            }
            catch (IllegalStateException e){
                LOGGER.error("Unable to create Advancement for: " + fileName);
                e.printStackTrace();
            }
            catch (ResourceLocationException e) {
                LOGGER.error("Unable to create Resource Location for: " + fileName);
                e.printStackTrace();
            }
            catch (Exception e) {
                LOGGER.fatal("Something went wrong: " + e);
                throw new RuntimeException(e);
            }

            if (timer.getTime() >= 500){
                LOGGER.warn("Reading file {} took {} ms! Max. is 500 ms!",fileName, timer.getTime());
                throw new RuntimeException("Reading one single file took over 500 ms!");
            }
            timer.stop();
            timer.reset();
        }
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
            e.printStackTrace();
        }
    }


    private JsonElement getJsonObject(File file){
        final String fileName = file.getName();

        try{
            return JsonParser.parseReader(new FileReader(file));
        }
        catch (FileNotFoundException e) {
            LOGGER.error("Unable to parse " + fileName + " to JsonObject: " + e);
            e.printStackTrace();
            return null;
        }
        catch (JsonParseException e){
            LOGGER.error("Error parsing {} to JsonObject! Make sure you have the right syntax for 'Json' files!", fileName);
            e.printStackTrace();
            return null;
        }
    }


    private boolean isCorrectJsonFormat(JsonObject json, Path path){
        if(path.toString().contains("recipes/")){
            LOGGER.debug("Ignored '.json' format for recipe advancement: " + path.getFileName());
            return true;
        }
        else {
            return (json.get("parent") != null && json.get("criteria") != null && json.get("display") != null) || (json.get("parent") == null && json.get("display").getAsJsonObject().get("background") != null);
        }
    }
}
