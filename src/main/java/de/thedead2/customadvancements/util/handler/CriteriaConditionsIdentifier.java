package de.thedead2.customadvancements.util.handler;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.resources.ResourceLocation;

import java.util.*;

public abstract class CriteriaConditionsIdentifier {

    private static final Map<ResourceLocation, List<CriteriaCondition<?>>> criteriaConditions = new HashMap<>();

    public static void load(Criterion criterion){
        CriterionTriggerInstance instance = criterion.getTrigger();
        ResourceLocation resourceLocation = instance.getCriterion();
        JsonObject jsonObject = instance.serializeToJson(SerializationContext.INSTANCE);

        List<CriteriaPreCondition> preConditions = new ArrayList<>();
        JsonHandler.getInstance().readJsonObject(preConditions, jsonObject);
        List<CriteriaCondition<?>> conditions =new ArrayList<>();
        preConditions.forEach(criteriaPreCondition -> {
            Object o = criteriaPreCondition.preCondition();

            if(o instanceof String s){
                if(s.contains(":")){
                    new CriteriaCondition<>(criteriaPreCondition.jsonFieldName(), ResourceLocation.tryParse(s));
                }
            }
            else if (o instanceof Number) {

            }
        });

        criteriaConditions.put(resourceLocation, conditions);

        /*criteriaTriggers.forEach((resourceLocation, trigger) -> {

            Class<?> triggerClass = trigger.getClass();
            try {
                Method createInstance = triggerClass.getDeclaredMethod("createInstance", JsonObject.class, DeserializationContext.class);
                Class<?> triggerInstanceClass = createInstance.getReturnType();
                Method serializeToJson = triggerInstanceClass.getDeclaredMethod("serializeToJson", SerializationContext.class);
                for(Constructor<?> constructor : triggerInstanceClass.getDeclaredConstructors()){
                    Parameter[] constructorParameters = constructor.getParameters();
                }
            }
            catch (NoSuchMethodException e) {
                LOGGER.error("Can't find declared method \"createInstance\" for class: {}", triggerClass.getName());
            }
        });*/
    }



    public static List<CriteriaCondition<?>> getConditionsFor(ResourceLocation key){
        return criteriaConditions.get(key);
    }

    public static List<CriteriaCondition<?>> getConditionsFor(Criterion criterion){
        return criteriaConditions.get(criterion.getTrigger().getCriterion());
    }

    public static List<CriteriaCondition<?>> getConditionsFor(CriterionTriggerInstance triggerInstance){
        return criteriaConditions.get(triggerInstance.getCriterion());
    }

    public record CriteriaCondition<T>(String jsonFieldName, T condition) {

        public Optional<Class<?>> getConditionType(){
            Class<?> aClass = null;
            if(condition != null){
                aClass = condition.getClass();
            }
            return Optional.ofNullable(aClass);
        }
    }

    public record CriteriaPreCondition(String jsonFieldName, Object preCondition) {
    }
}
