package de.thedead2.customadvancements.advancements.criteria;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.ResourceLocationException;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringJoiner;
import java.util.function.Function;

import static de.thedead2.customadvancements.advancements.criteria.CriteriaConditionsIdentifier.ALL_PREDICATES;

public class Predicate {
    private final Key predicateKey;
    private final ImmutableList<PredicateCondition> predicateConditions;

    private Predicate(Key predicateKey, Collection<PredicateCondition> predicateConditions){
        this.predicateKey = predicateKey;
        this.predicateConditions = ImmutableList.copyOf(predicateConditions);
    }

    public Key getKey() {
        return predicateKey;
    }

    public ImmutableList<PredicateCondition> getPredicateConditions() {
        return predicateConditions;
    }

    public JsonObject serializeToJson(){
        JsonObject jsonObject = new JsonObject();
        predicateConditions.forEach(condition -> condition.toJson(jsonObject));
        return jsonObject;
    }

    public static Predicate deserializeFromJson(String key, JsonObject jsonObject){
        Key predicateKey = new Key(key);
        List<PredicateCondition> conditions = new ArrayList<>();
        jsonObject.keySet().forEach(key1 -> {
            JsonElement jsonElement = jsonObject.get(key1);
            conditions.add(PredicateCondition.fromJson(key1, jsonElement));
        });

        return new Predicate(predicateKey, conditions);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Predicate.class.getSimpleName() + "[", "]")
                .add("predicateKey=" + predicateKey)
                .add("predicateConditions=" + predicateConditions)
                .toString();
    }


    public static class Entry<T> {
        @Nullable
        private final T defaultValue;
        @NotNull
        private final String key;
        private final Function<String, T> inputConverter;
        private final java.util.function.Predicate<String> verifier;
        private final Collection<T> possibleValues;

        public Entry(@NotNull String key, @Nullable T defaultValue, Function<String, T> inputConverter, java.util.function.Predicate<String> verifier, Collection<T> possibleValues) {
            this.key = key;
            this.defaultValue = defaultValue;
            this.inputConverter = inputConverter;
            this.verifier = verifier;
            this.possibleValues = possibleValues;
        }

        public Entry(@NotNull String key, Function<String, T> inputConverter, java.util.function.Predicate<String> verifier, Collection<T> possibleValues) {
            this(key, null, inputConverter, verifier, possibleValues);
        }

        @SafeVarargs
        public Entry(@NotNull String key, @Nullable T defaultValue, Function<String, T> inputConverter, java.util.function.Predicate<String> verifier, T... possibleValues) {
            this(key, defaultValue, inputConverter, verifier, List.of(possibleValues));
        }

        @SafeVarargs
        public Entry(@NotNull String key, Function<String, T> inputConverter, java.util.function.Predicate<String> verifier, T... possibleValues) {
            this(key, inputConverter, verifier, List.of(possibleValues));
        }

        public @NotNull String getKey() {
            return key;
        }

        public @Nullable T getDefaultValue() {
            return defaultValue;
        }

        public @Nullable T convert(String input){
            if(verifier.test(input) && (possibleValues.contains(inputConverter.apply(input)) || possibleValues.isEmpty()))
                return inputConverter.apply(input);
            else return defaultValue;
        }

        public Collection<T> getPossibleValues() {
            return possibleValues;
        }

        public static Entry<?> fromJson(JsonElement jsonElement) {
            if(jsonElement.isJsonPrimitive()){
                JsonPrimitive primitive = jsonElement.getAsJsonPrimitive();
                String key = primitive.getAsString();

                return createFromKey(key);
            }
            else if(jsonElement.isJsonArray()){
                return createFromArray(jsonElement.getAsJsonArray());
            }
            else throw new IllegalArgumentException("Can't create Entry from JsonElement of type: " + jsonElement);
        }

        private static Entry<?> createFromArray(JsonArray asJsonArray) {
            List<String> list = new ArrayList<>();
            asJsonArray.forEach(jsonElement -> {
                if(jsonElement.isJsonPrimitive()){
                    list.add(jsonElement.getAsString());
                }
            });
            return new Entry<>("array", s -> s, list::contains, list);
        }


        private static Entry<?> createFromKey(String key) {
            switch (key) {
                case "boolean" -> {
                    return PredicateEntries.BOOLEAN;
                }
                case "int" -> {
                    return PredicateEntries.INTEGER;
                }
                case "double" -> {
                    return PredicateEntries.DOUBLE;
                }
                case "float" -> {
                    return PredicateEntries.FLOAT;
                }
                case "short" -> {
                    return PredicateEntries.SHORT;
                }
                case "long" -> {
                    return PredicateEntries.LONG;
                }
                case "byte" -> {
                    return PredicateEntries.BYTE;
                }
                case "String" -> {
                    return PredicateEntries.STRING;
                }
            }
            if(key.contains("ResourceLocation:")){
                String string1 = key.substring(key.indexOf(":") + 2);
                switch (string1) {
                    case "item" -> {
                        return PredicateEntries.ITEM;
                    }
                    case "potion" -> {
                        return PredicateEntries.POTION;
                    }
                    case "biome" -> {
                        return PredicateEntries.BIOME;
                    }
                    case "fluid" -> {
                        return PredicateEntries.FLUID;
                    }
                    case "block" -> {
                        return PredicateEntries.BLOCK;
                    }
                    case "enchantment" -> {
                        return PredicateEntries.ENCHANTMENT;
                    }
                    case "recipe" -> {
                        return PredicateEntries.RECIPE;
                    }
                    case "advancement" -> {
                        return PredicateEntries.ADVANCEMENT;
                    }
                    case "criterion" -> {
                        return PredicateEntries.ADVANCEMENT_CRITERION;
                    }
                    case "entity" -> {
                        return PredicateEntries.ENTITY;
                    }
                    case "effect" -> {
                        return PredicateEntries.EFFECT;
                    }
                    case "statistic" -> {
                        return PredicateEntries.STATISTIC;
                    }
                    case "item_tag" -> {
                        return PredicateEntries.ITEM_TAG;
                    }
                    case "dimension" -> {
                        return PredicateEntries.DIMENSION;
                    }
                    case "fluid_tag" -> {
                        return PredicateEntries.FLUID_TAG;
                    }
                    case "structure" -> {
                        return PredicateEntries.STRUCTURE;
                    }
                    case "block_tag" -> {
                        return PredicateEntries.BLOCK_TAG;
                    }
                    case "loot_table" -> {
                        return PredicateEntries.LOOT_TABLE;
                    }
                }
            }
            else if(key.contains("predicates:")){
                Function<String, Predicate> function = (s) -> {
                    Key key1 = new Key(new ResourceLocation(s));
                    return ALL_PREDICATES.get(key1);
                };
                return new Entry<>(key, function, s -> {
                    try{
                        Key key1 = new Key(new ResourceLocation(s));
                        return ALL_PREDICATES.containsKey(key1);
                    }
                    catch (ResourceLocationException e){
                        return false;
                    }
                }, ALL_PREDICATES.values());
            }
            else if(key.contains(":")){
                Function<String, ResourceLocation> function = ResourceLocation::new;
                return new Entry<>(key, function, s -> {
                    try {
                        new ResourceLocation(s);
                        return true;
                    }
                    catch (ResourceLocationException e){
                        return false;
                    }
                });
            }
            throw new IllegalArgumentException("Unknown entry key for input: " + key);
        }

        public JsonElement toJson() {
            if(key.equals("array")){
                JsonArray jsonArray = new JsonArray();
                possibleValues.forEach(t -> jsonArray.add(t.toString()));
                return jsonArray;
            }
            else return new JsonPrimitive(key);
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("key", key)
                    .add("defaultValue", defaultValue)
                    .add("inputConverter", inputConverter)
                    .add("verifier", verifier)
                    .toString();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Entry<?> entry = (Entry<?>) o;
            return Objects.equal(getDefaultValue(), entry.getDefaultValue()) && Objects.equal(getKey(), entry.getKey()) && Objects.equal(inputConverter, entry.inputConverter) && Objects.equal(verifier, entry.verifier);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(getDefaultValue(), getKey(), inputConverter, verifier);
        }
    }



    public static class Key{
        @NotNull
        private final ResourceLocation keyValue;
        @Nullable
        private final String subKey;

        public Key(@NotNull String name, @Nullable String subKey){
            this(new ResourceLocation("predicates", name), subKey);
        }

        public Key(@NotNull ResourceLocation name){
            this.keyValue = name;
            this.subKey = null;
        }

        public Key(@NotNull ResourceLocation name, @Nullable String subKey){
            this.keyValue = name;
            this.subKey = subKey;
        }

        public Key(@NotNull String name) {
            this(new ResourceLocation("predicates", name));
        }

        public String getName(){
            return keyValue.getPath();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Key key = (Key) o;
            return Objects.equal(keyValue, key.keyValue) && Objects.equal(subKey, key.subKey);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(keyValue, subKey);
        }

        @Override
        public String toString() {
            if(subKey == null) return keyValue.toString();
            else return keyValue + ":" + subKey;
        }
    }
}
