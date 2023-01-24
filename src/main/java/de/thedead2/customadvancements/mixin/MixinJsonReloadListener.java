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

import static de.thedead2.customadvancements.util.ModHelper.ALL_ADVANCEMENTS_RESOURCE_LOCATIONS;
import static de.thedead2.customadvancements.util.ModHelper.ALL_DETECTED_GAME_ADVANCEMENTS;

@Mixin(SimpleJsonResourceReloadListener.class)
public abstract class MixinJsonReloadListener {

    @Shadow @Final private String directory;

    @Inject(at = @At(value = "RETURN"), method = "prepare(Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)Ljava/util/Map;", locals = LocalCapture.CAPTURE_FAILSOFT)
    private void prepare(ResourceManager resourceManagerIn, ProfilerFiller pProfiler, CallbackInfoReturnable<Map<ResourceLocation, JsonElement>> cir, Map<ResourceLocation, JsonElement> map) {
        if (Objects.equals(this.directory, "advancements")) {
            ALL_ADVANCEMENTS_RESOURCE_LOCATIONS.addAll(resourceManagerIn.listResources("advancements", resourceLocation -> resourceLocation.toString().endsWith(".json")).keySet());
            ALL_DETECTED_GAME_ADVANCEMENTS.putAll(map);

            map = CustomAdvancementManager.modifyData(map);
        }
    }
}
