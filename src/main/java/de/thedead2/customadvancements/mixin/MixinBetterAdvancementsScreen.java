package de.thedead2.customadvancements.mixin;

import betteradvancements.gui.BetterAdvancementsScreen;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Pseudo
@Mixin(BetterAdvancementsScreen.class)
public class MixinBetterAdvancementsScreen {


    @Inject(at = @At("HEAD"), method = "onUpdateAdvancementProgress(Lnet/minecraft/advancements/Advancement;Lnet/minecraft/advancements/AdvancementProgress;)V", cancellable = true)
    public void onUpdateAdvancementProgress(Advancement advancement, AdvancementProgress advancementProgress, CallbackInfo ci){
        if(advancement == null){
            ci.cancel();
        }
    }
}
