package de.thedead2.customadvancements.mixin;

import de.thedead2.customadvancements.util.handler.LanguageHandler;
import net.minecraft.client.resources.language.ClientLanguage;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;

@Mixin(ClientLanguage.class)
public class MixinClientLanguage {

    @Inject(at = @At("TAIL"), method = "appendFrom(Ljava/util/List;Ljava/util/Map;)V")
    private static void onLoad(List<Resource> pResources, Map<String, String> pDestinationMap, CallbackInfo ci){
        ResourceLocation resourceLocation = null;
        for(Resource resource : pResources) {
            resourceLocation = resource.getLocation();
            break;
        }
        String pLanguageName = resourceLocation != null ? resourceLocation.getPath().replace("lang/", "").replace(".json", "") : "en_us";
        LanguageHandler.inject(pLanguageName, pDestinationMap);
    }
}
