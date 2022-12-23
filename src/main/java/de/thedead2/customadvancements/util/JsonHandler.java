package de.thedead2.customadvancements.util;


import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;


public class JsonHandler {

    private static final Logger LOGGER = LogManager.getLogger();


    protected JsonObject getJson(File file){
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

    protected boolean isCorrectJsonFormat(JsonObject json){
        return json.get("parent") != null && json.get("criteria") != null && json.get("display") != null || json.get("parent") == null && json.get("display") != null;
    }
}
