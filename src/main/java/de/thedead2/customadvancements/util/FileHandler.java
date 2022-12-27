package de.thedead2.customadvancements.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
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
                LOGGER.info("Created " + MOD_ID + " folder at " + GAME_DIR + " successfully!");

                try {
                    copyModFiles("examples/advancements", DIR_PATH, ".json");
                    LOGGER.debug("Created example custom advancements successfully!");
                }
                catch (IOException e) {
                    LOGGER.warn("Failed to create example advancements: " + e);
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
                LOGGER.info("Created textures folder at " + DIR_PATH + " successfully!");

                try {
                    copyModFiles("examples/textures", TEXTURES_PATH, ".png");
                    LOGGER.debug("Created example textures for custom advancements successfully!");
                }
                catch (IOException e) {
                    LOGGER.warn("Failed to create example textures for Custom Advancements: " + e);
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
        TEXTURE_HANDLER.readFiles(new File(TEXTURES_PATH));

        LOGGER.info("Starting to read files...");

        JSON_HANDLER.readFiles(main_directory);

        try (Stream<File> fileStream = Arrays.stream(Objects.requireNonNull(main_directory.listFiles()))) {
            fileStream.filter(file -> file.isDirectory() && !file.getName().equals("textures")).forEach(JSON_HANDLER::readFiles);
        }

        LOGGER.info("Loaded " + textures_counter + ((1 < textures_counter || textures_counter == 0) ? " Textures!" : " Texture!"));
        LOGGER.info("Loaded " + file_counter + ((file_counter > 1 || file_counter == 0) ? " Custom Advancements!" : " Custom Advancement!"));
    }


    private void copyModFiles(String pathIn, String pathOut, String filter) throws IOException {
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
                }
            });
            LOGGER.debug("Copied mod files from directory " + MOD_ID + ":" + pathIn + " to directory {} successfully!", pathOut);
        }
        catch (IOException e) {
            LOGGER.warn("Failed to copy mod files: " + e);
            e.printStackTrace();
        }
    }
}
