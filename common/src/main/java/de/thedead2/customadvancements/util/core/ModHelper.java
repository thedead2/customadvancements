package de.thedead2.customadvancements.util.core;

import com.google.common.collect.Lists;
import de.thedead2.customadvancements.advancements.CustomAdvancement;
import de.thedead2.customadvancements.advancements.CustomAdvancementManager;
import de.thedead2.customadvancements.platform.IPlatformHelper;
import de.thedead2.customadvancements.util.ReflectionHelper;
import de.thedead2.customadvancements.util.ResourceManagerExtender;
import de.thedead2.customadvancements.util.exceptions.ExceptionHandler;
import de.thedead2.customadvancements.util.io.FileHandler;
import de.thedead2.customadvancements.util.io.JsonHandler;
import de.thedead2.customadvancements.util.io.LanguageHandler;
import de.thedead2.customadvancements.util.io.TextureHandler;
import de.thedead2.customadvancements.util.localisation.TranslationKeyProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.world.level.storage.WorldData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import java.io.File;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Supplier;


public class ModHelper {

    public static final IPlatformHelper PLATFORM = load(IPlatformHelper.class);


    public static final String MOD_ID = "customadvancements";

    public static final char PATH_SEPARATOR = File.separatorChar;

    public static final Path DIR_PATH = PLATFORM.getGameDirectory().resolve(MOD_ID);

    public static final Path DATA_PATH = DIR_PATH.resolve("data");

    public static final Path TEXTURES_PATH = DATA_PATH.resolve("textures");

    public static final Path LANG_PATH = DATA_PATH.resolve("lang");

    public static final Path CUSTOM_ADVANCEMENTS_PATH = DIR_PATH.resolve(MOD_ID);

    public static final ModProperties MOD_PROPERTIES = ModProperties.fromInputStream(ReflectionHelper.findResource("META-INF/mod.properties"));

    public static final String MOD_VERSION = MOD_PROPERTIES.getProperty("mod_version");

    public static final String MOD_NAME = MOD_PROPERTIES.getProperty("mod_name");

    public static final String MOD_UPDATE_LINK = MOD_PROPERTIES.getProperty("mod_update_link");

    public static final String MOD_ISSUES_LINK = MOD_PROPERTIES.getProperty("mod_issues_link");


    public static final Map<ResourceLocation, CustomAdvancement> CUSTOM_ADVANCEMENTS = new HashMap<>();


    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    public static final Supplier<Boolean> BA_COMPATIBILITY = () -> PLATFORM.isModLoaded("betteradvancements");


    private static <T> T load(Class<T> clazz) {
        final T loadedService = ServiceLoader.load(clazz).findFirst().orElseThrow(() -> new NullPointerException("Failed to load service for " + clazz.getName()));
        LOGGER.debug("Loaded {} for service {}", loadedService, clazz);

        return loadedService;
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

            init();
            reloadGameData(server);

            LOGGER.info("Reload completed in {} ms!", System.currentTimeMillis() - startTime);
        }
        catch (Exception e) {
            ExceptionHandler.getInstance().log("Reload failed", e, Level.ERROR);
        }
    }


    public static void init() {
        clearAll();
        FileHandler.checkForMainDirectories();

        PLATFORM.executeOnClient(TextureHandler::loadTextureFiles);
        JsonHandler.loadAdvancementFiles();
        LanguageHandler.loadLangFiles();

        logLoadStatus(CUSTOM_ADVANCEMENTS.size(), "custom advancement");
        logLoadStatus(LanguageHandler.size(), "localisation file");
        logLoadStatus(ResourceManagerExtender.getResourcesCount(), "additional resource");
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
            server.sendSystemMessage(TranslationKeyProvider.chatMessage("reload_failed_message", ChatFormatting.RED));
            ExceptionHandler.getInstance().log("Failed to execute reload!", e, Level.ERROR);

            return null;
        });
    }


    private static void clearAll() {
        CUSTOM_ADVANCEMENTS.clear();
        CustomAdvancementManager.clearAll();
        ResourceManagerExtender.clear();
        ExceptionHandler.getInstance().reset();
    }


    private static void logLoadStatus(int size, String name) {
        if (size != 0) {
            LOGGER.info("Loaded {} {}{}", size, name, size != 1 ? "s!" : "!");
        }
    }
}
