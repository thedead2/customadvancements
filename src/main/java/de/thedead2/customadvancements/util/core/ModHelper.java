package de.thedead2.customadvancements.util.core;

import com.google.common.collect.Lists;
import de.thedead2.customadvancements.CustomAdvancements;
import de.thedead2.customadvancements.advancements.CustomAdvancement;
import de.thedead2.customadvancements.advancements.CustomAdvancementManager;
import de.thedead2.customadvancements.util.ReflectionHelper;
import de.thedead2.customadvancements.util.ResourceManagerExtender;
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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.util.thread.EffectiveSide;
import net.minecraftforge.forgespi.locating.IModFile;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Supplier;


public class ModHelper {

    public static final String MOD_ID = "customadvancements";

    public static final Path GAME_DIR = FMLPaths.GAMEDIR.get();

    public static final char PATH_SEPARATOR = File.separatorChar;

    public static final Path DIR_PATH = GAME_DIR.resolve(MOD_ID);

    public static final Path DATA_PATH = DIR_PATH.resolve("data");

    public static final Path TEXTURES_PATH = DATA_PATH.resolve("textures");

    public static final Path LANG_PATH = DATA_PATH.resolve("lang");

    public static final Path CUSTOM_ADVANCEMENTS_PATH = DIR_PATH.resolve(MOD_ID);

    public static final Map<ResourceLocation, CustomAdvancement> CUSTOM_ADVANCEMENTS = new HashMap<>();

    public static final Deque<String> WARNINGS = new ArrayDeque<>();

    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public static final ModProperties MOD_PROPERTIES = ModProperties.fromInputStream(ReflectionHelper.findResource("META-INF/mod.properties"));

    public static final String MOD_VERSION = MOD_PROPERTIES.getProperty("mod_version");

    public static final String MOD_NAME = MOD_PROPERTIES.getProperty("mod_name");

    public static final String MOD_UPDATE_LINK = MOD_PROPERTIES.getProperty("mod_update_link");

    public static final String MOD_ISSUES_LINK = MOD_PROPERTIES.getProperty("mod_issues_link");

    public static final String MAIN_PACKAGE = CustomAdvancements.class.getPackageName();

    public static final Supplier<Boolean> BA_COMPATIBILITY = () -> ModList.get().isLoaded("betteradvancements");


    public static boolean isDevEnv() {
        return !FMLLoader.isProduction();
    }


    public static ModContainer getModContainerFor(String id) {
        return ModList.get().getModContainerById(id).orElseThrow(() -> new RuntimeException("Unable to retrieve ModContainer for id: " + id));
    }


    public static IModFile getModFileFor(String id) {
        return ModList.get().getModFileById(id).getFile();
    }


    public static Optional<MinecraftServer> getServer() {
        return Optional.ofNullable(ServerLifecycleHooks.getCurrentServer());
    }


    public static boolean isRunningOnServerThread() {
        return EffectiveSide.get().isServer();
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
            CrashHandler.getInstance().handleException("Reload failed", e, Level.ERROR);
        }
    }


    public static void init() {
        clearAll();
        FileHandler.checkForMainDirectories();

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> TextureHandler::loadTextureFiles);
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
            CrashHandler.getInstance().handleException("Failed to execute reload!", e, Level.ERROR);

            return null;
        });
    }


    private static void clearAll() {
        CUSTOM_ADVANCEMENTS.clear();
        CustomAdvancementManager.clearAll();
        ResourceManagerExtender.clear();
        CrashHandler.getInstance().reset();
    }


    private static void logLoadStatus(int size, String name) {
        if (size != 0) {
            LOGGER.info("Loaded {} {}{}", size, name, size != 1 ? "s!" : "!");
        }
    }
}
