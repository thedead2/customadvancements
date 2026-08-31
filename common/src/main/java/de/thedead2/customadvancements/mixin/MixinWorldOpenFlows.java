package de.thedead2.customadvancements.mixin;

import com.mojang.serialization.Dynamic;
import de.thedead2.customadvancements.CAMain;
import net.minecraft.client.gui.screens.worldselection.WorldOpenFlows;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(WorldOpenFlows.class)
public class MixinWorldOpenFlows {

    @Inject(at = @At("HEAD"), method = "openWorldLoadLevelStem(Lnet/minecraft/world/level/storage/LevelStorageSource$LevelStorageAccess;Lcom/mojang/serialization/Dynamic;ZLjava/lang/Runnable;)V")
    public void onLevelLoad(LevelStorageSource.LevelStorageAccess levelStorage, Dynamic<?> levelData, boolean safeMode, Runnable onFail, CallbackInfo ci) {
        CAMain.getInstance().getCustomAdvancementManager().setSaveMode(safeMode);
    }
}
