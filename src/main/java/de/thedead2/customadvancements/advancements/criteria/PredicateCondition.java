package de.thedead2.customadvancements.advancements.criteria;

import com.google.common.base.MoreObjects;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PredicateCondition {

    static PredicateCondition fromJson(String key, JsonElement jsonElement) {
        if(jsonElement.isJsonPrimitive())
            return PrimitiveCondition.fromJson(key, jsonElement);
        else if(jsonElement.isJsonObject())
            return ObjectCondition.fromJson(key, jsonElement);
        else if(jsonElement.isJsonArray())
            return PrimitiveArrayCondition.fromJson(key, jsonElement);
        else
            return null;
    }

    String getKey();

    void toJson(JsonObject jsonObject);

    Optional<?> getEntries();


    record PrimitiveCondition(String key, Predicate.Entry<?> entry) implements PredicateCondition{

        public static PrimitiveCondition fromJson(String name, JsonElement jsonElement) {
            return new PrimitiveCondition(name, Predicate.Entry.fromJson(jsonElement));
        }

        @Override
        public String getKey() {
            return key();
        }


        @Override
        public void toJson(JsonObject jsonObject) {
            jsonObject.add(this.key, this.entry.toJson());
        }

        @Override
        public Optional<?> getEntries() {
            return Optional.of(entry);
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("key", key)
                    .add("entry", entry)
                    .toString();
        }
    }

    record PrimitiveArrayCondition(String key, Collection<Predicate.Entry<?>> entries) implements PredicateCondition{

        public static PrimitiveArrayCondition fromJson(String name, JsonElement jsonElement) {
            JsonArray jsonArray = jsonElement.getAsJsonArray();
            List<Predicate.Entry<?>> entries1 = new ArrayList<>();
            jsonArray.forEach(jsonElement1 -> entries1.add(Predicate.Entry.fromJson(jsonElement1)));
            return new PrimitiveArrayCondition(name, entries1);
        }

        @Override
        public String getKey() {
            return key;
        }

        @Override
        public void toJson(JsonObject jsonObject) {
            JsonArray jsonArray = new JsonArray();
            this.entries.forEach(entry -> jsonArray.add(entry.toJson()));
            jsonObject.add(this.key, jsonArray);
        }

        @Override
        public Optional<?> getEntries() {
            return Optional.of(entries);
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("key", key)
                    .add("entries", entries)
                    .toString();
        }
    }

    record ObjectCondition(String key, Collection<PredicateCondition> entries) implements PredicateCondition {

        public static ObjectCondition fromJson(String name, JsonElement jsonElement) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            List<PredicateCondition> conditions = new ArrayList<>();
            jsonObject.keySet().forEach(key1 -> {
                JsonElement jsonElement1 = jsonObject.get(key1);
                if(jsonElement1.isJsonPrimitive() || jsonElement1.isJsonArray()){
                    conditions.add(PrimitiveCondition.fromJson(key1, jsonElement1));
                }
                else conditions.add(PredicateCondition.fromJson(key1, jsonElement1));
            });
            return new ObjectCondition(name, conditions);
        }

        @Override
        public String getKey() {
            return key;
        }

        @Override
        public void toJson(JsonObject jsonObject) {
            JsonObject jsonObject1 = new JsonObject();
            this.entries.forEach(predicateCondition -> predicateCondition.toJson(jsonObject1));
            jsonObject.add(this.key, jsonObject1);
        }

        @Override
        public Optional<?> getEntries() {
            return Optional.of(entries);
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("key", key)
                    .add("entries", entries)
                    .toString();
        }
    }
}
