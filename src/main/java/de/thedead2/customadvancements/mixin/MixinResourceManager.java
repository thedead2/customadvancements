package de.thedead2.customadvancements.mixin;

import de.thedead2.customadvancements.util.ResourceManagerExtender;
import net.minecraft.resources.FallbackResourceManager;
import net.minecraft.resources.IResource;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.FileNotFoundException;

@Mixin(FallbackResourceManager.class)
public class MixinResourceManager {

    @Inject(at = @At(value = "HEAD"), method = "getResource(Lnet/minecraft/util/ResourceLocation;)Lnet/minecraft/resources/IResource;", cancellable = true)
    public void onGetResource(ResourceLocation resourceLocationIn, CallbackInfoReturnable<IResource> cir) throws FileNotFoundException {
        if(ResourceManagerExtender.getResource(resourceLocationIn) != null){
            cir.setReturnValue(ResourceManagerExtender.handleResourceRequest(resourceLocationIn).orElseThrow(() -> new FileNotFoundException(resourceLocationIn.toString())));
        }
    }
}
