package de.thedead2.customadvancements.util.core;


import org.apache.logging.log4j.Level;

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
            CrashHandler.getInstance().handleException("IOException while loading ModProperties", e, Level.ERROR);
        }

        return properties;
    }
}
