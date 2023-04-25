package de.thedead2.customadvancements.mixin;

import de.thedead2.customadvancements.util.core.ModHelper;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Function;

@Mixin(MinecraftServer.class)
public class MixinServer {

    @Inject(at = @At("RETURN"), method = "spin(Ljava/util/function/Function;)Lnet/minecraft/server/MinecraftServer;")
    private static <S extends MinecraftServer> void onServerStart(Function<Thread, S> pThreadFunction, CallbackInfoReturnable<S> cir){
        ModHelper.setServer(cir.getReturnValue());
    }
}
