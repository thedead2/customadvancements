package de.thedead2.customadvancements;

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

import static de.thedead2.customadvancements.util.ModHelper.*;

@Mod(MOD_ID)
public class CustomAdvancements {
    public static final Logger LOGGER = LogManager.getLogger();


    public CustomAdvancements() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onLoadComplete);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, SPEC, MOD_ID + "-common.toml");

        MinecraftForge.EVENT_BUS.addListener(this::onPlayerLogin);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(final FMLCommonSetupEvent event) {
        LOGGER.info("Starting " + MOD_NAME + ", Version: " + MOD_VERSION);

        FILE_HANDLER.getDirectory();
        FILE_HANDLER.readFiles();
    }

    private void onLoadComplete(final FMLLoadCompleteEvent event){
        sendLoggerMessage();
    }

    private void onPlayerLogin(final PlayerEvent.PlayerLoggedInEvent event) {
        if (OUT_DATED_MESSAGE) { //why is config not correctly loaded?
            sendChatMessage(event.getPlayer());
        }
    }
}
