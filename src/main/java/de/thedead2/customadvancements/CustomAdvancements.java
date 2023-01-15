package de.thedead2.customadvancements;

import de.thedead2.customadvancements.commands.GenerateGameAdvancementsCommand;
import de.thedead2.customadvancements.commands.GenerateResourceLocationsFileCommand;
import de.thedead2.customadvancements.commands.ReloadCommand;
import de.thedead2.customadvancements.util.logger.MissingAdvancementFilter;
import de.thedead2.customadvancements.util.logger.UnknownRecipeCategoryFilter;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.server.command.ConfigCommand;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static de.thedead2.customadvancements.util.ModHelper.*;

@Mod(MOD_ID)
public class CustomAdvancements {


    public CustomAdvancements() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onLoadComplete);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ConfigManager.CONFIG_SPEC, MOD_ID + "-common.toml");

        MinecraftForge.EVENT_BUS.addListener(this::onCommandsRegister);
        MinecraftForge.EVENT_BUS.addListener(this::onPlayerLogin);
        MinecraftForge.EVENT_BUS.register(this);


        Logger rootLogger = LogManager.getRootLogger();
        if (rootLogger instanceof org.apache.logging.log4j.core.Logger logger) {
            logger.addFilter(new MissingAdvancementFilter());
            logger.addFilter(new UnknownRecipeCategoryFilter());
        }
        else {
            LOGGER.error("Unable to register filter for Logger with class {}", rootLogger.getClass());
        }
    }

    private void setup(final FMLCommonSetupEvent event) {
        LOGGER.info("Starting " + MOD_NAME + ", Version: " + MOD_VERSION);

        init();

        LOGGER.info("Loading complete.");
    }

    private void onLoadComplete(final FMLLoadCompleteEvent event){
        VersionManager.sendLoggerMessage();
    }

    private void onPlayerLogin(final PlayerEvent.PlayerLoggedInEvent event) {
        if(ConfigManager.OUT_DATED_MESSAGE.get()){
            VersionManager.sendChatMessage(event.getPlayer());
        }
    }

    private void onCommandsRegister(final RegisterCommandsEvent event){
        LOGGER.debug("Registering commands...");
        new GenerateGameAdvancementsCommand(event.getDispatcher());
        new GenerateResourceLocationsFileCommand(event.getDispatcher());
        new ReloadCommand(event.getDispatcher());

        ConfigCommand.register(event.getDispatcher());
        LOGGER.debug("Command registration complete.");
    }
}
