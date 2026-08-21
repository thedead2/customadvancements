package de.thedead2.customadvancements.util.io;

import de.thedead2.customadvancements.network.SyncTextureDataPayload;
import de.thedead2.mc_libs.concurrent.PartialCompletableFuture;
import de.thedead2.mc_libs.io.FileHandler;
import de.thedead2.mc_libs.util.ImageUtils;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static de.thedead2.customadvancements.util.core.ModHelper.*;


public class TextureHandler {
    public static final Path TEXTURES_PATH = DATA_PATH.resolve("textures");
    private static final String[] VALID_FILE_EXTENSIONS = {".png", ".jpeg", ".jpg", ".hdr", ".bmp", ".tga", ".psd", ".gif", ".pic", ".pnm"};

    private PartialCompletableFuture<Map<ResourceLocation, Pair<File, int[]>>> resourcesFuture = PartialCompletableFuture.completedFuture(new ConcurrentHashMap<>());

    public void loadTextureFiles() {

        this.resourcesFuture = PartialCompletableFuture.supplyAsync(new ConcurrentHashMap<>(), PartialCompletableFuture.Utils.mapClone(), (map, throwable) -> {
            LOGGER.error("Failed to load texture files!", throwable);
            logLoadStatus(map.size(), "texture file");

            return map;
        }, textures -> {
            LOGGER.info("Loading texture files...");

            FileHandler.readDirectoryAndSubDirectories(TEXTURES_PATH.toFile(), directory -> {
                LOGGER.debug("Starting to read texture files in: {}", directory.getPath());

                File[] texture_files = directory.listFiles(File::isFile);

                for (File texture : Objects.requireNonNull(texture_files)) {
                    String fileName = texture.getName();
                    String fileExtension = fileName.substring(texture.getName().lastIndexOf('.'));

                    if (fileExtension.matches("(?i)" + String.join("|", VALID_FILE_EXTENSIONS))) {
                        LOGGER.debug("Found file: {}", fileName);

                        ResourceLocation textureId = ResourceLocation.tryParse(MOD_ID + ":" + "textures" + "/" + fileName);
                        int[] dimensions;

                        try {
                            dimensions = ImageUtils.getImageDimensions(texture);
                        }
                        catch (IOException e) {
                            LOGGER.error("Failed to read image dimensions of file: {}", fileName, e);
                            dimensions = new int[]{};
                        }
                        textures.put(textureId, Pair.of(texture, dimensions));
                    }
                    else {
                        LOGGER.warn("File '{}' is not a valid texture file, ignoring it! --> supported file types: {}", fileName, Arrays.toString(VALID_FILE_EXTENSIONS));
                    }
                }
            });

            logLoadStatus(textures.size(), "texture file");

            return textures;
        }, FileHandler.IO_POOL);

        this.resourcesFuture.completeOnTimeout(45, TimeUnit.SECONDS);
    }

    public void sendTexturesToClient(Consumer<CustomPacketPayload> sender) {
        final int chunkSize = 32768; // 32 KB

        this.resourcesFuture.thenAccept(map -> {
            LOGGER.info("Sending texture data to client...");

            map.forEach((id, pair) -> {
                try {
                    byte[] bytes = Files.readAllBytes(pair.getLeft().toPath());

                    int totalChunks = (int) Math.ceil((double) bytes.length / chunkSize);

                    for (int i = 0; i < totalChunks; i++) {
                        int start = i * chunkSize;
                        int end = Math.min(bytes.length, start + chunkSize);

                        byte[] chunk = Arrays.copyOfRange(bytes, start, end);

                        LOGGER.debug("Sending texture chunk {}/{} for texture {}", i+1, totalChunks, id);
                        sender.accept(new SyncTextureDataPayload(id, i, totalChunks, chunk));
                    }
                }
                catch (IOException e) {
                    LOGGER.error("Failed to read file: {}", pair.getLeft().getPath());
                }
            });
        });
    }

    public Map<ResourceLocation, int[]> getImageDimensions() {
        Map<ResourceLocation, int[]> temp = new HashMap<>();
        this.resourcesFuture.join().forEach((resourceLocation, pair) -> temp.put(resourceLocation, pair.getRight()));

        return temp;
    }

    public void clear() {
        this.resourcesFuture = PartialCompletableFuture.completedFuture(new ConcurrentHashMap<>());
    }
}
