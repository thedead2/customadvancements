package de.thedead2.customadvancements.util.io;

import de.thedead2.customadvancements.util.ResourceManagerExtender;
import net.minecraft.resources.ResourceLocation;

import java.io.File;
import java.util.Arrays;
import java.util.Objects;

import static de.thedead2.customadvancements.util.core.ModHelper.*;


public class TextureHandler {

    private static final String[] valid_file_extensions = {".png", ".jpeg", ".jpg", ".hdr", ".bmp", ".tga", ".psd", ".gif", ".pic", ".pnm"};


    public static void loadTextureFiles() {
        FileHandler.readDirectoryAndSubDirectories(TEXTURES_PATH.toFile(), directory -> {
            LOGGER.debug("Starting to read texture files in: {}", directory.getPath());

            File[] texture_files = directory.listFiles(File::isFile);

            for (File texture : Objects.requireNonNull(texture_files)) {
                String fileName = texture.getName();
                String fileExtension = fileName.substring(texture.getName().lastIndexOf('.'));

                if (fileExtension.matches("(?i)" + String.join("|", valid_file_extensions))) {
                    LOGGER.debug("Found file: {}", fileName);

                    ResourceManagerExtender.addResource(ResourceLocation.tryParse(MOD_ID + ":" + "textures" + "/" + fileName), texture);
                }
                else {
                    LOGGER.warn("File '{}' is not a valid texture file, ignoring it! --> supported file types: {}", fileName, Arrays.toString(valid_file_extensions));
                    WARNINGS.offer("File '" + fileName + "' is not a valid texture file and couldn't be loaded!\nSupported file types are: " + Arrays.toString(valid_file_extensions));
                }
            }
        });
    }
}
