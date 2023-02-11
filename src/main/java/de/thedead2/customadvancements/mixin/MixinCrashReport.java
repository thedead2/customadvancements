package de.thedead2.customadvancements.mixin;

import de.thedead2.customadvancements.util.handler.CrashHandler;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Mixin(CrashReport.class)
public class MixinCrashReport {

    @Shadow @Final private Throwable exception;
    @Shadow @Final private List<CrashReportCategory> details;

    @Inject(at = @At("HEAD"), method = "getFriendlyReport")
    public void onFriendlyReport(CallbackInfoReturnable<String> cir){
        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.resolveCrash(exception);
        details.forEach(crashReportCategory -> {
            AtomicReference<String> errorMessage = new AtomicReference<>();
            crashReportCategory.entries.forEach(crashReportCategory$Entry -> {
                String key = crashReportCategory$Entry.getKey();
                if(key.contains("Exception")){
                    errorMessage.set(crashReportCategory$Entry.getValue());
                }
            });
            crashHandler.resolveCrash(crashReportCategory.getStacktrace(), errorMessage.get());
        });
    }
}
