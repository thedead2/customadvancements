package de.thedead2.customadvancements.util.handler;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.loading.FMLEnvironment;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public class TextureHandler extends FileHandler {

    public TextureHandler(){
        if (!FMLEnvironment.dist.isDedicatedServer() || !ConfigManager.OPTIFINE_SHADER_COMPATIBILITY.get()){
            this.init(new File(TEXTURES_PATH));
        }
        else if (ConfigManager.OPTIFINE_SHADER_COMPATIBILITY.get()){
            LOGGER.warn("Enabling compatibility mode for Optifine Shaders! This disables custom background textures for advancements!");
        }
    }

    @Override
    public void readFiles(File directory) {
        File[] texture_files = directory.listFiles();

        LOGGER.debug("Starting to read texture files in: " + directory.getPath());

        assert texture_files != null;
        for (File texture : texture_files) {
            if (texture.getName().endsWith(".png")) {
                LOGGER.debug("Found file: " + texture.getName());

                createNativeImage(texture);
            }
            else {
                LOGGER.warn("File '" + texture.getName() + "' is not a '.png' file, ignoring it!");
            }
        }
    }


    private void createNativeImage(File texture){
        try {
            InputStream inputStream = Files.newInputStream(texture.toPath());
            NativeImage image = NativeImage.read(inputStream);
            ResourceLocation textureLocation = ResourceLocation.tryParse(MOD_ID + ":" + "textures" + "/" + texture.getName());

            LOGGER.debug("Texture Location for " + texture.getName() + ": " + textureLocation);

            TEXTURES.put(textureLocation, image);
            inputStream.close();
        }
        catch (IOException e) {
            LOGGER.error("Failed to read texture file: " + e);
            e.printStackTrace();
        }
    }
}
