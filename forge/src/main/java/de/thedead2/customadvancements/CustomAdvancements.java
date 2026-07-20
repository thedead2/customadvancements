package de.thedead2.customadvancements;

import de.thedead2.customadvancements.commands.ModCommand;
import de.thedead2.customadvancements.events.CommonEventListeners;
import de.thedead2.customadvancements.util.core.ConfigManager;
import de.thedead2.customadvancements.util.logging.MissingAdvancementFilter;
import de.thedead2.customadvancements.util.logging.UnknownAdvancementFilter;
import de.thedead2.customadvancements.util.logging.UnknownRecipeCategoryFilter;
import fuzs.forgeconfigapiport.forge.api.neoforge.v4.NeoForgeConfigRegistry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static de.thedead2.customadvancements.util.core.ModHelper.*;


/*
 * Mod Workflow:
 * on Servers: Load Advancement and lang files and inject them into the Language- and AdvancementManagers. Sync them with the client and accept commands --> Handles logic
 * on Clients: Accept the synced advancements from the server, hold a Map of ids with BackgroundRenderers for rendering the advancement screen --> Handles rendering
 * */
@Mod(MOD_ID)
public class CustomAdvancements {

    static {
        //CrashReportCallables.registerCrashCallable(CrashHandler.getInstance());
    }


    public CustomAdvancements() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::setup);
        modEventBus.addListener(this::onConfigChanged);

        // Register Config through Forge Config API Port
        NeoForgeConfigRegistry.INSTANCE.register(MOD_ID, ModConfig.Type.COMMON, ConfigManager.CONFIG_SPEC, MOD_ID + "-common.toml");

        IEventBus forgeEventBus = MinecraftForge.EVENT_BUS;
        forgeEventBus.addListener(this::onCommandsRegister);
        forgeEventBus.addListener(this::onPlayerDeath);

        registerLoggerFilter();
    }


    private void setup(final FMLCommonSetupEvent event) {
        CommonEventListeners.onCommonSetup();
    }


    private void onConfigChanged(final ModConfigEvent event) {
        ModConfig config = event.getConfig();

        if (config.getModId().equals(MOD_ID)) {
            getServer().ifPresent((server) -> server.executeIfPossible(() -> {
                LOGGER.info("Config just changed! Attempting to reload...");
                reloadAll(server);
            }));
        }
    }


    private void onCommandsRegister(final RegisterCommandsEvent event) {
        ModCommand.registerCommands(event.getDispatcher());
    }


    private void onPlayerDeath(final PlayerEvent.PlayerRespawnEvent event) {
        CommonEventListeners.onPlayerDeath((ServerPlayer) event.getEntity());
    }


    private void registerLoggerFilter() {
        Logger rootLogger = LogManager.getRootLogger();

        if (rootLogger instanceof org.apache.logging.log4j.core.Logger logger) {
            logger.addFilter(new MissingAdvancementFilter());
            logger.addFilter(new UnknownRecipeCategoryFilter());
            logger.addFilter(new UnknownAdvancementFilter());
        }
        else {
            LOGGER.error("Unable to register filter for Logger with unexpected class: {}", rootLogger.getClass().getName());
        }
    }
}
