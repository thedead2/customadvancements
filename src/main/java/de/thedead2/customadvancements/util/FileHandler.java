package de.thedead2.customadvancements.util;

import de.thedead2.customadvancements.advancements.CustomAdvancement;
import de.thedead2.customadvancements.util.miscellaneous.FileCopyException;
import de.thedead2.customadvancements.util.miscellaneous.FileWriteException;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static de.thedead2.customadvancements.util.ModHelper.*;

public class FileHandler implements IFileHandler {

    private static final Logger LOGGER = LogManager.getLogger();

    public static long cA_file_counter = 0;
    public static long gA_file_counter = 0;
    public static long textures_counter = 0;


    public void checkForMainDirectories() {
        if(createDirectory(new File(DIR_PATH))){
            try {
                copyModFiles("examples/advancements", DIR_PATH, ".json");
                LOGGER.debug("Created example custom advancements!");
            }
            catch (FileCopyException e){
                LOGGER.error("Unable to create example advancements!");
                e.printStackTrace();
            }
        }

        if(createDirectory(new File(TEXTURES_PATH))){
            try {
                copyModFiles("examples/textures", TEXTURES_PATH, ".png");
                LOGGER.debug("Created example textures for custom advancements!");
            }
            catch (FileCopyException e){
                LOGGER.error("Unable to create example textures for custom advancements!");
                e.printStackTrace();
            }
        }
    }


    public void readFiles(File main_directory) {
        LOGGER.info("Starting to read files...");

        TEXTURE_HANDLER.readFiles(new File(TEXTURES_PATH));
        JSON_HANDLER.readFiles(main_directory);

        try (Stream<File> fileStream = Arrays.stream(Objects.requireNonNull(main_directory.listFiles()))) {
            fileStream.filter(file -> file.isDirectory() && !file.getName().equals("textures") && !file.getName().equals("game_advancements")).forEach(JSON_HANDLER::readFiles);
        }
        readGameAdvancements();

        LOGGER.info("Loaded " + textures_counter + (textures_counter != 1 ? " Textures!" : " Texture!"));
        LOGGER.info("Loaded " + cA_file_counter + (cA_file_counter != 1 ? " Custom Advancements!" : " Custom Advancement!"));
        if(DISABLE_STANDARD_ADVANCEMENT_LOAD){
            LOGGER.info("Loaded " + gA_file_counter + (gA_file_counter != 1 ? " Game Advancements!" : " Game Advancement!"));
        }

        cA_file_counter = 0;
        gA_file_counter = 0;
        textures_counter = 0;
    }


    private void readGameAdvancements(){
        File gameAdvancementsDirectory = new File(GAME_ADVANCEMENTS_PATH);

        if (gameAdvancementsDirectory.exists()){
            File[] modFolders = gameAdvancementsDirectory.listFiles();
            DISABLE_STANDARD_ADVANCEMENT_LOAD = true;

            assert modFolders != null;
            for(File modFolder:modFolders){
                if(modFolder.isDirectory()){
                    JSON_HANDLER.readFiles(modFolder);

                    try (Stream<File> fileStream = Arrays.stream(Objects.requireNonNull(modFolder.listFiles()))) {
                        fileStream.filter(File::isDirectory).forEach(JSON_HANDLER::readFiles);
                    }
                }
            }
        }
    }


