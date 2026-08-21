package de.thedead2.customadvancements.mixin;

import de.thedead2.customadvancements.client.ClientTranslationManager;
import net.minecraft.client.resources.language.ClientLanguage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientLanguage.class)
public class MixinClientLanguage {

    @Inject(method = "getOrDefault(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;", at = @At("HEAD"), cancellable = true)
    private void injectDynamicTranslation(String key, String defaultValue, CallbackInfoReturnable<String> cir) {
        if (ClientTranslationManager.contains(key)) {
            cir.setReturnValue(ClientTranslationManager.get(key));
        }
    }

    @Inject(method = "has(Ljava/lang/String;)Z", at = @At("HEAD"), cancellable = true)
    private void hasDynamicTranslation(String key, CallbackInfoReturnable<Boolean> cir) {
        if (ClientTranslationManager.contains(key)) {
            cir.setReturnValue(true);
        }
    }
}
