package de.thedead2.customadvancements.util;

import com.google.gson.JsonObject;
import de.thedead2.customadvancements.CustomAdvancement;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.ResourceLocationException;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static de.thedead2.customadvancements.util.ModHelper.*;

public class FileHandler {

    private static final Logger LOGGER = LogManager.getLogger();

    public static Set<CustomAdvancement> customadvancements = new HashSet<>();
    public static Map<ResourceLocation, NativeImage> textures = new HashMap<>();

    private static long filecounter = 0;


    public void getDirectory() {
        File directory = new File(DIR_PATH);

        if (!directory.exists()) {
            if (directory.mkdir()) {
                LOGGER.info("Created " + MOD_ID + " folder at " + GAME_DIR + " successfully!");

                try {
                    copyModFiles("examples/advancements", DIR_PATH);
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
                    copyModFiles("examples/textures", TEXTURES_PATH);
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

    public void readFiles() {
        File[] fileList = new File(DIR_PATH).listFiles();

        LOGGER.info("Starting to read files...");

        if (FMLEnvironment.dist.isDedicatedServer()){
            LOGGER.debug("Detected dedicated server running! Disabling textures loading...");
        }

        try {
            assert fileList != null;

            if (fileList.length == 0) {
                LOGGER.info("Found 0 files in " + MOD_ID + " to inject! Skipping...");
                return;

            }

            for (File file : fileList) {
                try {
                    if (file.isFile()) {
                        if(file.getName().endsWith(".json")){
                            readFile(file);
                            filecounter++;
                        }
                        else {
                            LOGGER.warn("File '" + file.getName() + "' is not a .json file, ignoring it!");
                        }
                    }
                    else if (file.isDirectory() && file.getName().equals("textures") && FMLEnvironment.dist.isClient()) {
                        readTextures(file);
                    }
                    else if (file.isDirectory() && !file.getName().equals("textures")) {
                        LOGGER.debug("Found subdirectory {}! Scanning for Custom Advancements...", file.getPath().substring(file.getPath().indexOf(MOD_ID)));

                        File[] subFileList = new File(file.getAbsolutePath()).listFiles();

                        assert subFileList != null;
                        LOGGER.debug("Found " + subFileList.length + " files in " + file.getPath() + " directory!");

                        for (File subFile : subFileList) {
                            readFile(subFile);
                            filecounter++;
                        }
                    }
                }
                catch (NullPointerException e) {
                    LOGGER.error("Unable to read {}! Make sure that it matches the required format!", file.getName());
                    e.printStackTrace();
                }
                catch (IllegalStateException e) {
                    LOGGER.error("Error loading custom advancement: " + e);
                    e.printStackTrace();
                }
                catch (ResourceLocationException e) {
                    LOGGER.error("Unable to load {} as Custom Advancement!", file.getName());
                    e.printStackTrace();
                }
                catch (Exception e) {
                    LOGGER.fatal("Something went wrong: " + e);
                    throw new RuntimeException(e);
                }

            }

            LOGGER.info("Found " + filecounter + " Custom Advancements!");

        }
        catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    private String getId(String filePath) {
        return filePath.substring(filePath.lastIndexOf(MOD_ID)).replaceFirst("/", ":");
    }

    private void readFile(File file) {
        String fileName = file.getName();

        LOGGER.debug("Found file: " + fileName);

        JsonObject jsonObject = JSON_HANDLER.getJson(file);

        LOGGER.debug("File " + fileName + " as JsonObject: " + jsonObject);

        if (JSON_HANDLER.isCorrectJsonFormat(jsonObject)) {
            CustomAdvancement customadvancement = new CustomAdvancement(jsonObject, fileName, file.getAbsolutePath());

            customadvancements.add(customadvancement);
        }
        else {
            LOGGER.error(fileName + " does not match the required Json Format!");
            throw new IllegalStateException();
        }
    }


    private void readTextures(File directory) throws IOException {
        File[] texture_files = directory.listFiles();

        LOGGER.info("Reading texture files!");

        assert texture_files != null;

        for (File texture : texture_files) {
            if (texture.getName().endsWith(".png")) {
                LOGGER.debug("Found file: " + texture.getName());

                InputStream inputStream = Files.newInputStream(texture.toPath());
                NativeImage image = NativeImage.read(inputStream);
                ResourceLocation textureLocation = ResourceLocation.tryCreate(getId(texture.getPath()));

                LOGGER.debug("Texture Location for file " + texture.getName() + ": " + textureLocation);

                textures.put(textureLocation, image);
                inputStream.close();
            }
            else {
                LOGGER.warn("File '" + texture.getName() + "' is not a .png file, ignoring it!");
            }
        }
    }

    public void copyModFiles(String pathIn, String pathOut) throws IOException {
        Path filespath = THIS_MOD_FILE.findResource(pathIn);

        File[] files = new File(filespath.toUri()).listFiles();

        assert files != null;

        for(File file:files){
            InputStream fileIn = Files.newInputStream(file.toPath());
            OutputStream fileOut = Files.newOutputStream(Paths.get(pathOut + "/" + file.getName()));

            int input;

            while ((input = fileIn.read()) != -1){
                fileOut.write(input);
            }

            fileIn.close();
            fileOut.close();
        }

        LOGGER.debug("Copied mod files from directory " + MOD_ID + ":" + pathIn + " to directory {} successfully!", pathOut);
    }
}
