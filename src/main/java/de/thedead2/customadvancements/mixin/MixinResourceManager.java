package de.thedead2.customadvancements.mixin;

import de.thedead2.customadvancements.util.ResourceManagerExtender;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.FallbackResourceManager;
import net.minecraft.server.packs.resources.Resource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;


@Mixin(FallbackResourceManager.class)
public class MixinResourceManager {

    @Inject(at = @At(value = "RETURN", ordinal = 2), method = "getResource(Lnet/minecraft/resources/ResourceLocation;)Ljava/util/Optional;", cancellable = true)
    public void onGetResource(ResourceLocation resourceLocation, CallbackInfoReturnable<Optional<Resource>> cir) {
        cir.setReturnValue(ResourceManagerExtender.handleResourceRequest(resourceLocation));
    }
}
