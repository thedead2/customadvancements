package de.thedead2.customadvancements.client;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import de.thedead2.customadvancements.client.gui.*;
import de.thedead2.customadvancements.network.SyncBackgroundDataPayload;
import de.thedead2.mc_libs.network.NetworkUtils;
import de.thedead2.mc_libs.network.SyncChunkedDataPayload;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementNode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static de.thedead2.customadvancements.util.ModHelper.LOGGER;


public class ClientRenderer {

    private static final Map<ResourceLocation, IBackgroundRenderer> BACKGROUND_RENDERERS = new HashMap<>();
    private static final Set<ResourceLocation> TEXTURE_LOCATIONS = new HashSet<>();


    public static void drawAdvancementTabBg(AdvancementNode rootNode, GuiGraphics guiGraphics, CallbackInfo ci, int xMin, int xMax, int yMin, int yMax, double scrollX, double scrollY, RootRenderer rootRenderer) {
        AdvancementHolder advancementHolder = rootNode.holder();
        Advancement advancement = rootNode.advancement();

        ResourceLocation advancementId = advancementHolder.id();
        ResourceLocation root = ResourceLocation.tryBuild(advancementId.getNamespace(), advancementId.getPath() + ".json");

        if (advancement.display().isEmpty()) {
            return;
        }

        if (BACKGROUND_RENDERERS.containsKey(root)) {
            IBackgroundRenderer backgroundRenderer = BACKGROUND_RENDERERS.get(root);

            if (backgroundRenderer != null) {
                ci.cancel();

                int roundedScrollX = Mth.floor(scrollX);
                int roundedScrollY = Mth.floor(scrollY);

                guiGraphics.enableScissor(xMin, yMin, xMax, yMax);
                RenderSystem.enableBlend();

                backgroundRenderer.drawBg(guiGraphics, xMin, xMax, yMin, yMax, roundedScrollX, roundedScrollY);

                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate((float) xMin, (float) yMin, 0.0F);

                rootRenderer.draw(guiGraphics, roundedScrollX, roundedScrollY);

                guiGraphics.pose().popPose();
                RenderSystem.disableBlend();
                guiGraphics.disableScissor();
            }
        }
    }


    public static void acceptBackgroundSync(SyncBackgroundDataPayload payload) {
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


    public static void acceptTextureSync(SyncChunkedDataPayload payload) {
        NetworkUtils.acceptChunkedSync(SyncChunkedDataPayload.DataType.TEXTURE, payload, LOGGER, (id, data) -> {
            LOGGER.info("Received texture with id {} ({} bytes) from server. Tying to inject it...", id, data.length);

            Minecraft.getInstance().execute(() -> {
                try {
                    NativeImage image = NativeImage.read(new ByteArrayInputStream(data));
                    DynamicTexture dynamicTexture = new DynamicTexture(image);

                    Minecraft.getInstance().getTextureManager().register(id, dynamicTexture);
                    TEXTURE_LOCATIONS.add(id);
                }
                catch (IOException e) {
                    LOGGER.error("Failed to inject texture data into texture manager: ", e);
                }
            });
        });
    }

    public static void clear() {
        BACKGROUND_RENDERERS.clear();
        TEXTURE_LOCATIONS.forEach(id -> Minecraft.getInstance().getTextureManager().release(id));
        TEXTURE_LOCATIONS.clear();
    }
}
