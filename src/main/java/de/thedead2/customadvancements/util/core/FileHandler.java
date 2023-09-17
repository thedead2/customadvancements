package de.thedead2.customadvancements.util.core;

import de.thedead2.customadvancements.util.exceptions.FileCopyException;
import net.minecraft.resources.ResourceLocation;
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

import static de.thedead2.customadvancements.util.core.ModHelper.*;

public abstract class FileHandler {

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
                CrashHandler.getInstance().handleException("Unable to create example advancements!", e, Level.WARN, true);
            }
        }
        createDirectory(DATA_PATH.toFile());
        if(createDirectory(TEXTURES_PATH.toFile())){
            try {
                copyModFiles("examples/data/textures", TEXTURES_PATH, ".png");
                LOGGER.debug("Created example textures for advancements!");
            }
            catch (FileCopyException e){
                CrashHandler.getInstance().handleException("Unable to create example textures!", e, Level.WARN, true);
            }
        }
        if(createDirectory(LANG_PATH.toFile())){
            try {
                copyModFiles("examples/data/lang", LANG_PATH, ".json");
                LOGGER.debug("Created example lang files for advancements!");
            }
            catch (FileCopyException e){
                CrashHandler.getInstance().handleException("Unable to create example lang files!", e, Level.WARN, true);
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


    public static ResourceLocation getId(String filePath){
        return getId(filePath, false);
    }

    public static ResourceLocation getId(String filePath, boolean onlyWrap){
        try{
            String subString = filePath.replace(String.valueOf(DIR_PATH), "");
            if(!onlyWrap){
                subString = subString.replaceAll(Matcher.quoteReplacement(String.valueOf(PATH_SEPARATOR)), "/");
                subString = subString.replaceFirst("/", "");
                subString = subString.replaceFirst("/", ":");
            }

            return new ResourceLocation(subString);
        }
        catch (Throwable throwable){
            CrashHandler.getInstance().handleException("Unable to create ID!", throwable, Level.ERROR);
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
        Path filespath = THIS_MOD_FILE.get().findResource(pathIn);

        try (Stream<Path> paths = Files.list(filespath)) {
            paths.filter(path -> path.toString().endsWith(filter)).forEach(path -> {
                try {
                    writeFile(Files.newInputStream(path), pathOut.resolve(path.getFileName().toString()));
                }
                catch (IOException e) {
                    FileCopyException copyException = new FileCopyException("Failed to copy mod files!");
                    copyException.addSuppressed(e);
                    throw copyException;
                }
            });
            LOGGER.debug("Copied files from directory " + MOD_ID + ":" + pathIn + " to directory {}", pathOut);
        }
        catch (IOException e) {
            FileCopyException copyException = new FileCopyException("Unable to locate directory: " + MOD_ID + ":" + pathIn);
            copyException.addSuppressed(e);
            throw copyException;
        }
    }

    protected abstract void readFiles(File directory);
}
