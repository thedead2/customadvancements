package de.thedead2.customadvancements.platform;

import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.Optional;

public class FabricPlatformHelper implements IPlatformHelper{

    @Nullable
    private MinecraftServer currentServer;

    @Override
    public String getPlatformName() {
        return "Fabric";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    @Override
    public Path getGameDirectory() {
        return FabricLoader.getInstance().getGameDir();
    }

    @Override
    public boolean isClient() {
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;
    }

    public void initServerTracker() {
        ServerLifecycleEvents.SERVER_STARTING.register(server -> currentServer = server);

        ServerLifecycleEvents.SERVER_STOPPED.register(server -> currentServer = null);
    }


    @Override
    public Optional<MinecraftServer> getServer() {
        return Optional.ofNullable(this.currentServer);
    }

    @Override
    public String getModVersion(String modId) {
        return FabricLoader.getInstance()
                .getModContainer(modId)
                .map(container -> container.getMetadata().getVersion().getFriendlyString())
                .orElse("UNKNOWN");
    }
}
