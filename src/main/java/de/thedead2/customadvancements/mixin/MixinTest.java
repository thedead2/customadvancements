package de.thedead2.customadvancements.mixin;

import com.google.gson.JsonObject;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.critereon.SerializationContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CriterionTriggerInstance.class)
public class MixinTest {

    @Inject(at = @At("RETURN"), method = "serializeToJson(Lnet/minecraft/advancements/critereon/SerializationContext;)Lcom/google/gson/JsonObject;")
    private static void onFromJson(SerializationContext serializationContext, CallbackInfoReturnable<JsonObject> cir){

    }
}
