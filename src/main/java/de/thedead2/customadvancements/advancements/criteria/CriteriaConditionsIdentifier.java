package de.thedead2.customadvancements.advancements.criteria;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;
import de.thedead2.customadvancements.util.Timer;
import de.thedead2.customadvancements.util.handler.FileHandler;
import de.thedead2.customadvancements.util.handler.JsonHandler;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.resources.ResourceLocation;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;

import static de.thedead2.customadvancements.util.ModHelper.*;

public abstract class CriteriaConditionsIdentifier {

    public static final Map<ResourceLocation, TriggerConditions> CRITERIA_CONDITIONS = new HashMap<>();
    public static final Map<Predicate.Key, Predicate> ALL_PREDICATES = new HashMap<>();
    public static final Map<ResourceLocation, Trigger> ALL_TRIGGERS = new HashMap<>();
    public static final Map<ResourceLocation, CriterionTrigger<?>> CRITERIA_TRIGGERS = ImmutableMap.copyOf(CriteriaTriggers.CRITERIA);
    public static final File conditionsFile = new File(String.valueOf(DIR_PATH.resolve("criteriaConditions.json")));
    public static final File newconditionsFile = new File(String.valueOf(DIR_PATH.resolve("test.json")));




    public static void init(){
        Timer timer = new Timer(true);
        LOGGER.debug("Starting to resolve conditions for criteria triggers...");
        if(!conditionsFile.exists()){
            JsonObject jsonObject = new JsonObject();
            try {
                FileHandler.writeFile(new ByteArrayInputStream(jsonObject.toString().getBytes()), conditionsFile.toPath());
            }
            catch (IOException e) {
                LOGGER.error("Couldn't write file: {}", conditionsFile.getPath());
            }
        }
        deserializeFromJson(JsonHandler.getInstance().getJsonObject(newconditionsFile));

        createCriteriaConditions();

        LOGGER.info("All predicates:");
        ALL_PREDICATES.forEach((key, predicate) -> LOGGER.debug(predicate));

        LOGGER.info("All triggers:");
        ALL_TRIGGERS.forEach((resourceLocation, trigger) -> LOGGER.debug(trigger));

        LOGGER.info("All trigger conditions:");
        CRITERIA_CONDITIONS.forEach((resourceLocation, triggerConditions) -> LOGGER.debug(triggerConditions));

        /*criteriaTriggers.forEach((resourceLocation, trigger) -> {
            if(criteriaConditions.containsKey(resourceLocation)){
                return;
            }
            LOGGER.debug("Trying to resolve conditions for criteria trigger: " + resourceLocation);


            *//*Map<Constructor<?>, Parameter[]> constructorMap = new HashMap<>();
            Class<?> triggerClass = trigger.getClass();
            try {
                Method[] methods = triggerClass.getDeclaredMethods();
                Method createInstance = null;
                for (Method method : methods) {
                    if(method.getName().equals("createInstance")){
                        createInstance = method;
                        break;
                    }
                }
                if(createInstance == null){
                    throw new NoSuchMethodException();
                }
                Class<?> triggerInstanceClass = createInstance.getReturnType();
                getConstructorParameters(triggerInstanceClass.getDeclaredConstructors(), constructorMap);

                criteriaConditions.put(resourceLocation, new CriteriaConditions(resourceLocation, triggerInstanceClass, resolveClasses(constructorMap)));
            }
            catch (NoSuchMethodException e) {
                LOGGER.error("Can't find declared method \"createInstance\" for class: {}", triggerClass.getName());
            }*//*
        });*/

        timer.stop();
        LOGGER.debug("Resolving criteria conditions took: " + timer.getTime() + " ms");
    }

    private static void createCriteriaConditions() {
        ALL_TRIGGERS.forEach((resourceLocation, trigger) -> {
            TriggerConditions triggerConditions = TriggerConditions.fromTrigger(trigger);
            CRITERIA_CONDITIONS.put(resourceLocation, triggerConditions);
        });
    }

    public static void deserializePredicates(JsonObject jsonObject){
        JsonObject jsonObject1 = jsonObject.getAsJsonObject("predicates");

        jsonObject1.entrySet().forEach(entry -> {
            Predicate predicate = Predicate.deserializeFromJson(entry.getKey(), entry.getValue().getAsJsonObject());
            ALL_PREDICATES.put(predicate.getKey(), predicate);
        });
    }

    public static void serializePredicates(JsonObject jsonObject) {
        JsonObject jsonObject1 = new JsonObject();
        ALL_PREDICATES.forEach((key, predicate) -> {
            jsonObject1.add(key.getName(), predicate.serializeToJson());
        });
        jsonObject.add("predicates", jsonObject1);
    }

