package de.thedead2.customadvancements;

import de.thedead2.customadvancements.advancements.progression.AdvancementProgressionMode;
import de.thedead2.customadvancements.commands.ModCommand;
import de.thedead2.customadvancements.util.Timer;
import de.thedead2.customadvancements.util.core.ConfigManager;
import de.thedead2.customadvancements.util.core.CrashHandler;
import de.thedead2.customadvancements.util.core.VersionManager;
import de.thedead2.customadvancements.util.logger.MissingAdvancementFilter;
import de.thedead2.customadvancements.util.logger.UnknownAdvancementFilter;
import de.thedead2.customadvancements.util.logger.UnknownRecipeCategoryFilter;
import net.minecraft.advancements.Advancement;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.CrashReportCallables;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Method;

import static de.thedead2.customadvancements.util.core.ModHelper.*;

@Mod(MOD_ID)
public class CustomAdvancements {

    public static final String MAIN_PACKAGE = CustomAdvancements.class.getPackageName();

    public CustomAdvancements() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::setup);
        modEventBus.addListener(this::onConfigChanged);

        ModLoadingContext loadingContext = ModLoadingContext.get();
        loadingContext.registerConfig(ModConfig.Type.COMMON, ConfigManager.CONFIG_SPEC, MOD_ID + "-common.toml");

        IEventBus forgeEventBus = MinecraftForge.EVENT_BUS;
        forgeEventBus.addListener(this::onCommandsRegister);
        forgeEventBus.addListener(this::onPlayerDeath);

        if(isDevEnv()) {
            forgeEventBus.addListener(this::testSafetyMechanisms);
        }

        forgeEventBus.register(this);

        registerLoggerFilter();
        VersionManager.register(modEventBus, forgeEventBus);
    }


    private void setup(final FMLCommonSetupEvent event) {
        Timer timer = new Timer(true);

        LOGGER.info("Starting " + MOD_NAME + ", Version: " + MOD_VERSION);

        if(BA_COMPATIBILITY.get()) LOGGER.info("Found BetterAdvancements to be present! Enabling compatibility mode...");
        init();

        LOGGER.info("Loading completed in {} ms.", timer.getTime());
        timer.stop(true);
    }

    private void testSafetyMechanisms(final PlayerEvent.PlayerLoggedInEvent event) {
        if(event.getEntity() instanceof ServerPlayer player) {
            try {
                PlayerAdvancements playerAdvancements = player.getAdvancements();
                Class<PlayerAdvancements> playerAdvancementsClass = (Class<PlayerAdvancements>) playerAdvancements.getClass();

                LOGGER.debug("Testing {}.award()", playerAdvancements.getClass().getName());
                playerAdvancements.award(null, null);

                LOGGER.debug("Testing {}.revoke()", playerAdvancements.getClass().getName());
                playerAdvancements.revoke(null, null);

                LOGGER.debug("Testing {}.getOrStartProgress()", playerAdvancements.getClass().getName());
                playerAdvancements.getOrStartProgress(null);


                LOGGER.debug("Testing {}.registerListeners()", playerAdvancements.getClass().getName());
                Method registerListeners = playerAdvancementsClass.getDeclaredMethod("registerListeners", Advancement.class);
                registerListeners.setAccessible(true);
                registerListeners.invoke(playerAdvancements, (Advancement) null);

                LOGGER.debug("Testing {}.unregisterListeners()", playerAdvancements.getClass().getName());
                Method unregisterListeners = playerAdvancementsClass.getDeclaredMethod("unregisterListeners", Advancement.class);
                unregisterListeners.setAccessible(true);
                unregisterListeners.invoke(playerAdvancements, (Advancement) null);

                LOGGER.info("No errors while testing safety mechanisms!");
            }
            catch(Throwable e) {
                LOGGER.error("Testing failed!", e);
            }
        }
    }

    private void onPlayerDeath(final PlayerEvent.PlayerRespawnEvent event){
        if(ConfigManager.RESET_ADVANCEMENTS_ON_DEATH.get()){
            AdvancementProgressionMode.resetAdvancementProgress((ServerPlayer) event.getEntity());
        }
    }


    private void onCommandsRegister(final RegisterCommandsEvent event){
        ModCommand.registerCommands(event.getDispatcher());
    }

    private void onConfigChanged(final ModConfigEvent event){
        ModConfig config = event.getConfig();
        if(config.getModId().equals(MOD_ID)){
            getServer().ifPresent((server) -> {
                LOGGER.info("Config just changed! Attempting to reload...");
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
