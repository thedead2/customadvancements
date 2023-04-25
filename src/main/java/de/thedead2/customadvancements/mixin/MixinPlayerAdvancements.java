package de.thedead2.customadvancements.mixin;

import de.thedead2.customadvancements.util.core.ConfigManager;
import net.minecraft.advancements.Advancement;
import net.minecraft.server.PlayerAdvancements;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static de.thedead2.customadvancements.util.handler.AdvancementHandler.grantingAllAdvancements;

@Mixin(PlayerAdvancements.class)
public abstract class MixinPlayerAdvancements {

    @Inject(at = @At("HEAD"), method = "award(Lnet/minecraft/advancements/Advancement;Ljava/lang/String;)Z", cancellable = true)
    public void onAwardingAdvancement(Advancement pAdvancement, String pCriterionKey, CallbackInfoReturnable<Boolean> cir){
        if(ConfigManager.ADVANCEMENT_PROGRESSION.get() && !grantingAllAdvancements){
            ConfigManager.ADVANCEMENT_PROGRESSION_MODE.get().handleAdvancementAchieving(pAdvancement, (PlayerAdvancements) (Object) this).ifPresent(cir::setReturnValue);
        }
    }
}
