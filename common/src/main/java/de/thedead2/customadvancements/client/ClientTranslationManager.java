package de.thedead2.customadvancements.client;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;

import java.util.HashMap;
import java.util.Map;

public class ClientTranslationManager {
    private static final Map<String, JsonElement> LANG_FILES = new HashMap<>();
    private static final Map<String, String> CUSTOM_TRANSLATIONS = new HashMap<>();

    public static void setTranslations(Map<String, JsonElement> langFiles, Map<String, String> translations) {
        clear();

        LANG_FILES.putAll(langFiles);
        CUSTOM_TRANSLATIONS.putAll(translations);
    }

    public static void reloadLang() {
        processTranslationData(Map.copyOf(LANG_FILES));
    }

    public static void processTranslationData(Map<String, JsonElement> langFiles) {
        Map<String, String> mergedTranslations = new HashMap<>();

        if (langFiles.containsKey("en_us")) {
            parseJsonToMap(langFiles.get("en_us"), mergedTranslations);
        }

        String selectedLang = Minecraft.getInstance().getLanguageManager().getSelected().toLowerCase();

        if (!selectedLang.equals("en_us") && langFiles.containsKey(selectedLang)) {
            parseJsonToMap(langFiles.get(selectedLang), mergedTranslations);
        }

        setTranslations(langFiles, mergedTranslations);
    }

    private static void parseJsonToMap(JsonElement jsonElement, Map<String, String> outputMap) {
        if (jsonElement != null && jsonElement.isJsonObject()) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();

            jsonObject.entrySet().forEach(entry -> {
                if (entry.getValue().isJsonPrimitive()) {
                    outputMap.put(entry.getKey(), entry.getValue().getAsString());
                }
            });
        }
    }

    public static String get(String key) {
        return CUSTOM_TRANSLATIONS.get(key);
    }

    public static boolean contains(String key) {
        return CUSTOM_TRANSLATIONS.containsKey(key);
    }

    public static void clear() {
        CUSTOM_TRANSLATIONS.clear();
        LANG_FILES.clear();
    }
}
