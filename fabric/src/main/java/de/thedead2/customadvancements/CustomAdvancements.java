package de.thedead2.customadvancements;

import de.thedead2.customadvancements.commands.CommandManager;
import de.thedead2.customadvancements.events.CommonEventListeners;
import de.thedead2.customadvancements.network.SyncBackgroundDataPayload;
import de.thedead2.customadvancements.network.SyncLangDataPayload;
import de.thedead2.customadvancements.network.SyncTextureDataPayload;
import de.thedead2.customadvancements.platform.FabricPlatformHelper;
import de.thedead2.customadvancements.util.core.ConfigManager;
import de.thedead2.customadvancements.util.logging.FilterRegistration;
import fuzs.forgeconfigapiport.fabric.api.neoforge.v4.NeoForgeConfigRegistry;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.fml.config.ModConfig;

import static de.thedead2.customadvancements.util.core.ModHelper.*;

public class CustomAdvancements implements ModInitializer {
    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess, environment) -> {
            CommonEventListeners.onCommonSetup();
            CommandManager.registerCommandsToDispatcher(dispatcher);
            CommonEventListeners.onServerStart();
        }));
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> CommonEventListeners.onPlayerDeath(newPlayer));
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> CommonEventListeners.onGameShutdown());

        if(PLATFORM.isClient())
            ClientLifecycleEvents.CLIENT_STARTED.register(client -> CommonEventListeners.onCommonSetup());

        PayloadTypeRegistry.playS2C().register(SyncBackgroundDataPayload.TYPE, SyncBackgroundDataPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(SyncTextureDataPayload.TYPE, SyncTextureDataPayload.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(SyncLangDataPayload.TYPE, SyncLangDataPayload.STREAM_CODEC);

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            CAMain.getInstance().sendDataToClient(sender::sendPacket);
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> CommonEventListeners.onServerStop());

        ((FabricPlatformHelper) PLATFORM).initServerTracker();

        NeoForgeConfigRegistry.INSTANCE.register(MOD_ID, ModConfig.Type.COMMON, ConfigManager.CONFIG_SPEC);

        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(
                new SimpleSynchronousResourceReloadListener() {
                    @Override
                    public void onResourceManagerReload(ResourceManager resourceManager) {
                        getServer().ifPresent(server -> server.getPlayerList().getPlayers().forEach(serverPlayer ->
                                CAMain.getInstance().sendDataToClient(packet -> ServerPlayNetworking.send(serverPlayer, packet))));
                    }

                    @Override
                    public ResourceLocation getFabricId() {
                        return ResourceLocation.tryBuild(MOD_ID, "server_reload_listener");
                    }
                }
        );

        FilterRegistration.registerLoggerFilters();
    }
}
