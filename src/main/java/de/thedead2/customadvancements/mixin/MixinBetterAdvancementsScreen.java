package de.thedead2.customadvancements.mixin;

import betteradvancements.gui.BetterAdvancementTab;
import de.thedead2.customadvancements.util.core.ConfigManager;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Map;

@Pseudo
@Mixin(targets = "betteradvancements.gui.BetterAdvancementsScreen")
public class MixinBetterAdvancementsScreen {

    @Shadow @Final private Map<Advancement, BetterAdvancementTab> tabs;

    @Inject(at = @At("HEAD"), method = "onUpdateAdvancementProgress", cancellable = true)
    public void onUpdateAdvancementProgress(Advancement advancementIn, AdvancementProgress progress, CallbackInfo ci){
        if (advancementIn == null){
            ci.cancel();
        }
    }

    @Inject(at =  @At(value = "INVOKE", target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", shift = At.Shift.AFTER), method = "onAddAdvancementRoot(Lnet/minecraft/advancements/Advancement;)V", locals =
            LocalCapture.CAPTURE_FAILSOFT, remap = false)
    public void onNewAdvancementTab(Advancement pAdvancement, CallbackInfo ci){
        ConfigManager.ADVANCEMENT_TAB_SORTING_MODE.get().sortAdvancementTabs(this.tabs);
    }
}
