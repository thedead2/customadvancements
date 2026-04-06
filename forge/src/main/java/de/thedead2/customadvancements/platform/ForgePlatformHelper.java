package de.thedead2.customadvancements.platform;

import net.minecraft.server.MinecraftServer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.nio.file.Path;
import java.util.Optional;

public class ForgePlatformHelper implements IPlatformHelper {

    @Override
    public String getPlatformName() {
        return "Forge";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return !FMLLoader.isProduction();
    }

    @Override
    public Path getGameDirectory() {
        return FMLPaths.GAMEDIR.get();
    }

    @Override
    public boolean executeOnClient(Runnable action) {
        try {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> action);
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }

    @Override
    public Optional<MinecraftServer> getServer() {
        return Optional.ofNullable(ServerLifecycleHooks.getCurrentServer());
    }


}
