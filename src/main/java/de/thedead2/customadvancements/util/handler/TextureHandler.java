package de.thedead2.customadvancements.util.handler;

import de.thedead2.customadvancements.util.ResourceManagerExtender;
import de.thedead2.customadvancements.util.core.CrashHandler;
import de.thedead2.customadvancements.util.core.FileHandler;
import net.minecraft.util.ResourceLocation;

import java.io.File;

import static de.thedead2.customadvancements.util.core.ModHelper.*;

public class TextureHandler extends FileHandler {

    private static TextureHandler instance;

    private TextureHandler(File directory){
        super(directory);
        instance = this;
    }

    @Override
    public void readFiles(File directory) {
        File[] texture_files = directory.listFiles();

        LOGGER.debug("Starting to read texture files in: " + directory.getPath());

        assert texture_files != null;
        for (File texture : texture_files) {
            CrashHandler.getInstance().setActiveFile(texture);
            if (texture.getName().endsWith(".png")) {
                LOGGER.debug("Found file: " + texture.getName());

                ResourceManagerExtender.addResource(ResourceLocation.tryCreate(MOD_ID + ":" + "textures" + "/" + texture.getName()), texture);
            }
            else {
                LOGGER.warn("File '" + texture.getName() + "' is not a '.png' file, ignoring it!");
            }
        }
        CrashHandler.getInstance().setActiveFile(null);
    }

    public static TextureHandler getInstance(){
        return instance != null ? instance : new TextureHandler(TEXTURES_PATH.toFile());
    }
}
