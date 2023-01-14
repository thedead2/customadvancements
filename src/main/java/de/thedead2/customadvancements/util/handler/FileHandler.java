package de.thedead2.customadvancements.util.handler;

import de.thedead2.customadvancements.util.exceptions.FileCopyException;
import de.thedead2.customadvancements.util.exceptions.FileWriteException;
import net.minecraftforge.fml.loading.FMLEnvironment;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.stream.Stream;

import static de.thedead2.customadvancements.util.ModHelper.*;

public class FileHandler implements IFileHandler {

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

        if (!FMLEnvironment.dist.isDedicatedServer() || !ConfigManager.OPTIFINE_SHADER_COMPATIBILITY.get()){
            TEXTURE_HANDLER.readFiles(new File(TEXTURES_PATH));
        }
        else if (ConfigManager.OPTIFINE_SHADER_COMPATIBILITY.get()){
            LOGGER.warn("Enabling compatibility mode for Optifine Shaders! This disables custom background textures for advancements!");
        }

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
        LOGGER.info("Loaded " + CUSTOM_ADVANCEMENTS.size() + (CUSTOM_ADVANCEMENTS.size() != 1 ? " CustomAdvancements!" : " CustomAdvancement!"));
        LOGGER.info("Loaded " + GAME_ADVANCEMENTS.size() + (GAME_ADVANCEMENTS.size() != 1 ? " GameAdvancements!" : " GameAdvancement!"));
    }


    private void readSubDirectories(File folderIn){
        for(File folder: Objects.requireNonNull(folderIn.listFiles())){
            if(folder.isDirectory()){
                JSON_HANDLER.readFiles(folder);
                readSubDirectories(folder);
            }
        }
    }


    public static boolean createDirectory(File directoryIn){
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


    public static void writeFile(InputStream inputStreamIn, Path outputPath){
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
