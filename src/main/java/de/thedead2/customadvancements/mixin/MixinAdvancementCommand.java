package de.thedead2.customadvancements.mixin;

import net.minecraft.advancements.Advancement;
import net.minecraft.server.commands.AdvancementCommands;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static de.thedead2.customadvancements.util.handler.AdvancementHandler.grantingAllAdvancements;

@Mixin(AdvancementCommands.Action.class)
public abstract class MixinAdvancementCommand {

    @Inject(at = @At("HEAD"), method = "perform(Lnet/minecraft/server/level/ServerPlayer;Ljava/lang/Iterable;)I")
    public void onGrantingAll(ServerPlayer pPlayer, Iterable<Advancement> pAdvancements, CallbackInfoReturnable<Integer> cir){
        grantingAllAdvancements = true;
    }

    @Inject(at = @At("RETURN"), method = "perform(Lnet/minecraft/server/level/ServerPlayer;Ljava/lang/Iterable;)I")
    public void onGrantingAll2(ServerPlayer pPlayer, Iterable<Advancement> pAdvancements, CallbackInfoReturnable<Integer> cir){
        grantingAllAdvancements = false;
    }
}
