package de.thedead2.customadvancements.mixin;

import de.thedead2.customadvancements.advancements.CustomAdvancementManager;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.WorldOpenFlows;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(WorldOpenFlows.class)
public class MixinWorldOpenFlows {

    @Inject(at = @At("HEAD"), method = "doLoadLevel(Lnet/minecraft/client/gui/screens/Screen;Ljava/lang/String;ZZZ)V", remap = false)
    public void onLevelLoad(Screen lastScreen, String levelName, boolean safeMode, boolean askForBackup, boolean confirmExperimentalWarning, CallbackInfo ci) {
        CustomAdvancementManager.setSaveMode(safeMode);
    }
}
