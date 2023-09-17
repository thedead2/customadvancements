package de.thedead2.customadvancements.mixin;

import de.thedead2.customadvancements.util.core.CrashHandler;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static de.thedead2.customadvancements.CustomAdvancements.MAIN_PACKAGE;

@Mixin(CrashReport.class)
public abstract class MixinCrashReport {

    @Shadow @Final private Throwable cause;
    @Shadow @Final private List<CrashReportCategory> crashReportSections;

    @Inject(at = @At("HEAD"), method = "getCompleteReport")
    public void onFriendlyReport(CallbackInfoReturnable<String> cir){
        CrashHandler crashHandler = CrashHandler.getInstance();
        if(crashHandler.resolveCrash(cause)){
            return;
        }
        crashReportSections.forEach(crashReportCategory -> {
            AtomicReference<String> errorMessage = new AtomicReference<>();
            crashReportCategory.children.forEach(crashReportCategory$Entry -> {
                String key = crashReportCategory$Entry.getKey();
                if(key.contains("Exception")){
                    errorMessage.set(crashReportCategory$Entry.getValue());
                    if(crashHandler.resolveCrash(crashReportCategory.getStackTrace(), errorMessage.get())){
                        return;
                    }
                }
                else if(key.contains("Screen")){
                    if(crashReportCategory$Entry.getValue().contains(MAIN_PACKAGE)){
                        crashHandler.addScreenCrash(crashReportCategory$Entry, cause);
                        return;
                    }
                }
            });
        });
    }
}
