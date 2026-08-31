package de.thedead2.customadvancements.util.io;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import de.thedead2.customadvancements.util.helper.JsonHelper;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static de.thedead2.customadvancements.util.ModHelper.DIR_PATH;
import static de.thedead2.customadvancements.util.ModHelper.LOGGER;
import static de.thedead2.mc_libs.io.FileHandler.createDirectoryIfNecessary;
import static de.thedead2.mc_libs.io.FileHandler.writeFile;


public class AdvancementHandler {

    public static boolean grantingAllAdvancements = false; //Move


    public static void writeAdvancementToFile(ResourceLocation advancementId, JsonElement advancementData) throws IOException {
        LOGGER.debug("Generating file: {}", advancementId);

        List<String> folderNames = new ArrayList<>();
        Path basePath = Path.of(String.valueOf(DIR_PATH), advancementId.getNamespace());

        createDirectoryIfNecessary(basePath.toFile(), LOGGER);

        String formatedJsonObject = JsonHelper.formatJsonObject(advancementData);
        Path filePath = resolvePath(basePath, advancementId.getPath(), folderNames);

        writeFile(new ByteArrayInputStream(formatedJsonObject.getBytes()), filePath);
    }


    public static void writeAdvancementToFile(AdvancementHolder advancementIn, HolderLookup.Provider registryAccess) throws IOException {
        writeAdvancementToFile(advancementIn.id(), serializeToJson(advancementIn.value(), registryAccess));
    }


    public static JsonElement serializeToJson(Advancement advancementIn, HolderLookup.Provider registryAccess) {
        RegistryOps<JsonElement> ops = registryAccess.createSerializationContext(JsonOps.INSTANCE);

        var temp = Advancement.CODEC.encodeStart(ops, advancementIn).getOrThrow(s -> new NullPointerException("Can't serialize advancement " + advancementIn + "\n\n" + s));
        JsonObject jsonObject = temp.getAsJsonObject();
        JsonHelper.removeNullFields(jsonObject);

        return jsonObject;
    }


    private static void getSubDirectories(String pathIn, List<String> folderNames) {
        if (pathIn.contains("/")) {
            String temp1 = pathIn.substring(pathIn.indexOf("/"));
            String temp2 = pathIn.replace(temp1 + "/", "");
            String temp3 = temp2.replace(temp2.substring(temp2.indexOf("/")), "");

            folderNames.add(temp3);

            String next = pathIn.replace((temp3 + "/"), "");
            getSubDirectories(next, folderNames);
        }
    }


    private static Path resolvePath(Path basePath, String advancementPath, List<String> folderNames) {
        if (advancementPath.contains("/")) {
            String subStringDirectory = advancementPath.replaceAll(advancementPath.substring(advancementPath.indexOf("/")), "");
            folderNames.add(subStringDirectory);

            String nextSubString = advancementPath.replace(subStringDirectory + "/", "");
            getSubDirectories(nextSubString, folderNames);

            for (String folderName : folderNames) {
                basePath = Path.of(String.valueOf(basePath), folderName);

                createDirectoryIfNecessary(basePath.toFile(), LOGGER);
            }

            return Path.of(String.valueOf(basePath), advancementPath.substring(advancementPath.lastIndexOf("/")) + ".json");
        }
        else {
            return Path.of(String.valueOf(basePath), advancementPath + ".json");
        }
    }
}
