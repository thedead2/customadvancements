package de.thedead2.customadvancements.util.handler;

import net.minecraft.advancements.Advancement;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.CancellationException;

import javax.annotation.Nullable;


public class MixinHandler {

    public static void ensureExistent(@Nullable Advancement advancement, CallbackInfo callbackInfo) throws CancellationException {
        if(advancement == null) {
            callbackInfo.cancel();
        }
    }
    public static <T> void ensureExistent(@Nullable Advancement advancement, T returnVal, CallbackInfoReturnable<T> callbackInfo) throws CancellationException {
        if(advancement == null) {
            callbackInfo.setReturnValue(returnVal);
        }
    }
}
