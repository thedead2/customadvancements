package de.thedead2.customadvancements.events;

import de.thedead2.customadvancements.CAMain;
import de.thedead2.customadvancements.advancements.AdvancementProgressionMode;
import de.thedead2.customadvancements.util.core.ConfigManager;
import de.thedead2.customadvancements.util.io.FileHandler;
import net.minecraft.server.level.ServerPlayer;

import static de.thedead2.customadvancements.util.core.ModHelper.*;
import static de.thedead2.customadvancements.util.core.ModHelper.LOGGER;

public class CommonEventListeners {

    public static void onCommonSetup() {
        LOGGER.info("Starting {}, Version: {}", MOD_NAME, PLATFORM.getModVersion(MOD_ID));

        FileHandler.checkForMainDirectories();
    }

    public static void onServerStart() {
        CAMain.getInstance().loadData();
    }

    public static void onPlayerDeath(ServerPlayer player) {
        if (ConfigManager.RESET_ADVANCEMENTS_ON_DEATH.get()) {
            AdvancementProgressionMode.resetAdvancementProgress(player);
        }
    }

    public static void onGameShutdown() {
        ConfigManager.resetDebugMode();
    }

    public static void onServerStop() {
        CAMain.getInstance().clearLoadingStates();
    }
}
