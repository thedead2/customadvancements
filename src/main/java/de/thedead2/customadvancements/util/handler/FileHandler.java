package de.thedead2.customadvancements.util.handler;

import de.thedead2.customadvancements.util.ModHelper;
import de.thedead2.customadvancements.util.exceptions.FileCopyException;

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

public abstract class FileHandler extends ModHelper {

    private final File directory;

    public FileHandler(File directory){
        this.directory = directory;
    }

    public static void checkForMainDirectories() {
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

    public void start() {
        if (this.directory.exists()){
            File[] folders = this.directory.listFiles();

            assert folders != null;
            if(Arrays.stream(folders).anyMatch(File::isFile)){
                this.readFiles(this.directory);
            }

            for(File subfolder : folders){
                if(subfolder.isDirectory()){
                    if (!subfolder.getName().equals(MOD_ID)){
                        DISABLE_STANDARD_ADVANCEMENT_LOAD = true;
                    }

                    this.readFiles(subfolder);

                    readSubDirectories(subfolder);
                }
            }
        }
    }


    private void readSubDirectories(File folderIn){
        for(File folder: Objects.requireNonNull(folderIn.listFiles())){
            if(folder.isDirectory()){
                this.readFiles(folder);
                readSubDirectories(folder);
            }
        }
    }


    public static String getId(String filePath){
        String subString = filePath.replace(DIR_PATH + "/", "");
        return subString.replaceFirst("/", ":");
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


    public static void writeFile(InputStream inputStream, Path outputPath) throws IOException {
        OutputStream fileOut = Files.newOutputStream(outputPath);

        writeToFile(inputStream, fileOut);

        fileOut.close();
    }


    public static void writeToFile(InputStream inputStream, OutputStream fileOut) throws IOException {
        int input;
        while ((input = inputStream.read()) != -1){
            fileOut.write(input);
        }

        inputStream.close();
    }


    public static void copyModFiles(String pathIn, String pathOut, String filter) throws FileCopyException {
        Path filespath = THIS_MOD_FILE.findResource(pathIn);

        try (Stream<Path> paths = Files.list(filespath)) {
            paths.filter(path -> path.toString().endsWith(filter)).forEach(path -> {
                try {
                    writeFile(Files.newInputStream(path), Paths.get(pathOut + "/" + path.getFileName()));
                }
                catch (IOException e) {
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

    protected abstract void readFiles(File directory);
}
