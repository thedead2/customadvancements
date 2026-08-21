package de.thedead2.customadvancements.util.helper;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.CancellationException;



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
