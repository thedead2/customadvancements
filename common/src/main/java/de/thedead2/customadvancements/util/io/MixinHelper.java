package de.thedead2.customadvancements.util.io;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.CancellationException;

import javax.annotation.Nullable;


public class MixinHelper {

    public static <T> void checkNonNull(@Nullable T advancement, CallbackInfo callbackInfo) throws CancellationException {
        if (advancement == null) {
            callbackInfo.cancel();
        }
    }


    public static <R, T> void checkNonNullWithReturn(@Nullable R advancement, T returnVal, CallbackInfoReturnable<T> callbackInfo) throws CancellationException {
        if (advancement == null) {
            callbackInfo.setReturnValue(returnVal);
        }
    }
}
