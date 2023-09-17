package de.thedead2.customadvancements.mixin;

import net.minecraft.advancements.Advancement;
import net.minecraft.command.impl.AdvancementCommand;
import net.minecraft.entity.player.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static de.thedead2.customadvancements.util.handler.AdvancementHandler.grantingAllAdvancements;

@Mixin(AdvancementCommand.Action.class)
public abstract class MixinAdvancementCommand {

    @Inject(at = @At("HEAD"), method = "applyToAdvancements(Lnet/minecraft/entity/player/ServerPlayerEntity;Ljava/lang/Iterable;)I")
    public void onGrantingAll(ServerPlayerEntity pPlayer, Iterable<Advancement> pAdvancements, CallbackInfoReturnable<Integer> cir){
        grantingAllAdvancements = true;
    }

    @Inject(at = @At("RETURN"), method = "applyToAdvancements(Lnet/minecraft/entity/player/ServerPlayerEntity;Ljava/lang/Iterable;)I")
    public void onGrantingAll2(ServerPlayerEntity pPlayer, Iterable<Advancement> pAdvancements, CallbackInfoReturnable<Integer> cir){
        grantingAllAdvancements = false;
    }
}
