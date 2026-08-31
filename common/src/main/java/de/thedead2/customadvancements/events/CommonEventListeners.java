package de.thedead2.customadvancements.events;

import de.thedead2.customadvancements.CAMain;
import de.thedead2.customadvancements.advancements.AdvancementProgressionMode;
import de.thedead2.customadvancements.util.ConfigManager;
import de.thedead2.mc_libs.io.FileHandler;
import net.minecraft.server.level.ServerPlayer;

import static de.thedead2.customadvancements.advancements.CustomAdvancementManager.CUSTOM_ADVANCEMENTS_PATH;
import static de.thedead2.customadvancements.util.ModHelper.*;
import static de.thedead2.customadvancements.util.ModHelper.LOGGER;
import static de.thedead2.customadvancements.data.LanguageHandler.LANG_PATH;
import static de.thedead2.customadvancements.data.TextureHandler.TEXTURES_PATH;

public class CommonEventListeners {

    public static void onCommonSetup() {
        LOGGER.info("Starting {}, Version: {}", MOD_NAME, PLATFORM.getModVersion(MOD_ID));

        FileHandler.createDirectoryIfNecessary(DIR_PATH.toFile(), LOGGER);
        FileHandler.copyModFilesIfNecessary(CUSTOM_ADVANCEMENTS_PATH, "/examples/advancements", ".json", LOGGER);

        FileHandler.createDirectoryIfNecessary(DATA_PATH.toFile(), LOGGER);
        FileHandler.copyModFilesIfNecessary(TEXTURES_PATH, "/examples/data/textures", ".png", LOGGER);
        FileHandler.copyModFilesIfNecessary(LANG_PATH, "/examples/data/lang", ".json", LOGGER);
    }

    public static void onServerStart() {
        CAMain.getInstance().loadData();
    }

    public static void onPlayerDeath(ServerPlayer player) {
        if (ConfigManager.RESET_ADVANCEMENTS_ON_DEATH.get()) {
            AdvancementProgressionMode.resetAdvancementProgress(player);
        }
    }

    public static void onServerStop() {
        CAMain.getInstance().clearLoadingStates();
    }
}
