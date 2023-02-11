package de.thedead2.customadvancements.util.handler;

import de.thedead2.customadvancements.util.ModHelper;
import de.thedead2.customadvancements.util.exceptions.FileCopyException;
import org.apache.logging.log4j.Level;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.stream.Stream;

public abstract class FileHandler extends ModHelper {

    private final File directory;

    public FileHandler(File directory){
        this.directory = directory;
    }

    public static void checkForMainDirectories() {
        createDirectory(DIR_PATH.toFile());
        if(createDirectory(CUSTOM_ADVANCEMENTS_PATH.toFile())) {
            try {
                copyModFiles("examples/advancements", CUSTOM_ADVANCEMENTS_PATH, ".json");
                LOGGER.debug("Created example advancements!");
            }
            catch (FileCopyException e){
                LOGGER.error("Unable to create example advancements!");
                CrashHandler.getInstance().addCrashDetails("Unable to create example advancements!", Level.WARN, e);
                e.printStackTrace();
            }
        }

        if(createDirectory(TEXTURES_PATH.toFile())){
            try {
                copyModFiles("examples/textures", TEXTURES_PATH, ".png");
                LOGGER.debug("Created example textures for advancements!");
            }
            catch (FileCopyException e){
                LOGGER.error("Unable to create example textures for advancements!");
                CrashHandler.getInstance().addCrashDetails("Unable to create example textures!", Level.WARN, e);
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
        try{
            String subString = filePath.replace(String.valueOf(DIR_PATH), "");
            subString = subString.replaceAll(Matcher.quoteReplacement(String.valueOf(PATH_SEPARATOR)), "/");
            subString = subString.replaceFirst("/", "");
            subString = subString.replaceFirst("/", ":");
            return subString;
        }
        catch (Throwable throwable){
            CrashHandler.getInstance().addCrashDetails("Unable to create ID!", Level.ERROR , throwable);
            throw throwable;
        }
    }


    public static boolean createDirectory(File directoryIn){
        CrashHandler.getInstance().setActiveFile(directoryIn);
        if (!directoryIn.exists()) {
            if (directoryIn.mkdir()){
                LOGGER.debug("Created directory: " + directoryIn.toPath());
                CrashHandler.getInstance().setActiveFile(null);
                return true;
            }
            else {
                LOGGER.fatal("Failed to create directory at: " + directoryIn.toPath());
                throw new RuntimeException("Unable to create directory! Maybe something is blocking file access?!");
            }
        }
        else {
            LOGGER.debug("Found directory {} at {}", directoryIn.getName(), directoryIn.toPath());
            CrashHandler.getInstance().setActiveFile(null);
            return false;
        }
    }


    public static void writeFile(InputStream inputStream, Path outputPath) throws IOException {
        CrashHandler.getInstance().setActiveFile(outputPath.toFile());
        OutputStream fileOut = Files.newOutputStream(outputPath);

        writeToFile(inputStream, fileOut);

        fileOut.close();
        CrashHandler.getInstance().setActiveFile(null);
    }


    public static void writeToFile(InputStream inputStream, OutputStream fileOut) throws IOException {
        int input;
        while ((input = inputStream.read()) != -1){
            fileOut.write(input);
        }

        inputStream.close();
    }


    public static void copyModFiles(String pathIn, Path pathOut, String filter) throws FileCopyException {
        Path filespath = THIS_MOD_FILE.findResource(pathIn);

        try (Stream<Path> paths = Files.list(filespath)) {
            paths.filter(path -> path.toString().endsWith(filter)).forEach(path -> {
                try {
                    writeFile(Files.newInputStream(path), pathOut.resolve(path.getFileName().toString()));
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
