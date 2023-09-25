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


    @Inject(at = @At("HEAD"), method = "onUpdateAdvancementProgress", cancellable = true, remap = false)
    public void onUpdateAdvancementProgress(Advancement advancementIn, AdvancementProgress progress, CallbackInfo ci){
        if (advancementIn == null){
            ci.cancel();
        }
    }
}
