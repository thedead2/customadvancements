package de.thedead2.customadvancements.events;

import de.thedead2.customadvancements.advancements.AdvancementProgressionMode;
import de.thedead2.customadvancements.util.core.ConfigManager;
import net.minecraft.server.level.ServerPlayer;

import static de.thedead2.customadvancements.util.core.ModHelper.*;
import static de.thedead2.customadvancements.util.core.ModHelper.LOGGER;
import static de.thedead2.customadvancements.util.core.ModHelper.init;

public class CommonEventListeners {

    public static void onCommonSetup() {
        long startTime = System.currentTimeMillis();

        LOGGER.info("Starting {}, Version: {}", MOD_NAME, MOD_VERSION);

        /*if (BA_COMPATIBILITY.get()) {
            LOGGER.info("Found BetterAdvancements to be present! Enabling compatibility mode...");
        }*/

        init();

        LOGGER.info("Loading completed in {} ms.", System.currentTimeMillis() - startTime);
    }

    public static void onPlayerDeath(ServerPlayer player) {
        if (ConfigManager.RESET_ADVANCEMENTS_ON_DEATH.get()) {
            AdvancementProgressionMode.resetAdvancementProgress(player);
        }
    }

    public static void onGameShutdown() {
        ConfigManager.resetDebugMode();
    }
}
