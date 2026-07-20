package de.thedead2.customadvancements.util.io;

import de.thedead2.customadvancements.util.exceptions.ExceptionHandler;
import net.minecraft.locale.Language;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static de.thedead2.customadvancements.util.core.ModHelper.*;


public class LanguageHandler {

    private static final Map<String, File> LANG_FILES = new HashMap<>();


    public static void loadLangFiles() {
        FileHandler.readDirectoryAndSubDirectories(LANG_PATH.toFile(), directory -> {
            if (!Files.exists(LANG_PATH)) {
                return;
            }

            try (Stream<Path> paths = Files.list(directory.toPath())) {
                paths.filter(path -> path.toString().endsWith(".json"))
                     .forEach(path -> {
                         String langName = path.getFileName().toString().replace(".json", "");
                         LOGGER.debug("Found localisation file for {}", langName);

                         LANG_FILES.put(langName, path.toFile());
                     });
            }
            catch (IOException e) {
                ExceptionHandler.getInstance().logWarning("Can't list files of directory: " + directory, e);
            }
        });
    }


    public static int size() {
        return LANG_FILES.size();
    }


    public static void inject(String langName, Map<String, String> map) {
        File langFile = LANG_FILES.get(langName);

        if (langFile == null) {
            return;
        }

        try {
            Language.loadFromJson(new FileInputStream(langFile), map::put);
        }
        catch (FileNotFoundException e) {
            ExceptionHandler.getInstance().log("Didn't find file " + langFile + "! That shouldn't be possible?!", e);
        }
    }
}
