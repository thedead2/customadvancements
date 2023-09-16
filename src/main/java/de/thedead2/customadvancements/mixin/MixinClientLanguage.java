package de.thedead2.customadvancements.mixin;

import de.thedead2.customadvancements.util.handler.LanguageHandler;
import net.minecraft.client.resources.language.ClientLanguage;
import net.minecraft.server.packs.resources.Resource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;

@Mixin(ClientLanguage.class)
public class MixinClientLanguage {

    @Inject(at = @At("TAIL"), method = "appendFrom(Ljava/lang/String;Ljava/util/List;Ljava/util/Map;)V")
    private static void onLoad(String pLanguageName, List<Resource> pResources, Map<String, String> pDestinationMap, CallbackInfo ci){
        LanguageHandler.inject(pLanguageName, pDestinationMap);
    }
}
