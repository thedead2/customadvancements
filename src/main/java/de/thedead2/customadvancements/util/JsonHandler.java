package de.thedead2.customadvancements.util;


import com.google.common.io.ByteStreams;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import de.thedead2.customadvancements.CustomAdvancement;
import net.minecraft.util.ResourceLocationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static de.thedead2.customadvancements.util.ModHelper.*;


public class JsonHandler implements IFileHandler {

    private static final Logger LOGGER = LogManager.getLogger();


    public void readFiles(File directory) {

        File[] fileList = directory.listFiles();

        assert fileList != null;
        for(File file : fileList) {
            String fileName = file.getName();

            try {
                if (file.isFile() && fileName.endsWith(".json")) {
                    LOGGER.debug("Found file: " + fileName);

                    try {
                        InputStream fileInput = Files.newInputStream(file.toPath());
                        String filedata = new String(ByteStreams.toByteArray(fileInput), StandardCharsets.UTF_8);
                        LOGGER.debug(fileName + ":\n" + filedata);
                        fileInput.close();
                    }
                    catch (IOException e) {
                        LOGGER.debug("Failed to read File by InputStream!");
                        e.printStackTrace();
                    }

                    JsonObject jsonObject = getJson(file);

                    assert jsonObject != null;
                    if (isCorrectJsonFormat(jsonObject)) {
                        CustomAdvancement customadvancement = new CustomAdvancement(jsonObject, fileName, file.getAbsolutePath());

                        CUSTOM_ADVANCEMENTS.add(customadvancement);
                    }
                    else {
                        LOGGER.error(fileName + " does not match the required Json Format!");
                        throw new IllegalStateException("File does not match the required Json Format!");
                    }
                    FileHandler.file_counter++;
                }
                else if(file.isFile()) {
                    LOGGER.warn("File '" + file.getName() + "' is not a .json file, ignoring it!");
                }
            }
            catch (IllegalStateException e){
                LOGGER.error("Unable to create Custom Advancement for " + fileName);
                e.printStackTrace();
            }
            catch (ResourceLocationException e) {
                LOGGER.error("Unable to create Resource Location for Custom Advancement: " + fileName);
                e.printStackTrace();
            }
            catch (Exception e) {
                LOGGER.fatal("Something went wrong: " + e);
                throw new RuntimeException(e);
            }
        }
    }


    private JsonObject getJson(File file){
        JsonParser parser = new JsonParser();

        try{
            Object obj = parser.parse(new FileReader(file));
            JsonObject file_to_Json = (JsonObject) obj;

            LOGGER.debug("Parsed " + file.getName() + " to JsonObject!");

            return file_to_Json;
        }
        catch (FileNotFoundException e) {
            LOGGER.error("Unable to parse " + file.getName() + " to JsonObject: " + e);

            return null;
        }
        catch (ClassCastException e){
            LOGGER.error("Failed to cast {} to JsonObject as it is empty!", file.getName());
            e.printStackTrace();

            return null;
        }
        catch (JsonSyntaxException e){
            LOGGER.error("Failed to read {}! Make sure to have the right Syntax for Json Files!", file.getName());
            e.printStackTrace();

            return null;
        }
    }

    private boolean isCorrectJsonFormat(JsonObject json){
        return json.get("parent") != null && json.get("criteria") != null && json.get("display") != null || json.get("parent") == null && json.get("display").getAsJsonObject().get("background") != null;
    }
}