    public int printResourceLocations(CommandSource source) {

        source.sendFeedback(new StringTextComponent("[" + MOD_NAME + "]: Starting to write resource locations to file..."), false);
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
                    source.sendErrorMessage(new StringTextComponent("[" + MOD_NAME + "]: Unable to write resource locations to file!"));
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

            for (CustomAdvancement customAdvancement: CUSTOM_ADVANCEMENTS){
                String resourceLocation_as_String = customAdvancement.getResourceLocation().toString().replace(".json", "") + ",\n";

                InputStream inputStream = new ByteArrayInputStream(resourceLocation_as_String.getBytes());

                try {
                    int input;
                    while ((input = inputStream.read()) != -1){
                        fileOut.write(input);
                    }
                }
                catch (IOException e) {
                    LOGGER.error("Unable to write file: " + outputPath.getFileName());
                    source.sendErrorMessage(new StringTextComponent("[" + MOD_NAME + "]: Unable to write resource locations to file!"));
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
            source.sendFeedback(new StringTextComponent("[" + MOD_NAME + "]: Finished!"), false);
            return 1;
        }
        catch (IOException e){
            LOGGER.error("Unable to write resource locations to file!");
            source.sendErrorMessage(new StringTextComponent("[" + MOD_NAME + "]: Unable to write resource locations to file!"));
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


    public int reload(CommandSource source) {
        source.sendFeedback(new StringTextComponent("[" + MOD_NAME + "]: Starting to reload data..."), false);
        LOGGER.info("Starting to reload data...");

        clearAll();

        checkForMainDirectories();
        readFiles(new File(DIR_PATH));

        if(FMLEnvironment.dist.isClient()){
            assert Minecraft.getInstance().player != null;
            Minecraft.getInstance().player.sendChatMessage("/reload");
            source.sendFeedback(new StringTextComponent("[" + MOD_NAME + "]: Reload completed!"), false);
        }
        else {
            source.sendFeedback(new StringTextComponent("[" + MOD_NAME + "]: Reload completed! Please use /reload to reload all advancements"), false);
        }
        LOGGER.info("Reload completed!");
        return 1;
    }


    public int generateGameAdvancements(CommandSource source) {
        AtomicInteger counter = new AtomicInteger();

        LOGGER.info("Starting to generate files for {} game advancements...", ALL_DETECTED_GAME_ADVANCEMENTS.size());
        source.sendFeedback(new StringTextComponent("[" + MOD_NAME + "]: Starting to generate files for " + ALL_DETECTED_GAME_ADVANCEMENTS.size() + " game advancements..."), false);

        createDirectory(new File(ModHelper.GAME_ADVANCEMENTS_PATH));

        ALL_DETECTED_GAME_ADVANCEMENTS.forEach((advancement, advancementData) -> {
            String advancementNamespace = advancement.getNamespace();
            String advancementPath = advancement.getPath();

            if(advancementPath.contains("recipes/")){
                LOGGER.debug("Skipping recipe advancement: " + advancement);
                return;
            }

            LOGGER.debug("Starting to generate file: " + advancement);

            createDirectory(new File(GAME_ADVANCEMENTS_PATH + "/" + advancementNamespace));

            File advancementJson;

            if(advancementPath.contains("/")){
                String subStringDirectory = advancementPath.replaceAll(advancementPath.substring(advancementPath.indexOf("/")), "");
                String modSubDirectory = GAME_ADVANCEMENTS_PATH + "/" + advancementNamespace + "/" + subStringDirectory;
                createDirectory(new File(modSubDirectory));

                advancementJson = new File(modSubDirectory + "/" + advancementPath.substring(advancementPath.lastIndexOf("/")) + ".json");
            }
            else {
                advancementJson = new File(GAME_ADVANCEMENTS_PATH + "/" + advancementNamespace + "/" + advancementPath.substring(advancementPath.lastIndexOf("/")) + ".json");
            }


            InputStream inputStream = new ByteArrayInputStream(advancementData.toString().getBytes());

            try {
                writeFile(inputStream, advancementJson.toPath());
                counter.getAndIncrement();
            }
            catch (FileWriteException e){
                source.sendErrorMessage(new StringTextComponent("[" + MOD_NAME + "]: Unable to write advancement " + advancement + " to file!"));
                LOGGER.error("Unable to write advancement {} to file!", advancement);
            }
        });
        LOGGER.info("Generated {} files for game advancements", counter.get());
        source.sendFeedback(new StringTextComponent("[" + MOD_NAME + "]: Generated " + counter.get() + " files for game advancements successfully!"), true);
        counter.set(0);
        return 1;
    }


    private boolean createDirectory(File directoryIn){
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


    private void writeFile(InputStream inputStreamIn, Path outputPath){
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


    private void copyModFiles(String pathIn, String pathOut, String filter) throws FileCopyException {
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