    /*private static Map<Class<?>, List<Class<?>>> resolveClasses(Map<Constructor<?>, Parameter[]> constructorMap) {
        Map<Class<?>, List<Class<?>>> constructorParameterClasses = new HashMap<>();
        constructorMap.forEach((constructor, parameters) -> {
            Class<?> constructorClass = constructor.getDeclaringClass();
            List<Class<?>> parameterClasses = new ArrayList<>();
            for (Parameter parameter : parameters) {
                parameterClasses.add(parameter.getType());
            }
            constructorParameterClasses.put(constructorClass, parameterClasses);
        });
        return constructorParameterClasses;
    }*/

    private static void deserializeFromJson(JsonObject jsonObject){
        //try {
            if(jsonObject != null && (jsonObject.isJsonNull() || jsonObject.size() == 0))
                return;
            else if(jsonObject == null)
                return;

            deserializePredicates(jsonObject);
            deserializeTriggers(jsonObject);


            /*if (!jsonObject.has("triggerConditions")) {
                throw new IllegalArgumentException("Can't identify triggerConditions from json object: " + jsonObject);
            }
            JsonArray jsonArray = jsonObject.getAsJsonArray("triggerConditions");
            jsonArray.forEach(jsonElement -> {
                CriteriaConditions.deserializeFromJson(jsonElement.getAsJsonObject()).ifPresentOrElse(criteriaConditions1 -> criteriaConditions.put(criteriaConditions1.getTriggerName(), criteriaConditions1),
                        () -> {
                    throw new IllegalStateException("Criteria conditions cannot be null!");
                });
            });
        }
        catch (IllegalArgumentException e){
            LOGGER.error("Can't deserialize CriteriaConditions from json object: " + e);
        }
        catch (IllegalStateException e){
            e.printStackTrace();
        }*/
    }

    private static void deserializeTriggers(JsonObject jsonObject) {
        JsonObject jsonObject1 = jsonObject.getAsJsonObject("triggers");
        jsonObject1.entrySet().forEach(entry -> {
            Trigger trigger = Trigger.deserializeFromJson(entry.getKey(), entry.getValue().getAsJsonObject());
            ALL_TRIGGERS.put(trigger.getKey(), trigger);
        });
    }

    /*private static void getConstructorParameters(Constructor<?>[] constructors, Map<Constructor<?>, Parameter[]> constructorMap){
        for(Constructor<?> constructor : constructors){
            if(constructorMap.containsKey(constructor)){
                break;
            }
            Parameter[] constructorParameters = constructor.getParameters();
            for (Parameter parameter : constructorParameters) {
                Class<?> parameterClass = parameter.getType();
                if(parameterClass.getName().equals("java.lang.String") || parameterClass.getName().equals("java.lang.Integer")
                        || parameterClass.getName().equals("java.lang.Double") || parameterClass.getName().equals("java.lang.Long")
                        || parameterClass.getName().equals("java.lang.Number") || parameterClass.getName().equals("java.lang.Short")
                        || parameterClass.getName().equals("java.lang.Character") || parameterClass.getName().equals("java.lang.Boolean"))
                {
                    break;
                }
                getConstructorParameters(parameterClass.getDeclaredConstructors(), constructorMap);
            }
            constructorMap.put(constructor, constructorParameters);
        }
    }*/

    public static void save(){
        try {
            LOGGER.debug("Saving criteria conditions to file...");
            FileHandler.writeFile(new ByteArrayInputStream(JsonHandler.formatJsonObject(serializeToJson()).getBytes()), conditionsFile.toPath());
        }
        catch (IOException e) {
            LOGGER.error("Couldn't write criteria conditions data to file!");
        }
    }

    private static JsonObject serializeToJson(){
        JsonObject jsonObject = new JsonObject();
        serializePredicates(jsonObject);
        serializeTriggers(jsonObject);


        /*JsonArray jsonArray = new JsonArray();
        criteriaConditions.forEach((resourceLocation, criteriaConditions1) -> jsonArray.add(criteriaConditions1.serializeToJson()));
        jsonObject.add("triggerConditions", jsonArray);*/
        return jsonObject;
    }

    private static void serializeTriggers(JsonObject jsonObject) {
        JsonObject jsonObject1 = new JsonObject();
        ALL_TRIGGERS.forEach((key, trigger) -> {
            jsonObject1.add(key.toString(), trigger.serializeToJson());
        });
        jsonObject.add("triggers", jsonObject1);
    }


