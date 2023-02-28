package de.thedead2.customadvancements.mixin;

import de.thedead2.customadvancements.advancements.CustomAdvancementManager;
import net.minecraft.client.Minecraft;
import net.minecraft.server.WorldStem;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Function;

@Mixin(Minecraft.class)
public class MixinMinecraft {

    @Inject(at = @At("HEAD"), method = "doLoadLevel(Ljava/lang/String;Ljava/util/function/Function;Ljava/util/function/Function;ZLnet/minecraft/client/Minecraft$ExperimentalDialogType;Z)V", remap = false)
    public void onLevelLoad(String pLevelName, Function<LevelStorageSource.LevelStorageAccess, WorldStem.DataPackConfigSupplier> pLevelSaveToDatapackFunction, Function<LevelStorageSource.LevelStorageAccess, WorldStem.WorldDataSupplier> p_205208_, boolean pVanillaOnly, Minecraft.ExperimentalDialogType pSelectionType, boolean creating, CallbackInfo ci){
        if(pVanillaOnly){
            CustomAdvancementManager.enableSafeMode();
        }
        else {
            CustomAdvancementManager.disableSafeMode();
        }
    }
}
