package de.thedead2.customadvancements.util.io;

import com.google.gson.JsonObject;
import java.io.IOException;
import java.util.function.Consumer;

import net.minecraft.resources.ResourceLocation;

import static de.thedead2.customadvancements.util.core.ModHelper.LOGGER;

public class LegacyConverter {

    /**
     * Checks and corrects the advancement JSON format of an old version of this mod if necessary.
     */
    public static void checkAndUpdateIfNecessary(ResourceLocation advancementId, JsonObject jsonObject) {
        boolean jsonChanged = false;

        for (ModVersion version : ModVersion.values()) {
            if (version.convert(jsonObject)) {
                jsonChanged = true;
                LOGGER.info("Detected legacy format ({}) for advancement '{}'. Converting...", version.name(), advancementId);
            }
        }

        if (jsonChanged) {
            try {
                String cleanPath = advancementId.getPath().replace(".json", "");
                ResourceLocation cleanId = ResourceLocation.tryBuild(advancementId.getNamespace(), cleanPath);

                if (cleanId != null) {
                    AdvancementHandler.writeAdvancementToFile(cleanId, jsonObject);
                    LOGGER.info("Successfully converted and saved advancement '{}' to the new JSON format!", cleanId);
                }
            }
            catch (IOException e) {
                LOGGER.error("Failed to save converted legacy advancement: {}", advancementId, e);
            }
        }
    }

    private enum ModVersion {
        V1(json -> {
            JsonObject display = json.getAsJsonObject("display");
            if (display == null || !display.has("background")) return;

            boolean hasLargeBg = display.has("largeBackground");
            boolean hasBgClip = display.has("shouldBgClip");
            boolean hasBgRatio = display.has("bgRatio");

            if (!hasLargeBg && !hasBgClip && !hasBgRatio) return;

            JsonObject backgroundObj = new JsonObject();

            if (hasLargeBg) {
                boolean isLarge = display.get("largeBackground").getAsBoolean();
                display.remove("largeBackground");
                if (isLarge) {
                    backgroundObj.addProperty("type", "IMAGE");
                }
            }

            if (hasBgClip) {
                boolean shouldClip = display.get("shouldBgClip").getAsBoolean();
                display.remove("shouldBgClip");
                if (shouldClip) {
                    backgroundObj.addProperty("object_fit", "COVER");
                }
            }

            display.remove("bgRatio");

            backgroundObj.add("location", display.get("background"));
            display.remove("background");

            display.add("background", backgroundObj);
        });

        private final Consumer<JsonObject> converter;

        ModVersion(Consumer<JsonObject> converter) {
            this.converter = converter;
        }

        public boolean convert(JsonObject jsonObject) {
            String before = jsonObject.toString();

            this.converter.accept(jsonObject);

            return !before.equals(jsonObject.toString());
        }
    }
}

