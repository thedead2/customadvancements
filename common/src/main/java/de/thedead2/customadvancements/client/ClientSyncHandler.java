package de.thedead2.customadvancements.client;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.NativeImage;
import de.thedead2.customadvancements.client.gui.BackgroundType;
import de.thedead2.customadvancements.client.gui.IBackgroundRenderer;
import de.thedead2.customadvancements.network.SyncBackgroundDataPayload;
import de.thedead2.customadvancements.network.SyncLangDataPayload;
import de.thedead2.customadvancements.network.SyncTextureDataPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static de.thedead2.customadvancements.util.core.ModHelper.LOGGER;

public class ClientSyncHandler {

    public static final Map<ResourceLocation, IBackgroundRenderer> BACKGROUND_RENDERERS = new HashMap<>();
    private static final Set<ResourceLocation> TEXTURE_LOCATIONS = new HashSet<>();
    private static final Map<ResourceLocation, Map<Integer, byte[]>> TEXTURE_CHUNK_CACHE = new HashMap<>();


    public static void acceptAdvancementSync(SyncBackgroundDataPayload payload) {
        BACKGROUND_RENDERERS.clear();

        for (Map.Entry<ResourceLocation, JsonElement> entry : payload.backgroundData().entrySet()) {
            ResourceLocation id = entry.getKey();
            JsonElement jsonElement = entry.getValue();

            IBackgroundRenderer backgroundRenderer;

            if (jsonElement.isJsonPrimitive()) {
                backgroundRenderer = BackgroundType.TEXTURE.createRenderer(jsonElement);
            }
            else {
                JsonObject background = jsonElement.getAsJsonObject();

                backgroundRenderer = BackgroundType.fromJson(background.get("type"), id).createRenderer(background);
            }

            BACKGROUND_RENDERERS.put(id, backgroundRenderer);
        }

        LOGGER.info("Received server sync for {} background(s)", BACKGROUND_RENDERERS.size());
    }


    public static void acceptLangSync(SyncLangDataPayload payload) {
        LOGGER.info("Received language data from server...");
        ClientTranslationManager.processTranslationData(payload.langData());
    }


    public static void acceptTextureSync(SyncTextureDataPayload payload) {
        ResourceLocation id = payload.textureId();

        LOGGER.debug("Received texture chunk {}/{} for texture {}", payload.chunkIndex()+1, payload.totalChunks(), id);
        TEXTURE_CHUNK_CACHE.computeIfAbsent(id, k -> new HashMap<>())
                .put(payload.chunkIndex(), payload.chunkData());

        Map<Integer, byte[]> receivedChunks = TEXTURE_CHUNK_CACHE.get(id);

        if (receivedChunks.size() == payload.totalChunks()) {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();

            for (int i = 0; i < payload.totalChunks(); i++) {
                try {
                    buffer.write(receivedChunks.get(i));
                }
                catch (IOException e) {
                    LOGGER.error("Error recreating texture {} from chunks", id, e);
                    TEXTURE_CHUNK_CACHE.remove(id);
                    return;
                }
            }

            byte[] completeImageBytes = buffer.toByteArray();
            TEXTURE_CHUNK_CACHE.remove(id);
            injectTexture(id, completeImageBytes);
        }
    }

    private static void injectTexture(ResourceLocation location, byte[] bytes) {
        LOGGER.info("Received texture with id {} ({} bytes) from server. Tying to inject it...", location, bytes.length);

        Minecraft.getInstance().execute(() -> {
            try {
                NativeImage image = NativeImage.read(new ByteArrayInputStream(bytes));
                DynamicTexture dynamicTexture = new DynamicTexture(image);

                Minecraft.getInstance().getTextureManager().register(location, dynamicTexture);
                TEXTURE_LOCATIONS.add(location);
            }
            catch (IOException e) {
                LOGGER.error("Failed to inject texture data into texture manager: ", e);
            }
        });
    }
    

    public static void cleanUp() {
        LOGGER.info("Cleaning up client sync data...");
        BACKGROUND_RENDERERS.clear();
        ClientTranslationManager.clear();

        TEXTURE_LOCATIONS.forEach(location -> Minecraft.getInstance().getTextureManager().release(location));
        TEXTURE_LOCATIONS.clear();
        TEXTURE_CHUNK_CACHE.clear();
    }
}
