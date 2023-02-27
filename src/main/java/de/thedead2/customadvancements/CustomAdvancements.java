package de.thedead2.customadvancements;

import com.mojang.brigadier.CommandDispatcher;
import de.thedead2.customadvancements.commands.*;
import de.thedead2.customadvancements.util.handler.CrashHandler;
import de.thedead2.customadvancements.util.logger.MissingAdvancementFilter;
import de.thedead2.customadvancements.util.logger.UnknownRecipeCategoryFilter;
import net.minecraft.commands.CommandSourceStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.CrashReportCallables;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.server.command.ConfigCommand;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static de.thedead2.customadvancements.util.ModHelper.*;

@Mod(MOD_ID)
public class CustomAdvancements {

    public CustomAdvancements() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::setup);
        modEventBus.addListener(this::onLoadComplete);

        ModLoadingContext loadingContext = ModLoadingContext.get();
        loadingContext.registerConfig(ModConfig.Type.COMMON, ConfigManager.CONFIG_SPEC, MOD_ID + "-common.toml");

        IEventBus forgeEventBus = MinecraftForge.EVENT_BUS;
        forgeEventBus.addListener(this::onCommandsRegister);
        forgeEventBus.addListener(this::onPlayerLogin);
        forgeEventBus.register(this);

        CrashReportCallables.registerCrashCallable(CrashHandler.getInstance());
        registerLoggerFilter();
    }


    private void setup(final FMLCommonSetupEvent event) {
        StopWatch timer = new StopWatch();

        timer.start();
        LOGGER.info("Starting " + MOD_NAME + ", Version: " + MOD_VERSION);
        LOGGER.debug("Registered PATH_SEPARATOR with: " + PATH_SEPARATOR);

        init();

        LOGGER.info("Loading completed in {} ms.", timer.getTime());
        timer.stop();
        timer.reset();
    }


    private void onLoadComplete(final FMLLoadCompleteEvent event){
        VersionManager.sendLoggerMessage();
    }


    private void onPlayerLogin(final PlayerEvent.PlayerLoggedInEvent event) {
        if(ConfigManager.OUT_DATED_MESSAGE.get()){
            VersionManager.sendChatMessage(event.getEntity());
        }
    }


    private void onCommandsRegister(final RegisterCommandsEvent event){
        LOGGER.debug("Registering commands...");
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        new GenerateGameAdvancementsCommand(dispatcher);
        new GenerateResourceLocationsFileCommand(dispatcher);
        new ReloadCommand(dispatcher);
        new GenerateAdvancementCommand(dispatcher);

        ConfigCommand.register(dispatcher);
        LOGGER.debug("Command registration complete.");
    }


    private void registerLoggerFilter(){
        Logger rootLogger = LogManager.getRootLogger();

        if (rootLogger instanceof org.apache.logging.log4j.core.Logger logger) {
            logger.addFilter(new MissingAdvancementFilter());
            logger.addFilter(new UnknownRecipeCategoryFilter());
        }
        else {
            LOGGER.error("Unable to register filter for Logger with unexpected class: {}", rootLogger.getClass().getName());
        }
    }
}
