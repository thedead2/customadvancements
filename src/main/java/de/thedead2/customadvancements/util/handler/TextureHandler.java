package de.thedead2.customadvancements.util.handler;

import de.thedead2.customadvancements.util.ResourceManagerExtender;
import de.thedead2.customadvancements.util.core.CrashHandler;
import de.thedead2.customadvancements.util.core.FileHandler;
import net.minecraft.resources.ResourceLocation;

import java.io.File;
import java.util.Arrays;

import static de.thedead2.customadvancements.util.core.ModHelper.*;

public abstract class TextureHandler {
    private static final String[] valid_file_extensions = {".png", ".jpeg", ".jpg", ".hdr", ".bmp", ".tga", ".psd", ".gif", ".pic", ".pnm"};

    public static void start() {
        FileHandler.readDirectory(TEXTURES_PATH.toFile(), directory -> {
            File[] texture_files = directory.listFiles();

            LOGGER.debug("Starting to read texture files in: " + directory.getPath());

            assert texture_files != null;
            for (File texture : texture_files) {
                CrashHandler.getInstance().setActiveFile(texture);

                final String fileName = texture.getName();
                final String fileExtension = fileName.substring(texture.getName().lastIndexOf('.'));

                if (fileExtension.matches("(?i)" + String.join("|", valid_file_extensions))) {
                    LOGGER.debug("Found file: " + fileName);

                    ResourceManagerExtender.addResource(ResourceLocation.tryParse(MOD_ID + ":" + "textures" + "/" + fileName), texture);
                }
                else {
                    LOGGER.warn("File '" + fileName + "' is not a valid texture file, ignoring it! --> supported file types: {}", Arrays.toString(valid_file_extensions));
                }
            }
            CrashHandler.getInstance().setActiveFile(null);
        });
    }
}
