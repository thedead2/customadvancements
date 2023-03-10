package de.thedead2.customadvancements.advancements.criteria;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.resources.ResourceLocation;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Trigger {

    private final ResourceLocation key;
    private final CriterionTrigger<?> criterionTrigger;
    private final ImmutableMap<String, Predicate.Key> predicates;

    public Trigger(ResourceLocation key, CriterionTrigger<?> criterionTrigger, Map.Entry<String, Predicate.Key>... predicates) {
        this.key = key;
        this.criterionTrigger = criterionTrigger;
        this.predicates = ImmutableMap.copyOf(Arrays.stream(predicates).toList());
    }

    public Trigger(ResourceLocation key, CriterionTrigger<?> criterionTrigger, Map<String, Predicate.Key> predicates) {
        this.key = key;
        this.criterionTrigger = criterionTrigger;
        this.predicates = ImmutableMap.copyOf(predicates);
    }

    public static Trigger deserializeFromJson(String key, JsonObject jsonObject) {
        ResourceLocation resourceLocation = new ResourceLocation(key);
        Map<String, Predicate.Key> keys = new HashMap<>();
        jsonObject.keySet().forEach(key1 -> {
            JsonElement jsonElement = jsonObject.get(key1);
            if(jsonElement.isJsonArray()){
                key1 = key1 + " (List)";
            }
            String string = jsonElement.getAsString();
            String sub = string.substring(string.indexOf(":") + 1);;
            if(sub.contains(":")) {
                String sub1 = sub.replace(sub.substring(sub.indexOf(":")), "");
                keys.put(key1, new Predicate.Key(sub.replace(sub1 + ":", ""), sub1));
            }
            else {
                keys.put(key1, new Predicate.Key(new ResourceLocation(string)));
            }
        });

        CriterionTrigger<?> criterionTrigger = CriteriaTriggers.getCriterion(resourceLocation);

        if(criterionTrigger == null)
            throw new IllegalStateException("Unknown CriterionTrigger for id: " + resourceLocation);

        return new Trigger(resourceLocation, criterionTrigger, keys);
    }

    public JsonObject serializeToJson(){
        JsonObject jsonObject = new JsonObject();
        predicates.forEach((s, key1) -> {
            if(s.contains("(List)")){
                JsonArray jsonArray = new JsonArray();
                jsonArray.add(key1.toString());
                String name = s.replace(" (List)", "");
                jsonObject.add(name, jsonArray);
            }
            else
                jsonObject.addProperty(s, key1.toString());
        });
        return jsonObject;
    }

    public ResourceLocation getKey() {
        return key;
    }

    public ImmutableSet<String> getFieldNames(){
        return predicates.keySet();
    }

    public ImmutableCollection<Predicate.Key> getPredicateKeys() {
        return predicates.values();
    }

    public ImmutableMap<String, Predicate.Key> getPredicates() {
        return predicates;
    }

    public CriterionTrigger<?> getCriterionTrigger() {
        return criterionTrigger;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("key", key)
                .add("criterionTrigger", criterionTrigger)
                .add("predicates", predicates)
                .toString();
    }
}
