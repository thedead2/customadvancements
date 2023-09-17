package de.thedead2.customadvancements.mixin;

import de.thedead2.customadvancements.util.core.ConfigManager;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.client.gui.advancements.AdvancementTabGui;
import net.minecraft.client.gui.advancements.AdvancementsScreen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Map;

@Mixin(AdvancementsScreen.class)
public class MixinAdvancementsScreen {

    @Shadow @Final private Map<Advancement, AdvancementTabGui> tabs;

    @Inject(at = @At("HEAD"), method = "onUpdateAdvancementProgress", cancellable = true)
    public void onUpdateAdvancementProgress(Advancement advancementIn, AdvancementProgress progress, CallbackInfo ci){
        if (advancementIn == null){
            ci.cancel();
        }
    }

    @Inject(at =  @At(value = "INVOKE", target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", shift = At.Shift.AFTER), method = "rootAdvancementAdded(Lnet/minecraft/advancements/Advancement;)V", locals = LocalCapture.CAPTURE_FAILSOFT)
    public void onNewAdvancementTab(Advancement pAdvancement, CallbackInfo ci){
        ConfigManager.ADVANCEMENT_TAB_SORTING_MODE.get().sortAdvancementTabs(this.tabs);
    }
}
