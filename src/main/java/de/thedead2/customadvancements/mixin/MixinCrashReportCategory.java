package de.thedead2.customadvancements.mixin;

import de.thedead2.customadvancements.util.handler.CrashExtensionHandler;
import net.minecraft.CrashReportCategory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CrashReportCategory.class)
public class MixinCrashReportCategory {

    @Shadow private StackTraceElement[] stackTrace;


    @Inject(at = @At("HEAD"), method = "getDetails")
    public void onDetailAdd(StringBuilder pBuilder, CallbackInfo ci){
        CrashExtensionHandler.getInstance().setFault(stackTrace);
    }
}
