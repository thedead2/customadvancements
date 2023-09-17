package de.thedead2.customadvancements.mixin;

import de.thedead2.customadvancements.util.handler.LanguageHandler;
import net.minecraft.client.resources.ClientLanguageMap;

import net.minecraft.resources.IResource;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;

@Mixin(ClientLanguageMap.class)
public class MixinClientLanguageMap {

    @Inject(at = @At("TAIL"), method = "func_239498_a_(Ljava/util/List;Ljava/util/Map;)V")
    private static void onLoad(List<IResource> pResources, Map<String, String> pDestinationMap, CallbackInfo ci){
        ResourceLocation resourceLocation = null;
        for(IResource resource : pResources) {
            resourceLocation = resource.getLocation();
            break;
        }
        String pLanguageName = resourceLocation != null ? resourceLocation.getPath().replace("lang/", "").replace(".json", "") : "en_us";
        LanguageHandler.getInstance().inject(pLanguageName, pDestinationMap);
    }
}
