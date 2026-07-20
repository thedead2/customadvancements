package de.thedead2.customadvancements.util.core;


import de.thedead2.customadvancements.util.exceptions.ExceptionHandler;
import org.slf4j.event.Level;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


public class ModProperties extends Properties {

    private ModProperties() {
    }


    public static ModProperties fromInputStream(InputStream inputStream) {
        ModProperties properties = new ModProperties();

        try {
            properties.load(inputStream);
        }
        catch (IOException e) {
            ExceptionHandler.getInstance().log("IOException while loading ModProperties", e, Level.ERROR);
        }

        return properties;
    }
}
