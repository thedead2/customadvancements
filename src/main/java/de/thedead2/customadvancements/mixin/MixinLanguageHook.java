package de.thedead2.customadvancements.mixin;

import de.thedead2.customadvancements.util.io.LanguageHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.server.LanguageHook;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;


@Mixin(LanguageHook.class)
public abstract class MixinLanguageHook {

    @Shadow(remap = false)
    private static Map<String, String> modTable;


    @Inject(at = @At("TAIL"), method = "loadLanguage(Ljava/lang/String;Lnet/minecraft/server/MinecraftServer;)V", remap = false)
    private static void loadLanguage(String langName, MinecraftServer server, CallbackInfo ci) {
        LanguageHandler.inject(langName, modTable);
    }
}
