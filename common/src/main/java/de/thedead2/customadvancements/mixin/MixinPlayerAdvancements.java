package de.thedead2.customadvancements.mixin;

import de.thedead2.customadvancements.util.core.ConfigManager;
import de.thedead2.customadvancements.util.helper.MixinHelper;
import net.minecraft.advancements.AdvancementHolder;
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

    @Inject(at = @At("HEAD"), method = "award(Lnet/minecraft/advancements/AdvancementHolder;Ljava/lang/String;)Z", cancellable = true)
    public void onAwardingAdvancement(AdvancementHolder advancement, String pCriterionKey, CallbackInfoReturnable<Boolean> cir) {
        MixinHelper.checkNonNullWithReturn(advancement, false, cir);

        if (ConfigManager.ADVANCEMENT_PROGRESSION.get() && !grantingAllAdvancements) {
            ConfigManager.ADVANCEMENT_PROGRESSION_MODE.get().handleAdvancementAchieving(advancement, (PlayerAdvancements) (Object) this).ifPresent(cir::setReturnValue);
        }
    }


    @Inject(at = @At("HEAD"), method = "startProgress(Lnet/minecraft/advancements/AdvancementHolder;Lnet/minecraft/advancements/AdvancementProgress;)V", cancellable = true)
    public void onStartingProgress(AdvancementHolder advancement, AdvancementProgress progress, CallbackInfo ci) {
        MixinHelper.checkNonNull(advancement, ci);
    }


    @Inject(at = @At("HEAD"), method = "revoke(Lnet/minecraft/advancements/AdvancementHolder;Ljava/lang/String;)Z", cancellable = true)
    public void onRevokingAdvancement(AdvancementHolder pAdvancement, String pCriterionKey, CallbackInfoReturnable<Boolean> cir) {
        MixinHelper.checkNonNullWithReturn(pAdvancement, false, cir);
    }


    @Inject(at = @At("HEAD"), method = "registerListeners(Lnet/minecraft/advancements/AdvancementHolder;)V", cancellable = true)
    public void onRegisteringListeners(AdvancementHolder pAdvancement, CallbackInfo ci) {
        MixinHelper.checkNonNull(pAdvancement, ci);
    }


    @Inject(at = @At("HEAD"), method = "unregisterListeners(Lnet/minecraft/advancements/AdvancementHolder;)V", cancellable = true)
    public void onUnregisteringListeners(AdvancementHolder pAdvancement, CallbackInfo ci) {
        MixinHelper.checkNonNull(pAdvancement, ci);
    }
}
