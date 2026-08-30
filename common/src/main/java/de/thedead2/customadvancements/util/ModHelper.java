package de.thedead2.customadvancements.util;

import com.google.common.collect.Lists;
import de.thedead2.customadvancements.CAMain;
import de.thedead2.customadvancements.platform.IPlatformHelper;
import de.thedead2.customadvancements.util.localisation.ModTranslationKeys;
import net.minecraft.ChatFormatting;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.world.level.storage.WorldData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.util.*;


public class ModHelper {

    public static final String MOD_NAME = "Custom Advancements";

    public static final String MOD_ID = "customadvancements";

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    public static final IPlatformHelper PLATFORM = load(IPlatformHelper.class);


    public static final char PATH_SEPARATOR = File.separatorChar;

    public static final Path DIR_PATH = PLATFORM.getGameDirectory().resolve(MOD_ID);

    public static final Path DATA_PATH = DIR_PATH.resolve("data");


    private static <T> T load(Class<T> clazz) {
        final T loadedService = ServiceLoader.load(clazz).findFirst().orElseThrow(() -> new NullPointerException("Failed to load service for " + clazz.getName()));
        LOGGER.debug("Loaded {} for service {}", loadedService, clazz);

        return loadedService;
    }

    public static void logLoadStatus(int size, String name) {
        if (size != 0) {
            LOGGER.info("Loaded {} {}{}", size, name, size != 1 ? "s!" : "!");
        }
    }


    public static boolean isDevEnv() {
        return PLATFORM.isDevelopmentEnvironment();
    }

    public static Optional<MinecraftServer> getServer() {
        return PLATFORM.getServer();
    }


    public static void reloadAll(MinecraftServer server) {
        try {
            long startTime = System.currentTimeMillis();

            LOGGER.info("Reloading...");

            CAMain.getInstance().loadData();
            reloadGameData(server);

            LOGGER.info("Reload completed in {} ms!", System.currentTimeMillis() - startTime);
        }
        catch (Exception e) {
            LOGGER.error("Reload failed", e);
        }
    }


    private static void reloadGameData(MinecraftServer server) {
        PackRepository packRepository = server.getPackRepository();
        WorldData worldData = server.getWorldData();

        packRepository.reload();

        Collection<String> selectedIds = Lists.newArrayList(packRepository.getSelectedIds());
        Collection<String> disabledPacks = worldData.getDataConfiguration().dataPacks().getDisabled();

        for (String ids : packRepository.getAvailableIds()) {
            if (!disabledPacks.contains(ids) && !selectedIds.contains(ids)) {
                selectedIds.add(ids);
            }
        }

        server.reloadResources(selectedIds).exceptionally((e) -> {
            server.sendSystemMessage(ModTranslationKeys.chatMessage(ModTranslationKeys.RELOAD_FAILED_MESSAGE, ChatFormatting.RED));
            LOGGER.error("Failed to execute reload!", e);

            return null;
        });
    }
}
