package de.thedead2.customadvancements.util;

import de.thedead2.customadvancements.util.miscellaneous.FileCopyException;
import de.thedead2.customadvancements.util.miscellaneous.FileWriteException;
import net.minecraft.client.Minecraft;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;

import net.minecraftforge.fml.loading.FMLEnvironment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static de.thedead2.customadvancements.util.ModHelper.*;

public class FileHandler implements IFileHandler {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final List<String> STRINGS = new ArrayList<>();

    public void checkForMainDirectories() {
        createDirectory(new File(DIR_PATH));
        if(createDirectory(new File(CUSTOM_ADVANCEMENTS_PATH))){
            try {
                copyModFiles("examples/advancements", CUSTOM_ADVANCEMENTS_PATH, ".json");
                LOGGER.debug("Created example advancements!");
            }
            catch (FileCopyException e){
                LOGGER.error("Unable to create example advancements!");
                e.printStackTrace();
            }
        }

        if(createDirectory(new File(TEXTURES_PATH))){
            try {
                copyModFiles("examples/textures", TEXTURES_PATH, ".png");
                LOGGER.debug("Created example textures for advancements!");
            }
            catch (FileCopyException e){
                LOGGER.error("Unable to create example textures for advancements!");
                e.printStackTrace();
            }
        }
    }


    @Override
    public void readFiles(File main_directory) {
        LOGGER.info("Starting to read files...");

        TEXTURE_HANDLER.readFiles(new File(TEXTURES_PATH));

        if (main_directory.exists()){
            File[] modFolders = main_directory.listFiles();

            assert modFolders != null;
            for(File modFolder:modFolders){
                if(modFolder.isDirectory() && !modFolder.getName().equals("textures")){
                    if (!modFolder.getName().equals(MOD_ID)){
                        DISABLE_STANDARD_ADVANCEMENT_LOAD = true;
                    }

                    JSON_HANDLER.readFiles(modFolder);

                    readSubDirectories(modFolder);
                }
            }
        }

        LOGGER.info("Loaded " + TEXTURES.size() + (TEXTURES.size() != 1 ? " Textures!" : " Texture!"));
        LOGGER.info("Loaded " + CUSTOM_ADVANCEMENTS.size() + (CUSTOM_ADVANCEMENTS.size() != 1 ? " Custom Advancements!" : " Custom Advancement!"));
        LOGGER.info("Loaded " + GAME_ADVANCEMENTS.size() + (GAME_ADVANCEMENTS.size() != 1 ? " Game Advancements!" : " Game Advancement!"));

    }


    private void readSubDirectories(File folderIn){
        for(File folder: Objects.requireNonNull(folderIn.listFiles())){
            if(folder.isDirectory()){
                JSON_HANDLER.readFiles(folder);
                readSubDirectories(folder);
            }
        }
    }


