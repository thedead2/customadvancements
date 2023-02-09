package de.thedead2.customadvancements.mixin;

import de.thedead2.customadvancements.util.handler.CrashExtensionHandler;
import net.minecraft.CrashReport;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CrashReport.class)
public class MixinCrashReport {

    @Shadow @Final private Throwable exception;

    @Inject(at = @At("TAIL"), method = "<init>(Ljava/lang/String;Ljava/lang/Throwable;)V")
    public void onInit(String title, Throwable exception, CallbackInfo ci){
        CrashExtensionHandler.getInstance().setFault(exception);
    }

    @Inject(at = @At("HEAD"), method = "getFriendlyReport")
    public void onFriendlyReport(CallbackInfoReturnable<String> cir){
        CrashExtensionHandler.getInstance().setFault(exception);
    }
}
