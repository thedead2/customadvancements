package de.thedead2.customadvancements.util.io;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.thedead2.customadvancements.network.SyncLangDataPayload;
import de.thedead2.mc_libs.concurrent.PartialCompletableFuture;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.util.GsonHelper;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static de.thedead2.customadvancements.util.core.ModHelper.*;


public class LanguageHandler {

    public static final Path LANG_PATH = DATA_PATH.resolve("lang");

    private PartialCompletableFuture<Map<String, JsonElement>> langFilesFuture = PartialCompletableFuture.completedFuture(new ConcurrentHashMap<>());

    public void loadLangFiles() {
        this.langFilesFuture = PartialCompletableFuture.supplyAsync(new ConcurrentHashMap<>(), PartialCompletableFuture.Utils.mapClone(), (map, throwable) -> {
            LOGGER.error("Failed to load lang files!", throwable);
            logLoadStatus(map.size(), "lang file");

            return map;
        }, langFiles -> {
            LOGGER.info("Loading lang files...");

            FileHandler.readDirectoryAndSubDirectories(LANG_PATH.toFile(), directory -> {
                if (!Files.exists(LANG_PATH)) {
                    return;
                }

                try (Stream<Path> paths = Files.list(directory.toPath())) {
                    paths.filter(path -> path.toString().endsWith(".json"))
                            .forEach(path -> {
                                String langName = path.getFileName().toString().replace(".json", "");
                                LOGGER.debug("Found localisation file for {}", langName);

                                try {
                                    JsonObject jsonObject = GsonHelper.parse(new FileReader(path.toFile()));
                                    langFiles.put(langName, jsonObject);
                                }
                                catch (FileNotFoundException e) {
                                    LOGGER.error("Couldn't load file {}!", path, e);
                                }
                            });
                }
                catch (IOException e) {
                    LOGGER.warn("Can't list files of directory: {}", directory, e);
                }
            });

            logLoadStatus(langFiles.size(), "lang file");

            return langFiles;
        }, FileHandler.IO_POOL);

        this.langFilesFuture.completeOnTimeout(5, TimeUnit.SECONDS);
    }

    public void clear() {
        this.langFilesFuture = PartialCompletableFuture.completedFuture(new ConcurrentHashMap<>());
    }

    public void sendLangDataToClient(Consumer<CustomPacketPayload> sender) {
        this.langFilesFuture.thenAccept(map -> sender.accept(new SyncLangDataPayload(map)));
    }
}
