package de.thedead2.customadvancements.mixin;

import de.thedead2.customadvancements.util.core.ConfigManager;
import de.thedead2.customadvancements.util.io.MixinHelper;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.server.PlayerAdvancements;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static de.thedead2.customadvancements.util.io.AdvancementHandler.grantingAllAdvancements;


@Mixin(PlayerAdvancements.class)
public abstract class MixinPlayerAdvancements {

    @Inject(at = @At("HEAD"), method = "award(Lnet/minecraft/advancements/Advancement;Ljava/lang/String;)Z", cancellable = true)
    public void onAwardingAdvancement(Advancement advancement, String pCriterionKey, CallbackInfoReturnable<Boolean> cir) {
        MixinHelper.checkNonNull(advancement, false, cir);

        if (ConfigManager.ADVANCEMENT_PROGRESSION.get() && !grantingAllAdvancements) {
            ConfigManager.ADVANCEMENT_PROGRESSION_MODE.get().handleAdvancementAchieving(advancement, (PlayerAdvancements) (Object) this).ifPresent(cir::setReturnValue);
        }
    }


    @Inject(at = @At("HEAD"), method = "startProgress(Lnet/minecraft/advancements/Advancement;Lnet/minecraft/advancements/AdvancementProgress;)V", cancellable = true)
    public void onStartingProgress(Advancement advancement, AdvancementProgress progress, CallbackInfo ci) {
        MixinHelper.checkNonNull(advancement, ci);
    }


    @Inject(at = @At("HEAD"), method = "revoke(Lnet/minecraft/advancements/Advancement;Ljava/lang/String;)Z", cancellable = true)
    public void onRevokingAdvancement(Advancement pAdvancement, String pCriterionKey, CallbackInfoReturnable<Boolean> cir) {
        MixinHelper.checkNonNull(pAdvancement, false, cir);
    }


    @Inject(at = @At("HEAD"), method = "registerListeners(Lnet/minecraft/advancements/Advancement;)V", cancellable = true)
    public void onRegisteringListeners(Advancement pAdvancement, CallbackInfo ci) {
        MixinHelper.checkNonNull(pAdvancement, ci);
    }


    @Inject(at = @At("HEAD"), method = "unregisterListeners(Lnet/minecraft/advancements/Advancement;)V", cancellable = true)
    public void onUnregisteringListeners(Advancement pAdvancement, CallbackInfo ci) {
        MixinHelper.checkNonNull(pAdvancement, ci);
    }
}
