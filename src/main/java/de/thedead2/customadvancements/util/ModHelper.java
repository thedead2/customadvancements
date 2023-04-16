package de.thedead2.customadvancements.util;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.gson.JsonElement;
import de.thedead2.customadvancements.advancements.advancementtypes.CustomAdvancement;
import de.thedead2.customadvancements.advancements.advancementtypes.GameAdvancement;
import de.thedead2.customadvancements.util.exceptions.CrashHandler;
import de.thedead2.customadvancements.util.handler.FileHandler;
import de.thedead2.customadvancements.util.handler.JsonHandler;
import de.thedead2.customadvancements.util.handler.LanguageHandler;
import de.thedead2.customadvancements.util.handler.TextureHandler;
import de.thedead2.customadvancements.util.language.TranslationKeyProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.world.level.storage.WorldData;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.forgespi.locating.IModFile;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static de.thedead2.customadvancements.advancements.CustomAdvancementManager.ADVANCEMENTS;


public abstract class ModHelper {

    public static final String MOD_VERSION = "1.19.2-5.7.3";
    public static final String MOD_ID = "customadvancements";
    public static final String MOD_NAME = "Custom Advancements";
    public static final String MOD_UPDATE_LINK = "https://www.curseforge.com/minecraft/mc-mods/custom-advancements/files";
    public static final String MOD_ISSUES_LINK = "https://github.com/thedead2/customadvancements/issues";

    public static final IModFile THIS_MOD_FILE = ModList.get().getModFileById(MOD_ID).getFile();
    public static final ModContainer THIS_MOD_CONTAINER = ModList.get().getModContainerById(MOD_ID).orElseThrow(() -> new RuntimeException("Unable to retrieve ModContainer for id: " + MOD_ID));
    public static MinecraftServer server = null;

    public static final Path GAME_DIR = FMLPaths.GAMEDIR.get();
    public static final char PATH_SEPARATOR = File.separatorChar;
    public static final Path DIR_PATH = GAME_DIR.resolve(MOD_ID);
    public static final Path DATA_PATH = DIR_PATH.resolve("data");
    public static final Path CUSTOM_ADVANCEMENTS_PATH = DIR_PATH.resolve(MOD_ID);
    public static final Path TEXTURES_PATH = DATA_PATH.resolve("textures");
    public static final Path LANG_PATH = DATA_PATH.resolve("lang");

    public static final Map<ResourceLocation, CustomAdvancement> CUSTOM_ADVANCEMENTS = new HashMap<>();
    public static final Map<ResourceLocation, GameAdvancement> GAME_ADVANCEMENTS = new HashMap<>();
    public static final Map<ResourceLocation, JsonElement> ALL_DETECTED_GAME_ADVANCEMENTS = new HashMap<>();

    public static final Multimap<ResourceLocation, ResourceLocation> PARENT_CHILDREN_MAP = ArrayListMultimap.create();
    public static final Map<ResourceLocation, ResourceLocation> CHILDREN_PARENT_MAP = new HashMap<>();
    public static final Collection<ResourceLocation> ALL_ADVANCEMENTS_RESOURCE_LOCATIONS = new HashSet<>();

    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public static void reloadAll(MinecraftServer server){
        Timer timer = new Timer(true);
        LOGGER.info("Reloading...");

        init();
        reloadGameData(server);

        LOGGER.info("Reload completed in {} ms!", timer.getTime());
        timer.stop(true);
    }


    public static void init(){
        clearAll();
        FileHandler.checkForMainDirectories();

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> TextureHandler.getInstance().start());
        JsonHandler.getInstance().start();
        LanguageHandler.getInstance().start();

        logLoadStatus(CUSTOM_ADVANCEMENTS.size(), "Custom Advancement");
        logLoadStatus(GAME_ADVANCEMENTS.size(), "Game Advancement");
        logLoadStatus(LanguageHandler.size(), "Language File");
        logLoadStatus(ResourceManagerExtender.getResourcesCount(), "additional resource");
    }

    private static void logLoadStatus(int size, String name){
        if(size != 0)
            LOGGER.info("Loaded " + size + " " + name + (size != 1 ? "s!" : "!"));
    }


    private static void clearAll(){
        CUSTOM_ADVANCEMENTS.clear();
        GAME_ADVANCEMENTS.clear();
        ALL_DETECTED_GAME_ADVANCEMENTS.clear();
        PARENT_CHILDREN_MAP.clear();
        CHILDREN_PARENT_MAP.clear();
        ALL_ADVANCEMENTS_RESOURCE_LOCATIONS.clear();
        ADVANCEMENTS.clear();
        ResourceManagerExtender.clear();
        CrashHandler.getInstance().reset();

    }


    private static void reloadGameData(MinecraftServer server){
        PackRepository packRepository = server.getPackRepository();
        WorldData worldData = server.getWorldData();

        packRepository.reload();
        Collection<String> selectedIds = Lists.newArrayList(packRepository.getSelectedIds());
        Collection<String> disabledPacks = worldData.getDataPackConfig().getDisabled();

        for(String ids : packRepository.getAvailableIds()) {
            if (!disabledPacks.contains(ids) && !selectedIds.contains(ids)) {
                selectedIds.add(ids);
            }
        }

        server.reloadResources(selectedIds).exceptionally((e) -> {
            server.sendSystemMessage(TranslationKeyProvider.chatMessage("reload_failed_message", ChatFormatting.RED));
            CrashHandler.getInstance().handleException("Failed to execute reload!", e, Level.ERROR, true);
            return null;
        });
    }
}
