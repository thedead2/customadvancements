package de.thedead2.customadvancements.util;

import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import static de.thedead2.customadvancements.util.ModHelper.TEXTURES;

public class TextureHandler implements IFileHandler{

    public static final Logger LOGGER = LogManager.getLogger();


    public void readFiles(File directory) {
        if (FMLEnvironment.dist.isDedicatedServer()){
            LOGGER.debug("Detected dedicated server running! Disabling texture loading...");
            return;
        }

        if (ModHelper.ConfigManager.OPTIFINE_SHADER_COMPATIBILITY.get()){
            LOGGER.warn("Enabling compatibility mode for Optifine Shaders! This disables custom background textures for advancements!");
            return;
        }

        File[] texture_files = directory.listFiles();

        LOGGER.debug("Starting to read texture files...");

        assert texture_files != null;
        for (File texture : texture_files) {
            if (texture.getName().endsWith(".png")) {
                LOGGER.debug("Found file: " + texture.getName());

                try {
                    InputStream inputStream = Files.newInputStream(texture.toPath());
                    NativeImage image = NativeImage.read(inputStream);
                    ResourceLocation textureLocation = ResourceLocation.tryCreate(IFileHandler.getId(texture.getPath()));

                    LOGGER.debug("Texture Location for " + texture.getName() + ": " + textureLocation);

                    TEXTURES.put(textureLocation, image);
                    inputStream.close();

                    FileHandler.textures_counter++;
                }
                catch (IOException e) {
                    LOGGER.error("Failed to read texture files: " + e);
                    e.printStackTrace();
                }
            }
            else {
                LOGGER.warn("File '" + texture.getName() + "' is not a '.png' file, ignoring it!");
            }
        }
    }
}
