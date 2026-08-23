package de.thedead2.customadvancements;

import de.thedead2.customadvancements.client.ClientSyncHandler;
import de.thedead2.customadvancements.client.ClientTranslationManager;
import de.thedead2.customadvancements.commands.CommandManager;
import de.thedead2.customadvancements.events.CommonEventListeners;
import de.thedead2.customadvancements.network.SyncBackgroundDataPayload;
import de.thedead2.customadvancements.network.SyncLangDataPayload;
import de.thedead2.customadvancements.network.SyncTextureDataPayload;
import de.thedead2.customadvancements.util.core.ConfigManager;
import de.thedead2.customadvancements.util.logging.FilterRegistration;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.jetbrains.annotations.NotNull;

import static de.thedead2.customadvancements.util.core.ModHelper.*;


@Mod(MOD_ID)
public class CustomAdvancements {

    public CustomAdvancements(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::setup);
        modEventBus.addListener(this::registerPayloads);
        modEventBus.addListener(this::onRegisterClientReloadListeners);

        NeoForge.EVENT_BUS.addListener(this::onPlayerJoin);
        NeoForge.EVENT_BUS.addListener(this::onCommandsRegister);
        NeoForge.EVENT_BUS.addListener(this::onPlayerDeath);
        NeoForge.EVENT_BUS.addListener(this::onServerStopping);
        NeoForge.EVENT_BUS.addListener(this::onAddReloadListeners);

        modContainer.registerConfig(ModConfig.Type.COMMON, ConfigManager.CONFIG_SPEC);

        FilterRegistration.registerLoggerFilters();
    }


    // ################################################################## Common Event Listeners ##################################################################

    private void setup(final FMLCommonSetupEvent event) {
        CommonEventListeners.onCommonSetup();
    }

    private void registerPayloads(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(MOD_ID);

        registrar.playToClient(
                SyncBackgroundDataPayload.TYPE,
                SyncBackgroundDataPayload.CODEC,
                (payload, context) -> context.enqueueWork(() -> ClientSyncHandler.acceptAdvancementSync(payload))
        );

        registrar.playToClient(
                SyncTextureDataPayload.TYPE,
                SyncTextureDataPayload.STREAM_CODEC,
                ((payload, context) -> context.enqueueWork(() -> ClientSyncHandler.acceptTextureSync(payload)))
        );

        registrar.playToClient(
                SyncLangDataPayload.TYPE,
                SyncLangDataPayload.STREAM_CODEC,
                ((payload, context) -> context.enqueueWork(() -> ClientSyncHandler.acceptLangSync(payload)))
        );
    }

    private void onServerStopping(final ServerStoppingEvent ignored) {
        CommonEventListeners.onServerStop();
    }

    private void onPlayerJoin(final PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            CAMain.getInstance().sendDataToClient(packet -> serverPlayer.connection.send(packet));
        }
    }

    private void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new SimplePreparableReloadListener<Void>() {
            @Override
            protected @NotNull Void prepare(@NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profiler) {
                return null;
            }

            @Override
            protected void apply(@NotNull Void object, @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profiler) {
                getServer().ifPresent(server -> server.getPlayerList().getPlayers().forEach(serverPlayer ->
                        CAMain.getInstance().sendDataToClient(packet -> serverPlayer.connection.send(packet))));
            }
        });
    }

    private void onCommandsRegister(final RegisterCommandsEvent event) {
        CommandManager.registerCommandsToDispatcher(event.getDispatcher());

        // We need to load the data this early as advancement loading happens right after the command registration
        CommonEventListeners.onServerStart();
    }

    private void onPlayerDeath(final PlayerEvent.PlayerRespawnEvent event) {
        CommonEventListeners.onPlayerDeath((ServerPlayer) event.getEntity());
    }

    private void onRegisterClientReloadListeners(final RegisterClientReloadListenersEvent event) {
        event.registerReloadListener((ResourceManagerReloadListener) resourceManager -> ClientTranslationManager.reloadLang());
    }
}
