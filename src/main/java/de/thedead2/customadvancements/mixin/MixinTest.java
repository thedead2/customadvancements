package de.thedead2.customadvancements.mixin;

import com.google.gson.JsonObject;
import de.thedead2.customadvancements.util.handler.CriteriaConditionsIdentifier;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Criterion.class)
public class MixinTest {

    @Inject(at = @At("RETURN"), method = "criterionFromJson(Lcom/google/gson/JsonObject;Lnet/minecraft/advancements/critereon/DeserializationContext;)Lnet/minecraft/advancements/Criterion;")
    private static void onFromJson(JsonObject pJson, DeserializationContext pContext, CallbackInfoReturnable<Criterion> cir){
        Criterion criterion = cir.getReturnValue();
        CriteriaConditionsIdentifier.load(criterion);
    }
}
