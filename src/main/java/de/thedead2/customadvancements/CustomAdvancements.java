package de.thedead2.customadvancements;

import de.thedead2.customadvancements.advancements.progression.AdvancementProgressionMode;
import de.thedead2.customadvancements.commands.ModCommand;
import de.thedead2.customadvancements.util.core.ConfigManager;
import de.thedead2.customadvancements.util.Timer;
import de.thedead2.customadvancements.util.core.VersionManager;
import de.thedead2.customadvancements.util.core.CrashHandler;
import de.thedead2.customadvancements.util.logger.MissingAdvancementFilter;
import de.thedead2.customadvancements.util.logger.UnknownAdvancementFilter;
import de.thedead2.customadvancements.util.logger.UnknownRecipeCategoryFilter;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.CrashReportCallables;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static de.thedead2.customadvancements.util.core.ModHelper.*;

@Mod(MOD_ID)
public class CustomAdvancements {

    public static final String MAIN_PACKAGE = CustomAdvancements.class.getPackageName();

    public CustomAdvancements() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::setup);
        modEventBus.addListener(this::onLoadComplete);
        modEventBus.addListener(this::onConfigChanged);

        ModLoadingContext loadingContext = ModLoadingContext.get();
        loadingContext.registerConfig(ModConfig.Type.COMMON, ConfigManager.CONFIG_SPEC, MOD_ID + "-common.toml");

        IEventBus forgeEventBus = MinecraftForge.EVENT_BUS;
        forgeEventBus.addListener(this::onCommandsRegister);
        forgeEventBus.addListener(this::onPlayerLogin);
        forgeEventBus.addListener(this::onPlayerDeath);
        forgeEventBus.addListener(this::onServerStopped);
        forgeEventBus.register(this);

        registerLoggerFilter();
    }


    private void setup(final FMLCommonSetupEvent event) {
        Timer timer = new Timer(true);

        LOGGER.info("Starting " + MOD_NAME + ", Version: " + MOD_VERSION);

        init();

        LOGGER.info("Loading completed in {} ms.", timer.getTime());
        timer.stop(true);
    }


    private void onLoadComplete(final FMLLoadCompleteEvent event){
        VersionManager.sendLoggerMessage();
    }


    private void onPlayerLogin(final PlayerEvent.PlayerLoggedInEvent event) {
        if(ConfigManager.OUT_DATED_MESSAGE.get() && !isDevEnv()){
            VersionManager.sendChatMessage(event.getEntity());
        }
    }

    private void onPlayerDeath(final PlayerEvent.PlayerRespawnEvent event){
        if(ConfigManager.RESET_ADVANCEMENTS_ON_DEATH.get()){
            AdvancementProgressionMode.resetAdvancementProgress((ServerPlayer) event.getEntity());
        }
    }

    private void onServerStopped(final ServerStoppedEvent event){
        setServer(null);
    }


    private void onCommandsRegister(final RegisterCommandsEvent event){
        ModCommand.registerCommands(event.getDispatcher());
    }

    private void onConfigChanged(final ModConfigEvent event){
        ModConfig config = event.getConfig();
        if(config.getModId().equals(MOD_ID)){
            getServer().ifPresent((server) -> {
                LOGGER.debug("Config just changed! Attempting to reload...");
                reloadAll(server);
            });
        }
    }


    private void registerLoggerFilter(){
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

    static {
        CrashReportCallables.registerCrashCallable(CrashHandler.getInstance());
    }
}
