package de.thedead2.customadvancements.util;

import de.thedead2.customadvancements.CustomAdvancement;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Stream;

import static de.thedead2.customadvancements.util.ModHelper.*;

public class FileHandler implements IFileHandler {

    private static final Logger LOGGER = LogManager.getLogger();

    public static long file_counter = 0;
    public static long textures_counter = 0;


    public void getDirectory() {
        File directory = new File(DIR_PATH);

        if (!directory.exists()) {
            if (directory.mkdir()) {
                LOGGER.info("Created " + MOD_ID + " folder at: " + GAME_DIR);

                try {
                    copyModFiles("examples/advancements", DIR_PATH, ".json");
                    LOGGER.debug("Created example advancements");
                }
                catch (FileCopyException e){
                    LOGGER.error("Unable to create example advancements!");
                    e.printStackTrace();
                }
            }
            else {
                LOGGER.fatal("Failed to create directory at: " + GAME_DIR);
                throw new RuntimeException("Unable to create directory! Maybe something is blocking file access?!");
            }
        }
        else {
            LOGGER.info("Found " + MOD_ID + " folder at: " + DIR_PATH);
        }

        File texturesDirectory = new File(TEXTURES_PATH);

        if (!texturesDirectory.exists()) {
            if (texturesDirectory.mkdir()) {
                LOGGER.info("Created textures folder at: " + DIR_PATH);

                try {
                    copyModFiles("examples/textures", TEXTURES_PATH, ".png");
                    LOGGER.debug("Created example textures for custom advancements!");
                }
                catch (FileCopyException e){
                    LOGGER.error("Unable to create example textures for custom advancements!");
                    e.printStackTrace();
                }
            }
            else {
                LOGGER.fatal("Failed to create textures directory at: " + DIR_PATH);
                throw new RuntimeException("Unable to create directory! Maybe something is blocking file access?!");
            }
        }
        else {
            LOGGER.info("Found textures folder at: " + TEXTURES_PATH);
        }
    }


    public void readFiles(File main_directory) {
        LOGGER.info("Starting to read files...");

        TEXTURE_HANDLER.readFiles(new File(TEXTURES_PATH));
        JSON_HANDLER.readFiles(main_directory);

        try (Stream<File> fileStream = Arrays.stream(Objects.requireNonNull(main_directory.listFiles()))) {
            fileStream.filter(file -> file.isDirectory() && !file.getName().equals("textures")).forEach(JSON_HANDLER::readFiles);
        }

        LOGGER.info("Loaded " + textures_counter + (textures_counter != 1 ? " Textures!" : " Texture!"));
        LOGGER.info("Loaded " + file_counter + (file_counter != 1 ? " Custom Advancements!" : " Custom Advancement!"));
    }


    private void copyModFiles(String pathIn, String pathOut, String filter) throws FileCopyException {
        Path filespath = THIS_MOD_FILE.findResource(pathIn);

        try (Stream<Path> paths = Files.list(filespath)) {
            paths.filter(path -> path.toString().endsWith(filter)).forEach(path -> {
                try {
                    InputStream fileIn = Files.newInputStream(path);
                    OutputStream fileOut = Files.newOutputStream(Paths.get(pathOut + "/" + path.getFileName()));

                    int input;

                    while ((input = fileIn.read()) != -1) {
                        fileOut.write(input);
                    }

                    fileIn.close();
                    fileOut.close();
                }
                catch (IOException e){
                    LOGGER.warn("Failed to copy mod files: " + e);
                    e.printStackTrace();
                    throw new FileCopyException("Failed to copy files: " + e);
                }
            });
            LOGGER.debug("Copied files from directory " + MOD_ID + ":" + pathIn + " to directory {}...", pathOut);
        }
        catch (IOException e) {
            LOGGER.warn("Unable to locate directory: " + MOD_ID + ":" + pathIn);
            e.printStackTrace();
            throw new FileCopyException("Unable to locate directory: " + MOD_ID + ":" + pathIn);
        }
    }


    public void printResourceLocations(IResourceManager resourceManager) {
        Collection<ResourceLocation> resourceLocations = resourceManager.getAllResourceLocations("advancements", (filename) -> filename.endsWith(".json"));

        File outputFile = new File(DIR_PATH + "/resource_locations.txt");

        try {
            FileWriter fileWriter = new FileWriter(outputFile);
            for (ResourceLocation resourceLocation: resourceLocations) {
                String resourceLocation_as_String = resourceLocation.toString().replace("advancements/", "").replace(".json", "") + ",\n";

                fileWriter.write(resourceLocation_as_String);
            }

            for (CustomAdvancement customAdvancement: CUSTOM_ADVANCEMENTS){
                fileWriter.write((customAdvancement.getResourceLocation().toString().replace(".json", "") + ",\n"));
            }

            fileWriter.close();
        }
        catch (IOException e){
            LOGGER.error("Unable to write resource locations to file!");
            e.printStackTrace();
        }
    }
}