    public int printResourceLocations(CommandSourceStack source) {

        source.sendSuccess(new TextComponent("[" + MOD_NAME + "]: Starting to write resource locations to file..."), false);
        LOGGER.info("Starting to write resource locations to file...");

        OutputStream fileOut = null;
        try {
            Path outputPath = Paths.get(DIR_PATH + "/resource_locations.txt");
            fileOut = Files.newOutputStream(outputPath);

            for (ResourceLocation resourceLocation: ALL_DETECTED_GAME_ADVANCEMENTS.keySet()) {
                String resourceLocation_as_String = resourceLocation.toString() + ",\n";

                InputStream inputStream = new ByteArrayInputStream(resourceLocation_as_String.getBytes());

                try {
                    int input;
                    while ((input = inputStream.read()) != -1){
                        fileOut.write(input);
                    }
                }
                catch (IOException e) {
                    LOGGER.error("Unable to write file: " + outputPath.getFileName());
                    source.sendFailure(new TextComponent("[" + MOD_NAME + "]: Unable to write resource locations to file!"));
                    e.printStackTrace();
                }
                finally {
                    try {
                        inputStream.close();
                    }
                    catch (IOException e){
                        LOGGER.warn("Unable to close InputStream!");
                        e.printStackTrace();
                    }
                }
            }

            for (ResourceLocation resourceLocation: CUSTOM_ADVANCEMENTS.keySet()){
                String resourceLocation_as_String = resourceLocation.toString().replace(".json", "") + ",\n";

                InputStream inputStream = new ByteArrayInputStream(resourceLocation_as_String.getBytes());

                try {
                    int input;
                    while ((input = inputStream.read()) != -1){
                        fileOut.write(input);
                    }
                }
                catch (IOException e) {
                    LOGGER.error("Unable to write file: " + outputPath.getFileName());
                    source.sendFailure(new TextComponent("[" + MOD_NAME + "]: Unable to write resource locations to file!"));
                    e.printStackTrace();
                }
                finally {
                    try {
                        inputStream.close();
                    }
                    catch (IOException e){
                        LOGGER.warn("Unable to close InputStream!");
                        e.printStackTrace();
                    }
                }
            }
            source.sendSuccess(new TextComponent("[" + MOD_NAME + "]: Finished!"), false);
            return 1;
        }
        catch (IOException e){
            LOGGER.error("Unable to write resource locations to file!");
            source.sendFailure(new TextComponent("[" + MOD_NAME + "]: Unable to write resource locations to file!"));
            e.printStackTrace();
            return -1;
        }
        finally {
            try {
                assert fileOut != null;
                fileOut.close();
            }
            catch (IOException e) {
                LOGGER.warn("Unable to close OutputStream!");
                e.printStackTrace();
            }
        }
    }


    public int reload(CommandSourceStack source) {
        source.sendSuccess(new TextComponent("[" + MOD_NAME + "]: Starting to reload data..."), false);
        LOGGER.info("Starting to reload data...");

        clearAll();

        checkForMainDirectories();
        readFiles(new File(DIR_PATH));

        if(FMLEnvironment.dist.isClient()){
            assert Minecraft.getInstance().player != null;
            Minecraft.getInstance().player.chat("/reload");
            source.sendSuccess(new TextComponent("[" + MOD_NAME + "]: Reload completed!"), false);
        }
        else {
            source.sendSuccess(new TextComponent("[" + MOD_NAME + "]: Reload completed! Please use /reload to reload all advancements"), false);
        }
        LOGGER.info("Reload complete.");
        return 1;
    }


    public int generateGameAdvancements(CommandSourceStack source) {
        AtomicInteger counter = new AtomicInteger();

        LOGGER.info("Starting to generate files for game advancements...");
        source.sendSuccess(new TextComponent("[" + MOD_NAME + "]: Starting to generate files for game advancements..."), false);

        createDirectory(new File(DIR_PATH));

        ALL_DETECTED_GAME_ADVANCEMENTS.forEach((advancement, advancementData) -> {
            STRINGS.clear();
            String advancementNamespace = advancement.getNamespace();
            String advancementPath = advancement.getPath();


            LOGGER.debug("Generating file: " + advancement);

            createDirectory(new File(DIR_PATH + "/" + advancementNamespace));

            File advancementJson;

            if(advancementPath.contains("/")){
                String subStringDirectory = advancementPath.replaceAll(advancementPath.substring(advancementPath.indexOf("/")), "");
                STRINGS.add(subStringDirectory);
                discoverSubDirectories(subStringDirectory);

                String basePath = DIR_PATH + "/" + advancementNamespace;

                for(String folderName: STRINGS){
                    basePath = basePath + "/" + folderName;
                    createDirectory(new File(basePath));
                }

                advancementJson = new File(basePath + advancementPath.substring(advancementPath.lastIndexOf("/")) + ".json");
            }
            else {
                advancementJson = new File(DIR_PATH + "/" + advancementNamespace + "/" + advancementPath + ".json");
            }

            StringBuilder stringBuilder = new StringBuilder();

            for (char character:advancementData.toString().toCharArray()){
                if (character == '{' || character == ',' || character == '['){
                    stringBuilder.append(character).append('\n');
                }
                else {
                    stringBuilder.append(character);
                }
            }
            String temp = stringBuilder.toString();
            InputStream inputStream = new ByteArrayInputStream(temp.getBytes());

            try {
                writeFile(inputStream, advancementJson.toPath());
                counter.getAndIncrement();
            }
            catch (FileWriteException e){
                source.sendFailure(new TextComponent("[" + MOD_NAME + "]: Unable to write advancement " + advancement + " to file!"));
                LOGGER.error("Unable to write advancement {} to file!", advancement);
            }
        });
        LOGGER.info("Generated {} files for game advancements", counter.get());
        source.sendSuccess(new TextComponent("[" + MOD_NAME + "]: Generated " + counter.get() + " files for game advancements successfully!"), true);
        counter.set(0);
        return 1;
    }


