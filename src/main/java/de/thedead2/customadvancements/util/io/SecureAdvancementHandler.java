package de.thedead2.customadvancements.util.io;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import de.thedead2.customadvancements.util.ResourceLocationHelper;
import it.unimi.dsi.fastutil.chars.CharArrayList;
import net.minecraft.ResourceLocationException;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import static de.thedead2.customadvancements.util.core.ModHelper.PATH_SEPARATOR;


public class SecureAdvancementHandler {

    public static boolean checkAndUpdateAdvancementFormat(JsonObject jsonObject, File file) throws IOException {
        String fileName = file.getName();
        String s = checkAndCorrectResourceLocation(fileName);

        testJson(jsonObject);

        if (!fileName.equals(s)) {
            if (file.delete()) {
                AdvancementHandler.writeAdvancementToFile(ResourceLocationHelper.createIdFromPath(s), jsonObject);
            }
        }

        return file.toPath().toString().contains("recipes" + PATH_SEPARATOR) || isCorrectJsonFormat(jsonObject);
    }


    public static String checkAndCorrectResourceLocation(String id) {
        if (!id.contains(":") || !ResourceLocation.isValidResourceLocation(id)) {
            return id;
        }

        CharArrayList chars = new CharArrayList(id.toCharArray());

        for (int i = 0; i < chars.size(); i++) {
            char c = chars.getChar(i);

            if (!ResourceLocation.isAllowedInResourceLocation(c)) {
                Character c1 = null;

                switch (c) {
                    case 'ä', 'Ä' -> c1 = 'a';
                    case 'ö', 'Ö' -> c1 = 'o';
                    case 'ü', 'Ü' -> c1 = 'u';
                    case ' ' -> chars.set(i, '_');
                    case '\\' -> chars.set(i, '/');
                }

                if (c1 != null) {
                    chars.set(i, c1.charValue());

                    if (i + 1 < chars.size()) {
                        chars.add(i + 1, 'e');
                    }
                    else {
                        chars.add('e');
                    }
                }
            }
        }

        String s = new String(chars.toCharArray());
        s = s.toLowerCase();

        if (!ResourceLocation.isValidResourceLocation(s)) {
            throw new ResourceLocationException("Couldn't convert string " + s + " into a valid resource location!");
        }

        return s;
    }


    public static void testJson(JsonElement jsonElement) {
        if (jsonElement.isJsonArray()) {
            JsonArray jsonArray = jsonElement.getAsJsonArray();
            JsonArray copy = jsonArray.deepCopy();

            for (int i = 0; i < copy.size(); i++) {
                JsonElement jsonElement1 = copy.get(i);
                JsonElement c = checkAndCorrectIdsIfNecessary(jsonElement1);

                if (c != null) {
                    jsonArray.set(i, c);
                }
            }
        }
        else if (jsonElement.isJsonObject()) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();

            for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                String s = checkAndCorrectResourceLocation(entry.getKey());
                JsonElement jsonElement1 = checkAndCorrectIdsIfNecessary(entry.getValue());

                if (!s.equals(entry.getKey())) {
                    jsonObject.remove(entry.getKey());
                }

                if (jsonElement1 != null) {
                    jsonObject.add(s, jsonElement1);
                }
            }
        }
    }


    static boolean isCorrectJsonFormat(@NotNull JsonObject json) {
        if(!json.has("criteria") || !json.has("display")) {
            return false;
        }

        if (json.has("parent")) {
            return true;
        }
        else {
            return json.get("display").getAsJsonObject().has("background");
        }
    }


    public static JsonElement checkAndCorrectIdsIfNecessary(JsonElement jsonElement) {
        if (jsonElement == null || jsonElement.isJsonNull()) {
            return null;
        }

        if (jsonElement.isJsonPrimitive()) {
            String s = jsonElement.getAsString();
            String a = checkAndCorrectResourceLocation(s);

            if (!s.equals(a)) {
                return new JsonPrimitive(s);
            }
        }

        return null;
    }
}
