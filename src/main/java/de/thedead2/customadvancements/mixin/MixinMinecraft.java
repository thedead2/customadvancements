package de.thedead2.customadvancements.mixin;

import com.mojang.datafixers.util.Function4;
import de.thedead2.customadvancements.advancements.CustomAdvancementManager;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.datafix.codec.DatapackCodec;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.world.storage.IServerConfiguration;
import net.minecraft.world.storage.SaveFormat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Function;


@Mixin(Minecraft.class)
public class MixinMinecraft {

    @Inject(at = @At("HEAD"), method = "loadWorld(Ljava/lang/String;Lnet/minecraft/util/registry/DynamicRegistries$Impl;Ljava/util/function/Function;Lcom/mojang/datafixers/util/Function4;ZLnet/minecraft/client/Minecraft$WorldSelectionType;Z)V", remap = false)
    public void onLevelLoad(String worldName, DynamicRegistries.Impl dynamicRegistries, Function<SaveFormat.LevelSave, DatapackCodec> levelSaveToDatapackFunction, Function4<SaveFormat.LevelSave, DynamicRegistries.Impl, IResourceManager, DatapackCodec, IServerConfiguration> quadFunction, boolean vanillaOnly, Minecraft.WorldSelectionType selectionType, boolean creating, CallbackInfo ci){
        if(vanillaOnly){
            CustomAdvancementManager.enableSafeMode();
        }
        else {
            CustomAdvancementManager.disableSafeMode();
        }
    }
}