    private void discoverSubDirectories(String pathIn){
        if (pathIn.contains("/")){
            String first = pathIn.replace(pathIn.substring(pathIn.indexOf("/")), "");
            String second = pathIn.replace(first + "/", "");
            String temp = second.replace(second.substring(second.indexOf("/")), "");
            STRINGS.add(temp);
            discoverSubDirectories(temp);
        }
    }


    private static boolean createDirectory(File directoryIn){
        if (!directoryIn.exists()) {
            if (directoryIn.mkdir()){
                LOGGER.debug("Created directory: " + directoryIn.toPath());
                return true;
            }
            else {
                LOGGER.fatal("Failed to create directory at: " + directoryIn.toPath());
                throw new RuntimeException("Unable to create directory! Maybe something is blocking file access?!");
            }
        }
        else {
            LOGGER.debug("Found directory {} at {}", directoryIn.getName(), directoryIn.toPath());
            return false;
        }
    }


    private static void writeFile(InputStream inputStreamIn, Path outputPath){
        InputStream inputStream = null;
        OutputStream fileOut = null;

        try {
            inputStream = inputStreamIn;
            fileOut = Files.newOutputStream(outputPath);

            int input;
            while ((input = inputStream.read()) != -1){
                fileOut.write(input);
            }
        }
        catch (IOException e) {
            LOGGER.error("Unable to write file: " + outputPath.getFileName());
            e.printStackTrace();
            throw new FileWriteException("Unable to write file: " + outputPath.getFileName());
        }
        finally {
            try {
                assert inputStream != null;
                inputStream.close();
                assert fileOut != null;
                fileOut.close();
            }
            catch (IOException e) {
                LOGGER.warn("Unable to close InputStream/ OutputStream!");
                e.printStackTrace();
            }
        }
    }


    private static void copyModFiles(String pathIn, String pathOut, String filter) throws FileCopyException {
        Path filespath = THIS_MOD_FILE.findResource(pathIn);

        try (Stream<Path> paths = Files.list(filespath)) {
            paths.filter(path -> path.toString().endsWith(filter)).forEach(path -> {
                try {
                    writeFile(Files.newInputStream(path), Paths.get(pathOut + "/" + path.getFileName()));
                }
                catch (FileWriteException | IOException e){
                    LOGGER.warn("Failed to copy mod files: " + e);
                    e.printStackTrace();
                    throw new FileCopyException("Failed to copy files: " + e);
                }
            });
            LOGGER.debug("Copied files from directory " + MOD_ID + ":" + pathIn + " to directory {}", pathOut);
        }
        catch (IOException e) {
            LOGGER.warn("Unable to locate directory: " + MOD_ID + ":" + pathIn);
            e.printStackTrace();
            throw new FileCopyException("Unable to locate directory: " + MOD_ID + ":" + pathIn);
        }
    }
}
