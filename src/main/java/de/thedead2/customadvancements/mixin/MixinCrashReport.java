package de.thedead2.customadvancements.mixin;

import de.thedead2.customadvancements.CustomAdvancements;
import de.thedead2.customadvancements.util.core.CrashHandler;
import de.thedead2.customadvancements.util.core.ModHelper;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import org.apache.logging.log4j.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static de.thedead2.customadvancements.util.core.ModHelper.JAVA_PATH;

@Mixin(CrashReport.class)
public abstract class MixinCrashReport {

    @Shadow @Final private Throwable exception;
    @Shadow @Final private List<CrashReportCategory> details;

    @Inject(at = @At("HEAD"), method = "getFriendlyReport")
    public void onFriendlyReport(CallbackInfoReturnable<String> cir){
        CrashHandler crashHandler = CrashHandler.getInstance();
        try {
            if(crashHandler.resolveCrash(exception)){
                return;
            }
            details.forEach(crashReportCategory -> {
                AtomicReference<String> errorMessage = new AtomicReference<>();
                for (CrashReportCategory.Entry entry : crashReportCategory.entries) {
                    String key = entry.getKey();
                    if(key.contains("Exception")){
                        errorMessage.set(entry.getValue());
                        if(crashHandler.resolveCrash(crashReportCategory.getStacktrace(), errorMessage.get())){
                            break;
                        }
                    }
                    else if(key.contains("Screen")){
                        if(entry.getValue().contains(JAVA_PATH)){
                            crashHandler.addScreenCrash(entry, exception);
                            break;
                        }
                    }
                }
            });
        }
        catch (Throwable e){
            crashHandler.handleException("Error while reading crash-report", e, Level.ERROR);
        }
    }
}
