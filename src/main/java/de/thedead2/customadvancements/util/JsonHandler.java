package de.thedead2.customadvancements.util;


import com.google.common.io.ByteStreams;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import de.thedead2.customadvancements.advancements.CustomAdvancement;
import de.thedead2.customadvancements.advancements.GameAdvancement;
import net.minecraft.ResourceLocationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static de.thedead2.customadvancements.util.ModHelper.*;


public class JsonHandler implements IFileHandler {

    private static final Logger LOGGER = LogManager.getLogger();

    @Override
    public void readFiles(File directory) {
        LOGGER.debug("Starting to read json files in: " + directory.getPath());

        File[] fileList = directory.listFiles();

        if (fileList == null){
            LOGGER.warn("Skipped directory {} as the fileList was null!", directory);
            return;
        }

        for(File file : fileList) {
            String fileName = file.getName();

            try {
                if (file.isFile() && fileName.endsWith(".json")) {
                    LOGGER.debug("Found file: " + fileName);

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

                    JsonObject jsonObject = getJsonObject(file);

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
                else if(file.isFile() && !fileName.equals("resource_locations.txt")) {
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
        }
    }


    private JsonObject getJsonObject(File file){
        final String fileName = file.getName();

        try{
            final Object object = JsonParser.parseReader(new FileReader(file));

            return (JsonObject) object;
        }
        catch (FileNotFoundException e) {
            LOGGER.error("Unable to parse " + fileName + " to JsonObject: " + e);
            e.printStackTrace();
            return null;
        }
        catch (ClassCastException e){
            LOGGER.error("Failed to cast {} to JsonObject! Maybe it's empty...", fileName);
            e.printStackTrace();
            return null;
        }
        catch (JsonSyntaxException e){
            LOGGER.error("Failed to read {}! Make sure you have the right syntax for '.json' files!", fileName);
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