    public static TriggerConditions getConditionsFor(ResourceLocation key){
        return CRITERIA_CONDITIONS.get(key);
    }

    public static TriggerConditions getConditionsFor(Criterion criterion){
        return CRITERIA_CONDITIONS.get(criterion.getTrigger().getCriterion());
    }

    public static TriggerConditions getConditionsFor(CriterionTriggerInstance triggerInstance){
        return CRITERIA_CONDITIONS.get(triggerInstance.getCriterion());
    }

    public static void emergencySave() {
        try {
            LOGGER.fatal("Emergency saving criteria conditions to file...");
            FileHandler.writeFile(new ByteArrayInputStream(JsonHandler.formatJsonObject(serializeToJson()).getBytes()), conditionsFile.toPath());
        }
        catch (IOException e) {
            LOGGER.error("Couldn't write criteria conditions data to file!");
        }
    }

    /*public static class CriteriaConditions {
        private final Map<Class<?>, List<Class<?>>> conditions;
        private final ResourceLocation triggerName;
        private final Class<?> triggerInstanceClass;


        CriteriaConditions(ResourceLocation triggerName, Class<?> triggerInstanceClass, Map<Class<?>, List<Class<?>>> conditionClasses){
            this.triggerName = triggerName;
            this.conditions = conditionClasses;
            this.triggerInstanceClass = triggerInstanceClass;
        }

        public ResourceLocation getTriggerName() {
            return this.triggerName;
        }

        public Map<Class<?>, List<Class<?>>> getConditions() {
            return this.conditions;
        }

        public Class<?> getTriggerInstanceClass() {
            return this.triggerInstanceClass;
        }

        public static Optional<CriteriaConditions> deserializeFromJson(JsonObject jsonObject){
            CriteriaConditions criteriaConditions = null;
            try {
                if(!jsonObject.has("trigger")){
                    throw new IllegalArgumentException("Couldn't get trigger resource location from json object!");
                }
                ResourceLocation triggerName = new ResourceLocation(jsonObject.get("trigger").getAsString());

                if (criteriaTriggers.get(triggerName) == null) {
                    throw new IllegalArgumentException("Unknown trigger with resource location: " + triggerName);
                }

                if(!jsonObject.has("triggerInstanceClass")){
                    throw new IllegalArgumentException("Can't find triggerInstanceClassField");
                }
                Class<?> triggerInstanceClass = Class.forName(jsonObject.get("triggerInstanceClass").getAsString());
                Map<Class<?>, List<Class<?>>> conditionClasses = new HashMap<>();

                if(jsonObject.has("conditions")){
                    JsonObject jsonObject1 = jsonObject.getAsJsonObject("conditions");
                    for (String key : jsonObject1.keySet()) {
                        try {
                            Class<?> constructor = Class.forName(key);
                            List<Class<?>> parameters = new ArrayList<>();
                            for (JsonElement jsonElement1 : jsonObject1.getAsJsonArray(key)){
                                String parameterName = jsonElement1.getAsString();
                                Class<?> parameter = Class.forName(parameterName);
                                parameters.add(parameter);
                            }
                            conditionClasses.put(constructor, parameters);
                        }
                        catch (ClassNotFoundException e) {
                            LOGGER.error("Can't resolve class for key: {}", key);
                        }
                    }
                }

                criteriaConditions = new CriteriaConditions(triggerName, triggerInstanceClass, conditionClasses);
            }
            catch (IllegalArgumentException | ResourceLocationException e){
                LOGGER.error("Couldn't deserialize CriteriaConditions from json: " + e);
            }
            catch (ClassNotFoundException e) {
                LOGGER.error("Can't find trigger instance class: " + e);
            }
            return Optional.ofNullable(criteriaConditions);
        }

        public JsonObject serializeToJson(){
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("trigger", this.triggerName.toString());
            jsonObject.addProperty("triggerInstanceClass", this.triggerInstanceClass.getName());
            JsonObject jsonObject1 = new JsonObject();

            this.conditions.forEach((constructor, parameters) -> {
                JsonArray jsonArray = new JsonArray();
                parameters.forEach(parameter -> jsonArray.add(parameter.getName()));
                jsonObject1.add(constructor.getName(), jsonArray);
            });

            if(jsonObject1.size() != 0){
                jsonObject.add("conditions", jsonObject1);
            }
            return jsonObject;
        }

        @Override
        public String toString() {
            return "CriteriaConditions{" +
                    "triggerName=" + this.triggerName +
                    ", triggerInstanceClass=" + this.triggerInstanceClass +
                    ", conditions=" + this.conditions +
                    '}';
        }
    }*/
}
