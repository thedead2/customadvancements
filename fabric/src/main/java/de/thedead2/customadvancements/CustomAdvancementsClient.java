package de.thedead2.customadvancements;

import de.thedead2.customadvancements.client.ClientRenderer;
import de.thedead2.customadvancements.client.ClientTranslationManager;
import de.thedead2.customadvancements.events.ClientEventListeners;
import de.thedead2.customadvancements.events.CommonEventListeners;
import de.thedead2.customadvancements.network.SyncBackgroundDataPayload;
import de.thedead2.customadvancements.network.SyncLangDataPayload;
import de.thedead2.mc_libs.network.SyncChunkedDataPayload;
import de.thedead2.customadvancements.util.ModHelper;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;

public class CustomAdvancementsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(SyncBackgroundDataPayload.TYPE, (payload, context) -> context.client().execute(() -> ClientRenderer.acceptBackgroundSync(payload)));
        ClientPlayNetworking.registerGlobalReceiver(SyncChunkedDataPayload.TYPE, (payload, context) -> context.client().execute(() -> ClientRenderer.acceptTextureSync(payload)));
        ClientPlayNetworking.registerGlobalReceiver(SyncLangDataPayload.TYPE, (payload, context) -> context.client().execute(() -> ClientTranslationManager.acceptLangSync(payload)));

        ClientLifecycleEvents.CLIENT_STARTED.register(client -> CommonEventListeners.onCommonSetup());
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> ClientEventListeners.onClientLogout());

        ScreenEvents.AFTER_INIT.register(((client, screen, scaledWidth, scaledHeight) -> {
            ClientEventListeners.preventAdvancementScreenOpeningIfNeeded(screen);
            ClientEventListeners.removeAdvancementsButtonIfNeeded(screen, Screens.getButtons(screen), button -> Screens.getButtons(screen).remove(button));
        }));

        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(
                new SimpleSynchronousResourceReloadListener() {
                    @Override
                    public ResourceLocation getFabricId() {
                        return ResourceLocation.fromNamespaceAndPath(ModHelper.MOD_ID, "language_reload");
                    }

                    @Override
                    public void onResourceManagerReload(ResourceManager resourceManager) {
                        ClientTranslationManager.reloadLang();
                    }
                }
        );
    }
}
