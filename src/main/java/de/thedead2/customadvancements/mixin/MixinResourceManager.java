package de.thedead2.customadvancements.mixin;

import de.thedead2.customadvancements.util.ResourceManagerExtender;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.FallbackResourceManager;
import net.minecraft.server.packs.resources.Resource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.FileNotFoundException;
import java.util.Optional;

@Mixin(FallbackResourceManager.class)
public class MixinResourceManager {

    @Inject(at = @At(value = "HEAD"), method = "getResource(Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/server/packs/resources/Resource;", cancellable = true)
    public void onGetResource(ResourceLocation pResourceLocation, CallbackInfoReturnable<Resource> cir) throws FileNotFoundException {
        if(ResourceManagerExtender.getResource(pResourceLocation) != null){
            cir.setReturnValue(ResourceManagerExtender.handleResourceRequest(pResourceLocation).orElseThrow(() -> new FileNotFoundException(pResourceLocation.toString())));
        }
    }
}
