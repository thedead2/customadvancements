package de.thedead2.customadvancements;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static de.thedead2.customadvancements.util.ModHelper.*;

@Mod(MOD_ID)
public class CustomAdvancements {
    public static final Logger LOGGER = LogManager.getLogger();

    public CustomAdvancements() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(final FMLCommonSetupEvent event) {
        LOGGER.info("Starting " + MOD_NAME + ", Version: " + MOD_VERSION);
        LOGGER.debug("Path of the mod file: " + THIS_MOD_FILE.getFilePath());

        FILE_HANDLER.getDirectory();
        FILE_HANDLER.readFiles();
    }
}
