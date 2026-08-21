package de.thedead2.customadvancements;

import de.thedead2.customadvancements.advancements.CustomAdvancementManager;
import de.thedead2.customadvancements.util.io.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.function.Consumer;

import static de.thedead2.customadvancements.util.core.ModHelper.*;

public class CAMain {
    private static final CAMain INSTANCE = new CAMain();

    private final CustomAdvancementManager customAdvancementManager;
    private final TextureHandler textureHandler;
    private final LanguageHandler languageHandler;

    public CAMain() {
        this.customAdvancementManager = new CustomAdvancementManager();
        this.textureHandler = new TextureHandler();
        this.languageHandler = new LanguageHandler();
    }


    public void loadData() {
        LOGGER.info("Starting to load advancement data from disk...");

        /*if (BA_COMPATIBILITY.get()) {
            LOGGER.info("Found BetterAdvancements to be present! Enabling compatibility mode...");
        }*/

        textureHandler.loadTextureFiles();
        languageHandler.loadLangFiles();
        customAdvancementManager.loadAdvancementFiles();
    }

    public void sendDataToClient(Consumer<CustomPacketPayload> sender) {
        textureHandler.sendTexturesToClient(sender);
        languageHandler.sendLangDataToClient(sender);
        customAdvancementManager.sendBackgroundDataToClient(sender, textureHandler);
    }

    public CustomAdvancementManager getCustomAdvancementManager() {
        return customAdvancementManager;
    }

    public static CAMain getInstance() {
        return INSTANCE;
    }

    public void clearLoadingStates() {
        textureHandler.clear();
        languageHandler.clear();
        customAdvancementManager.clear();
    }

}
