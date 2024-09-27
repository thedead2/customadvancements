package de.thedead2.customadvancements.util.io;


import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import de.thedead2.customadvancements.advancements.CustomAdvancement;
import de.thedead2.customadvancements.util.core.CrashHandler;
import joptsimple.internal.Strings;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.fml.ModList;
import org.apache.logging.log4j.Level;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;

import static de.thedead2.customadvancements.util.core.ModHelper.*;


public class JsonHandler {

    public static void loadAdvancementFiles() {
        FileHandler.readDirectoryAndSubDirectories(DIR_PATH.toFile(), directory -> {
            if (directory.getPath().equals(String.valueOf(DIR_PATH)) || directory.getPath().contains(String.valueOf(DATA_PATH)) || !isModLoaded(directory)) {
                return;
            }

            int counter = 0;

            LOGGER.debug("Starting to read files in {}", directory.getPath());

            File[] fileList = directory.listFiles((fileDirectory, fileName) -> fileName.endsWith(".json"));

            for (File file : Objects.requireNonNull(fileList)) {
                long startTime = System.currentTimeMillis();
                String fileName = file.getName();

                try {
                    JsonObject jsonObject = GsonHelper.parse(new FileReader(file));

                    if (SecureAdvancementHandler.checkAndUpdateAdvancementFormat(jsonObject, file)) {
                        CustomAdvancement customadvancement = new CustomAdvancement(jsonObject, fileName, file.getPath());
                        CUSTOM_ADVANCEMENTS.put(customadvancement.getResourceLocation(), customadvancement);

                        counter++;
                    }
                    else {
                        LOGGER.error("{} does not match the required '.json' format!", fileName);
                        WARNINGS.offer(fileName + " wasn't loaded due to invalid json format!");
                    }
                }
                catch (JsonParseException e) {
                    CrashHandler.getInstance().handleException("Error parsing advancement " + fileName + "! Make sure you have the right syntax for '.json' files!", e, Level.ERROR);
                    WARNINGS.offer("Error parsing advancement " + fileName + "! Make sure you have the right syntax for '.json' files!\nPlease check the log for detailed information.");
                }
                catch (IOException e) {
                    CrashHandler.getInstance().handleException("Failed to check and update advancement with name " + fileName, e, Level.WARN);
                    WARNINGS.offer(fileName + " couldn't be updated to new json format for advancements!\nPlease check the log for detailed information.");
                }

                long elapsedTime = System.currentTimeMillis() - startTime;

                if (elapsedTime >= 500) {
                    LOGGER.warn("Reading file {} took {} ms! Max. should be 500 ms!", fileName, elapsedTime);
                }
            }

            LOGGER.debug("Found {} valid advancements in {}", counter, directory.getPath());
        });
    }


    private static boolean isModLoaded(File directory) {
        String modId = directory.getPath().replace(String.valueOf(DIR_PATH), "");

        modId = modId.replaceAll(Matcher.quoteReplacement(String.valueOf(PATH_SEPARATOR)), "/");
        modId = modId.replaceFirst("/", "");
        modId = modId.contains("/") ? modId.substring(0, modId.indexOf('/')) : modId;

        if (modId.isEmpty() || !ModList.get().isLoaded(modId)) {
            //TODO: Add path to list of unknown mod advancements and ask user if they should be deleted or kept
            LOGGER.warn("Found advancements of unknown mod {}! Skipping them...", directory.getName());
            WARNINGS.offer("Found advancements of unknown mod " + modId + "! Please delete the directory or transfer the advancements to:\n" + CUSTOM_ADVANCEMENTS_PATH);

            return false;
        }

        return true;
    }


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
}
