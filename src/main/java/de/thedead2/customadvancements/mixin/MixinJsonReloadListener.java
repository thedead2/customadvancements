package de.thedead2.customadvancements.mixin;

import com.google.gson.JsonElement;
import de.thedead2.customadvancements.advancements.CustomAdvancementManager;
import net.minecraft.client.resources.JsonReloadListener;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Map;
import java.util.Objects;

@Mixin(JsonReloadListener.class)
public abstract class MixinJsonReloadListener {

    @Shadow @Final private String folder;

    @Inject(at = @At(value = "RETURN"), method = "prepare(Lnet/minecraft/resources/IResourceManager;Lnet/minecraft/profiler/IProfiler;)Ljava/util/Map;", locals = LocalCapture.CAPTURE_FAILSOFT)
    private void prepare(IResourceManager resourceManagerIn, IProfiler profilerIn, CallbackInfoReturnable<Map<ResourceLocation, JsonElement>> cir, Map<ResourceLocation, JsonElement> map) {
        if (Objects.equals(this.folder, "advancements")) {
            CustomAdvancementManager.modifyAdvancementData(map);
        }
    }
}
