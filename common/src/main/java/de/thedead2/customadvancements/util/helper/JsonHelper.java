package de.thedead2.customadvancements.util.helper;


import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import joptsimple.internal.Strings;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;


public class JsonHelper {

    public static void removeNullFields(JsonElement jsonElement) {
        if (isJsonNull(jsonElement)) {
            throw new NullPointerException("Can't remove null fields from JsonElement that is null! -> " + jsonElement);
        }

        if (jsonElement.isJsonObject()) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            List<String> nullFields = new ArrayList<>();

            jsonObject.entrySet().forEach(entry -> {
                if (isJsonNull(entry.getValue())) {
                    nullFields.add(entry.getKey());
                }
                else {
                    removeNullFields(entry.getValue());
                }
            });

            nullFields.forEach(jsonObject::remove);
        }
        else if (jsonElement.isJsonArray()) {
            JsonArray jsonArray = jsonElement.getAsJsonArray();

            jsonArray.forEach(jsonElement1 -> {
                if (isJsonNull(jsonElement1)) {
                    jsonArray.remove(jsonElement1);
                }
                else {
                    removeNullFields(jsonElement1);
                }
            });
        }
    }


    private static boolean isJsonNull(JsonElement jsonElement) {
        return jsonElement.isJsonNull() || (jsonElement.isJsonPrimitive() && jsonElement.getAsString().equals("null"));
    }


    public static String formatJsonObject(JsonElement jsonElement) {
        StringBuilder stringBuilder = new StringBuilder();
        char[] chars = jsonElement.toString().toCharArray();
        int i = 0;

        for (int j = 0; j < chars.length; j++) {
            char c = chars[j];
            char previousChar = j - 1 < 0 ? c : chars[j - 1];
            char nextChar = j + 1 >= chars.length ? c : chars[j + 1];

            if (c == '{') {
                stringBuilder.append(c);

                if (nextChar != '}') {
                    i++;
                    stringBuilder.append('\n').append(Strings.repeat('\t', i));
                }
            }
            else if (c == '}') {
                if (previousChar != '{') {
                    i--;
                    stringBuilder.append("\n").append(Strings.repeat('\t', i));
                }

                stringBuilder.append(c);

                if (nextChar != ',' && nextChar != '\"' && nextChar != '\'' && nextChar != '}' && nextChar != ']') {
                    stringBuilder.append('\n').append(Strings.repeat('\t', i));
                }
            }
            else if (c == ',') {
                stringBuilder.append(c).append('\n').append(Strings.repeat('\t', i));
            }
            else if (c == '[' && (nextChar == '\"' || nextChar == '[')) {
                i++;
                stringBuilder.append(c).append('\n').append(Strings.repeat('\t', i));
            }
            else if (c == ']' && (previousChar == '\"' || previousChar == ']')) {
                i--;
                stringBuilder.append('\n').append(Strings.repeat('\t', i)).append(c);
            }
            else {
                stringBuilder.append(c);
            }
        }

        return stringBuilder.toString();
    }

    public static boolean isCorrectJsonFormat(@NotNull JsonObject json) {
        if (!json.has("criteria") || !json.has("display")) {
            return false;
        }

        return json.has("parent") || json.getAsJsonObject("display").has("background");
    }
}
