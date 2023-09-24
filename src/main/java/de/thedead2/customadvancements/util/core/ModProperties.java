package de.thedead2.customadvancements.util.core;


import org.apache.logging.log4j.Level;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Properties;


public class ModProperties extends Properties {

    private final Path propertiesFilePath;
    private ModProperties(Path propertiesFilePath){
        this.propertiesFilePath = propertiesFilePath;
    }

    private ModProperties(){
        this(null);
    }

    public static ModProperties empty(){
        return new ModProperties();
    }

    public static ModProperties fromPath(Path path) {
        ModProperties properties = new ModProperties(path);
        try {
            properties.load(Files.newInputStream(path));
        } catch (IOException e) {
            CrashHandler.getInstance().handleException("IOException while loading ModProperties", e, Level.ERROR);
        }
        return properties;
    }

    public static ModProperties fromInputStream(InputStream inputStream) {
        ModProperties properties = new ModProperties();
        try {
            properties.load(inputStream);
        } catch (IOException e) {
            CrashHandler.getInstance().handleException("IOException while loading ModProperties", e, Level.ERROR);
        }
        return properties;
    }

    @Override
    public ModProperties setProperty(String name, String value){
        super.setProperty(name, value);
        if(propertiesFilePath != null){
            try {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                this.store(outputStream, null);
                InputStream inputStream = FileHandler.outputStreamToInputStream(outputStream);
                this.load(inputStream);
                FileHandler.writeToFile(inputStream, Files.newOutputStream(propertiesFilePath, StandardOpenOption.WRITE));
            } catch (IOException e) {
                CrashHandler.getInstance().handleException("IOException while writing ModProperties", e, Level.ERROR);
            }
        }
        return this;
    }
}
