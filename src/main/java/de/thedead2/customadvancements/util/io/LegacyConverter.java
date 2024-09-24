package de.thedead2.customadvancements.util.io;

import com.google.gson.JsonObject;
import de.thedead2.customadvancements.util.core.ModHelper;
import net.minecraft.resources.ResourceLocation;

import java.io.IOException;


//TODO: Unite @LegacyConverter with @SecureAdvancementHandler
public class LegacyConverter {

    private static final VersionChecker[] VERSION_CHECKERS = new VersionChecker[] {LegacyConverter::checkForV1};


    public static void checkAndUpdate(ResourceLocation advancementId, JsonObject jsonObject) throws IOException {
        boolean jsonChanged = false;

        for (VersionChecker versionChecker : VERSION_CHECKERS) {
            if (versionChecker.check(jsonObject)) {
                jsonChanged = true;

                break;
            }
        }

        if (jsonChanged) {
            ResourceLocation resourceLocation1 = new ResourceLocation(advancementId.getNamespace(), advancementId.getPath().replace(".json", ""));

            AdvancementHandler.writeAdvancementToFile(resourceLocation1, jsonObject);

            ModHelper.LOGGER.info("Converted advancement with id {} to new json format!", advancementId);
        }
    }


    private static boolean checkForV1(JsonObject jsonObject) {
        JsonObject display = jsonObject.getAsJsonObject("display");

        if(display == null || !display.has("background")) {
            return false;
        }

        boolean jsonChanged = false;
        JsonObject background = new JsonObject();

        if (getAsBoolean(display, "largeBackground")) {
            display.remove("largeBackground");
            background.addProperty("type", "IMAGE");

            background.add("location", display.get("background"));
            display.remove("background");
            display.add("background", background);

            jsonChanged = true;
        }

        if (getAsBoolean(display, "shouldBgClip")) {
            display.remove("shouldBgClip");
            background.addProperty("object_fit", "COVER");

            jsonChanged = true;
        }

        if (display.has("bgRatio")) {
            display.remove("bgRatio");

            jsonChanged = true;
        }

        return jsonChanged;
    }


    private static boolean getAsBoolean(JsonObject jsonObject, String member) {
        if (jsonObject.has(member)) {
            return jsonObject.get(member).getAsBoolean();
        }
        else {
            return false;
        }
    }


    @FunctionalInterface
    private interface VersionChecker {

        boolean check(JsonObject jsonObject);
    }
}
