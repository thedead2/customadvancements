package de.thedead2.customadvancements.mixin;

import com.google.gson.JsonElement;
import de.thedead2.customadvancements.advancements.CustomAdvancementManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Map;
import java.util.Objects;

@Mixin(SimpleJsonResourceReloadListener.class)
public abstract class MixinJsonReloadListener {

    @Shadow @Final private String directory;

    @Inject(at = @At(value = "RETURN"), method = "prepare(Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)Ljava/util/Map;", locals = LocalCapture.CAPTURE_FAILSOFT)
    private void prepare(ResourceManager resourceManagerIn, ProfilerFiller pProfiler, CallbackInfoReturnable<Map<ResourceLocation, JsonElement>> cir, Map<ResourceLocation, JsonElement> map) {
        if (Objects.equals(this.directory, "advancements")) {
            map = CustomAdvancementManager.modifyData(map, resourceManagerIn);
        }
    }
}
