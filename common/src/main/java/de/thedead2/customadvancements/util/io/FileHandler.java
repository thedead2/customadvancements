package de.thedead2.customadvancements.util.io;

import de.thedead2.customadvancements.util.core.CrashHandler;
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
import java.util.function.Consumer;
import java.util.stream.Stream;

import static de.thedead2.customadvancements.util.core.ModHelper.*;


public class FileHandler {

    public static void checkForMainDirectories() {
        createDirectoryIfNecessary(DIR_PATH.toFile());

        createAndCopyDirectoryIfNecessary(CUSTOM_ADVANCEMENTS_PATH, "examples/advancements", ".json");

        createDirectoryIfNecessary(DATA_PATH.toFile());

        createAndCopyDirectoryIfNecessary(TEXTURES_PATH, "examples/data/textures", ".png");
        createAndCopyDirectoryIfNecessary(LANG_PATH, "examples/data/lang", ".json");
    }


    public static boolean createDirectoryIfNecessary(File directoryIn) {
        if (directoryIn.exists()) {
            LOGGER.debug("Found directory {} at {}", directoryIn.getName(), directoryIn.toPath());

            return false;
        }

        if (directoryIn.mkdir()) {
            LOGGER.debug("Created directory: {}", directoryIn.toPath());

            return true;
        }
        else {
            LOGGER.fatal("Failed to create directory at: {}", directoryIn.toPath());

            throw new RuntimeException("Unable to create directory! Maybe something is blocking file access?!");
        }
    }


    public static void createAndCopyDirectoryIfNecessary(Path path, String examplePath, String fileFilter) {
        if (createDirectoryIfNecessary(path.toFile())) {
            try {
                copyModFiles(examplePath, path, fileFilter);
            }
            catch (FileCopyException e) {
                CrashHandler.getInstance().handleException("Unable to copy example files to " + path, e, Level.WARN);
            }
        }
    }


    public static void copyModFiles(String pathIn, Path pathOut, String filter) throws FileCopyException {
        Path filespath = getModFileFor(MOD_ID).findResource(pathIn);

        try (Stream<Path> paths = Files.list(filespath)) {
            paths.filter(path -> path.toString().endsWith(filter)).forEach(path -> {
                try {
                    writeFile(Files.newInputStream(path), pathOut.resolve(path.getFileName().toString()));
                }
                catch (IOException e) {
                    throw new FileCopyException("Failed to copy mod files!", e);
                }
            });

            LOGGER.debug("Copied files from directory " + MOD_ID + ":{} to directory {}", pathIn, pathOut);
        }
        catch (IOException e) {
            throw new FileCopyException("Unable to locate directory: " + MOD_ID + ":" + pathIn, e);
        }
    }


    public static void writeFile(InputStream inputStream, Path outputPath) throws IOException {
        OutputStream fileOut = Files.newOutputStream(outputPath);

        writeToFile(inputStream, fileOut);

        fileOut.close();
    }


    public static void writeToFile(InputStream inputStream, OutputStream fileOut) throws IOException {
        int input;

        while ((input = inputStream.read()) != -1) {
            fileOut.write(input);
        }

        inputStream.close();
    }


    public static void readDirectoryAndSubDirectories(File directory, Consumer<File> fileReader) {
        if (directory.exists()) {
            File[] files = directory.listFiles();

            if (Arrays.stream(files).anyMatch(File::isFile)) {
                fileReader.accept(directory);
            }

            for (File subDirectory : Arrays.stream(files).filter(File::isDirectory).toList()) {
                fileReader.accept(subDirectory);

                readSubDirectories(subDirectory, fileReader);
            }
        }
    }


    private static void readSubDirectories(File folderIn, Consumer<File> fileReader) {
        for (File folder : Objects.requireNonNull(folderIn.listFiles(File::isDirectory))) {
            fileReader.accept(folder);

            readSubDirectories(folder, fileReader);
        }
    }
}
