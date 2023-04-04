package de.thedead2.customadvancements.util.handler;

import de.thedead2.customadvancements.util.ModHelper;
import de.thedead2.customadvancements.util.exceptions.CrashHandler;
import net.minecraft.locale.Language;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

public class LanguageHandler extends FileHandler{
    private static final Map<String, File> LANG_FILES = new HashMap<>();
    private static LanguageHandler instance;

    public LanguageHandler(File directory) {
        super(directory);
        instance = this;
    }

    @Override
    protected void readFiles(File directory) {
        if(!Files.exists(ModHelper.LANG_PATH))
            return;

        try (Stream<Path> paths = Files.list(directory.toPath())) {
            paths.filter(path -> path.toString().endsWith(".json")).forEach(path -> {
                CrashHandler.getInstance().setActiveFile(path.toFile());
                String langName = path.getFileName().toString().replace(".json", "");
                LOGGER.debug("Found language file for {}", langName);
                LANG_FILES.put(langName, path.toFile());
            });
            CrashHandler.getInstance().setActiveFile(null);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static int size(){
        return LANG_FILES.size();
    }

    public void inject(String langName, Map<String, String> map){
        File langFile = LANG_FILES.get(langName);
        if(langFile == null)
            return;
        try {
            Language.loadFromJson(new FileInputStream(langFile), map::put);
        }
        catch (FileNotFoundException ignored) {}
    }

    public static LanguageHandler getInstance() {
        return Objects.requireNonNullElseGet(instance, () -> new LanguageHandler(LANG_PATH.toFile()));
    }
}
