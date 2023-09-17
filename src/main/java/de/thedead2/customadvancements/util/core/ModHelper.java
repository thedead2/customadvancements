package de.thedead2.customadvancements.util.core;

import com.google.common.collect.Lists;
import de.thedead2.customadvancements.advancements.CustomAdvancementManager;
import de.thedead2.customadvancements.advancements.advancementtypes.CustomAdvancement;
import de.thedead2.customadvancements.advancements.advancementtypes.GameAdvancement;
import de.thedead2.customadvancements.util.ResourceManagerExtender;
import de.thedead2.customadvancements.util.Timer;
import de.thedead2.customadvancements.util.handler.JsonHandler;
import de.thedead2.customadvancements.util.handler.LanguageHandler;
import de.thedead2.customadvancements.util.handler.TextureHandler;
import net.minecraft.resources.ResourcePackList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.storage.IServerConfiguration;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import net.minecraftforge.forgespi.locating.IModFile;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


public abstract class ModHelper {
    public static final String MOD_ID = "customadvancements";
    public static final IModFile THIS_MOD_FILE = ModList.get().getModFileById(MOD_ID).getFile();
    public static final ModContainer THIS_MOD_CONTAINER = ModList.get().getModContainerById(MOD_ID).orElseThrow(() -> new RuntimeException("Unable to retrieve ModContainer for id: " + MOD_ID));
    public static final ModProperties MOD_PROPERTIES = ModProperties.fromPath(THIS_MOD_FILE.findResource("META-INF/mod.properties"));

    public static final String MOD_VERSION = MOD_PROPERTIES.getProperty("mod_version");
    public static final String MOD_NAME = "Custom Advancements";
    public static final String MOD_UPDATE_LINK = "https://www.curseforge.com/minecraft/mc-mods/custom-advancements/files";
    public static final String MOD_ISSUES_LINK = "https://github.com/thedead2/customadvancements/issues";

    public static final Path GAME_DIR = FMLPaths.GAMEDIR.get();
    public static final char PATH_SEPARATOR = File.separatorChar;
    public static final Path DIR_PATH = GAME_DIR.resolve(MOD_ID);
    public static final Path DATA_PATH = DIR_PATH.resolve("data");
    public static final Path CUSTOM_ADVANCEMENTS_PATH = DIR_PATH.resolve(MOD_ID);
    public static final Path TEXTURES_PATH = DATA_PATH.resolve("textures");
    public static final Path LANG_PATH = DATA_PATH.resolve("lang");

    public static final Map<ResourceLocation, CustomAdvancement> CUSTOM_ADVANCEMENTS = new HashMap<>();
    public static final Map<ResourceLocation, GameAdvancement> GAME_ADVANCEMENTS = new HashMap<>();

    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public static boolean isDevEnv(){
        return FMLLoader.launcherHandlerName().equals("fmluserdevclient") || FMLLoader.launcherHandlerName().equals("fmluserdevserver");
    }

    public static Optional<MinecraftServer> getServer(){
        return Optional.ofNullable(ServerLifecycleHooks.getCurrentServer());
    }

    public static void reloadAll(MinecraftServer server){
        Thread caThread = new Thread(MOD_NAME){
            @Override
            public void run() {
                try {
                    de.thedead2.customadvancements.util.Timer timer = new Timer(true);
                    LOGGER.info("Reloading...");

                    init();

                    reloadGameData(server);

                    LOGGER.info("Reload completed in {} ms!", timer.getTime());
                    timer.stop(true);
                }
                catch (Exception e){
                    CrashHandler.getInstance().handleException("Reload failed", e, Level.ERROR, true);
                }
            }
        };
        caThread.setDaemon(true);
        caThread.setPriority(5);
        caThread.start();
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
        CustomAdvancementManager.clearAll();
        ResourceManagerExtender.clear();
        CrashHandler.getInstance().reset();

    }


    private static void reloadGameData(MinecraftServer server) {
        ResourcePackList resourcePackList = server.getResourcePacks();
        IServerConfiguration serverConfiguration = server.getServerConfiguration();
        Collection<String> ids = resourcePackList.func_232621_d_();

        resourcePackList.reloadPacksFromFinders();
        Collection<String> selectedIds = Lists.newArrayList(ids);
        Collection<String> disabledPacks = serverConfiguration.getDatapackCodec().getDisabled();

        for(String s : resourcePackList.func_232616_b_()) {
            if (!disabledPacks.contains(s) && !selectedIds.contains(s)) {
                selectedIds.add(s);
            }
        }

        server.func_240780_a_(selectedIds).exceptionally((e) -> {
            server.sendMessage(TranslationKeyProvider.chatMessage("reload_failed_message", TextFormatting.RED), Util.DUMMY_UUID);
            CrashHandler.getInstance().handleException("Failed to execute reload!", e, Level.ERROR, true);
            return null;
        });
    }

}
