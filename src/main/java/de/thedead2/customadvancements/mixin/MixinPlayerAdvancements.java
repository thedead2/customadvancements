package de.thedead2.customadvancements.mixin;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.PlayerAdvancements;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerAdvancements.class)
public class MixinPlayerAdvancements {

    @Inject(at = @At("HEAD"), method = "startProgress", cancellable = true)
    public void startProgress(Advancement advancementIn, AdvancementProgress progress, CallbackInfo ci){
        if (advancementIn == null){
            ci.cancel();
        }
    }
}
