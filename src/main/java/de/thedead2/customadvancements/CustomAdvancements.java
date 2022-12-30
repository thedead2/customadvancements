package de.thedead2.customadvancements;

import de.thedead2.customadvancements.util.miscellaneous.LoggerFilter;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

import static de.thedead2.customadvancements.util.ModHelper.*;

@Mod(MOD_ID)
public class CustomAdvancements {
    public static final Logger LOGGER = LogManager.getLogger();


    public CustomAdvancements() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onLoadComplete);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ConfigManager.CONFIG_SPEC, MOD_ID + "-common.toml");

        MinecraftForge.EVENT_BUS.addListener(this::onPlayerLogin);
        MinecraftForge.EVENT_BUS.register(this);


        Logger rootLogger = LogManager.getRootLogger();
        if (rootLogger instanceof org.apache.logging.log4j.core.Logger logger) {
            logger.addFilter(new LoggerFilter.MissingAdvancementFilter());
            logger.addFilter(new LoggerFilter.UnknownRecipeCategoryFilter());
        }
        else {
            LOGGER.error("Unable to register filter for Logger with class {}", rootLogger.getClass());
        }
    }

    private void setup(final FMLCommonSetupEvent event) {
        LOGGER.info("Starting " + MOD_NAME + ", Version: " + MOD_VERSION);

        FILE_HANDLER.getDirectory();
        if(!ConfigManager.NO_ADVANCEMENTS.get()){
            FILE_HANDLER.readFiles(new File(DIR_PATH));
        }
    }

    private void onLoadComplete(final FMLLoadCompleteEvent event){
        VersionManager.sendLoggerMessage();
    }

    private void onPlayerLogin(final PlayerEvent.PlayerLoggedInEvent event) {
        if(ConfigManager.OUT_DATED_MESSAGE.get()){
            VersionManager.sendChatMessage(event.getPlayer());
        }
    }
}
