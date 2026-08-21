package de.thedead2.customadvancements;

import de.thedead2.customadvancements.client.ClientSyncHandler;
import de.thedead2.customadvancements.client.ClientTranslationManager;
import de.thedead2.customadvancements.events.ClientEventListeners;
import de.thedead2.customadvancements.network.SyncBackgroundDataPayload;
import de.thedead2.customadvancements.network.SyncLangDataPayload;
import de.thedead2.customadvancements.network.SyncTextureDataPayload;
import de.thedead2.customadvancements.util.core.ModHelper;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;

public class CustomAdvancementsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(SyncBackgroundDataPayload.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                ClientSyncHandler.acceptAdvancementSync(payload);
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(SyncTextureDataPayload.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                ClientSyncHandler.acceptTextureSync(payload);
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(SyncLangDataPayload.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                ClientSyncHandler.acceptLangSync(payload);
            });
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> ClientSyncHandler.cleanUp());

        ScreenEvents.BEFORE_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            ClientEventListeners.beforeScreenInit(screen);
        });

        ScreenEvents.AFTER_INIT.register(((client, screen, scaledWidth, scaledHeight) -> {
            ClientEventListeners.afterScreenInit(screen, screen.children(), button -> screen.children().remove(button));
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
