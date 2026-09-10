package de.thedead2.customadvancements.advancements;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import de.thedead2.customadvancements.network.SyncBackgroundDataPayload;
import de.thedead2.customadvancements.util.helper.ResourceLocationHelper;
import de.thedead2.customadvancements.util.ConfigManager;
import de.thedead2.mc_libs.io.FileHandler;
import de.thedead2.customadvancements.util.helper.JsonHelper;
import de.thedead2.customadvancements.util.io.LegacyConverter;
import de.thedead2.customadvancements.data.TextureHandler;
import de.thedead2.mc_libs.concurrent.PartialCompletableFuture;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

import java.io.File;
import java.io.FileReader;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import static de.thedead2.customadvancements.util.ModHelper.*;


public class CustomAdvancementManager {

    public static final Path CUSTOM_ADVANCEMENTS_PATH = DIR_PATH.resolve(MOD_ID);

    /**
     * Holds the ids of all advancements of the advancement manager before modifying them
     * */
    private final Set<ResourceLocation> advancementIds = ConcurrentHashMap.newKeySet();

    private PartialCompletableFuture<Map<ResourceLocation, CustomAdvancement>> customAdvancementsFuture = PartialCompletableFuture.completedFuture(new ConcurrentHashMap<>());

    private boolean safeMode = false;

    public void loadAdvancementFiles() {
        this.clear();

        this.customAdvancementsFuture = PartialCompletableFuture.supplyAsync(new ConcurrentHashMap<>(), PartialCompletableFuture.Utils.mapClone(), (map, throwable) -> {
            LOGGER.error("Failed to load all custom advancement files!", throwable);
            logLoadStatus(map.size(), "custom advancement");
            return map;
        }, customAdvancements -> {
            FileHandler.readDirectoryAndSubDirectories(DIR_PATH.toFile(), directory -> {
                if (shouldSkipDirectory(directory)) return;

                AtomicInteger counter = new AtomicInteger();
                LOGGER.debug("Starting to read files in {}", directory.getPath());

                Arrays.stream(Objects.requireNonNull(directory.listFiles((fileDirectory, fileName) -> fileName.endsWith(".json"))))
                        .forEach(file -> {
                            String fileName = file.getName();

                            try (FileReader reader = new FileReader(file)){
                                JsonObject jsonObject = GsonHelper.parse(reader);

                                if (file.toPath().toString().contains("recipes" + PATH_SEPARATOR) || JsonHelper.isCorrectJsonFormat(jsonObject)) {
                                    ResourceLocation id = ResourceLocationHelper.createIdFromFilepath(file.getPath());
                                    LegacyConverter.checkAndUpdateIfNecessary(id, jsonObject);

                                    CustomAdvancement customadvancement = new CustomAdvancement(id, jsonObject);
                                    customAdvancements.put(customadvancement.getId(), customadvancement);

                                    counter.getAndIncrement();
                                }
                                else {
                                    throw new JsonParseException(fileName + " does not match the required '.json' format!");
                                }
                            }
                            catch (Exception e) {
                                LOGGER.error("Error loading advancement: {}", fileName, e);
                            }
                        });

                LOGGER.debug("Found {} valid advancements in {}", counter.get(), directory.getPath());
            });

            logLoadStatus(customAdvancements.size(), "custom advancement");

            return customAdvancements;
        }, FileHandler.IO_POOL);

        this.customAdvancementsFuture.completeOnTimeout(10, TimeUnit.SECONDS);
    }

    private boolean shouldSkipDirectory(File directory) {
        return directory.getPath().equals(String.valueOf(DIR_PATH)) || directory.getPath().contains(String.valueOf(DATA_PATH)) || !isModLoaded(directory);
    }

    public Set<ResourceLocation> getAllAdvancementIds() {
        Set<ResourceLocation> temp = new HashSet<>(advancementIds);
        temp.addAll(this.customAdvancementsFuture.join().keySet());

        return temp;
    }

    private boolean isModLoaded(File directory) {
        String modId = directory.getPath().replace(String.valueOf(DIR_PATH), "");

        modId = modId.replaceAll(Matcher.quoteReplacement(String.valueOf(PATH_SEPARATOR)), "/");
        modId = modId.replaceFirst("/", "");
        modId = modId.contains("/") ? modId.substring(0, modId.indexOf('/')) : modId;

        if (modId.isEmpty() || !PLATFORM.isModLoaded(modId)) {
            LOGGER.warn("Found advancements of unknown mod {}! Skipping them...", directory.getName());

            return false;
        }

        return true;
    }



    public void modifyAdvancementData(Map<ResourceLocation, JsonElement> mapIn) {
        if (safeMode) {
            LOGGER.warn("Safe Mode is enabled! Skipping advancement load...");

            return;
        }


        long startTime = System.currentTimeMillis();
        final Map<ResourceLocation, JsonElement> advancements = new HashMap<>();

        try {
            int numRemoved = 0;
            int numAdded = 0;

            if (!ConfigManager.DISABLE_STANDARD_ADVANCEMENT_LOAD.get()) { // Don't remove existing advancements without replacement
                advancements.putAll(mapIn);
            }

            advancementIds.addAll(mapIn.keySet());

            if (loadNoAdvancements()) {
                LOGGER.info("Removing all advancements...");
                numRemoved = advancements.size();
                advancements.clear();
            }
            else {
                LOGGER.info("Starting to inject custom advancements...");
                Map<ResourceLocation, CustomAdvancement> customAdvancements = this.customAdvancementsFuture.join();

                for (Map.Entry<ResourceLocation, CustomAdvancement> entry : customAdvancements.entrySet()) {
                    ResourceLocation id = entry.getKey();

                    if (ResourceLocationHelper.containsInPath(id, "recipes/") && ConfigManager.NO_RECIPE_ADVANCEMENTS.get()) {
                        LOGGER.debug("Skipped recipe advancement: {}", id);
                        numRemoved++;
                    }
                    else {
                        id = ResourceLocationHelper.stripFileExtension(id, ".json");
                        advancements.put(id, entry.getValue().getJsonObject());

                        LOGGER.debug("Loaded {} into Advancement Manager!", id);
                        numAdded++;
                    }
                }

                if (ConfigManager.NO_RECIPE_ADVANCEMENTS.get()) {
                    LOGGER.info("Starting to remove recipe advancements...");
                    int temp = advancements.size();
                    advancements.keySet().removeIf(id -> {
                        boolean isRecipe = ResourceLocationHelper.containsInPath(id, "recipes/");

                        if(isRecipe) LOGGER.debug("Removed recipe advancement: {}", id);

                        return isRecipe;
                    });
                    numRemoved += temp - advancements.size();
                }

                Set<ResourceLocation> blacklistedAdvancements = getBlacklistedAdvancements();

                if (!blacklistedAdvancements.isEmpty()) {
                    int sizeBefore = advancements.size();

                    if (ConfigManager.BLACKLIST_IS_WHITELIST.get()) {
                        LOGGER.info("Starting to apply advancement whitelist...");

                        Set<ResourceLocation> allowedWithParents = collectWhitelistWithAllParents(advancements, blacklistedAdvancements);

                        advancements.keySet().removeIf(id -> {
                            boolean remove = !allowedWithParents.contains(id);
                            if (remove) {
                                LOGGER.debug("Removed non-whitelisted advancement: {}", id);
                            }
                            return remove;
                        });

                    } else {
                        Multimap<ResourceLocation, ResourceLocation> parentChildrenMap = getChildren(advancements);
                        blacklistedAdvancements.forEach(id -> {
                            advancements.remove(id);
                            LOGGER.debug("Removed blacklisted advancement: {}", id);
                            removeChildren(advancements, id, parentChildrenMap);
                        });
                    }
                    numRemoved += (sizeBefore - advancements.size());
                }

            }

            mapIn.clear();
            mapIn.putAll(advancements);

            LOGGER.info("Modifying Advancement data took {} ms. Added {} custom advancements, removed {} advancements.", System.currentTimeMillis() - startTime, numAdded, numRemoved);
        }
        catch (Throwable e) {
            CrashReport crashReport = new CrashReport("Error while modifying advancement data!", e);

            throw new ReportedException(crashReport);
        }
    }

    private Set<ResourceLocation> getBlacklistedAdvancements() {
        List<? extends String> ids = ConfigManager.ADVANCEMENT_BLACKLIST.get();
        Set<ResourceLocation> blacklistedAdvancements = new HashSet<>(ids.size());

        for (String id : ids) {
            if(id.contains("*")) {
                String modId = id.substring(0, id.indexOf(":"));
                blacklistedAdvancements.addAll(getAllAdvancementIds().stream().filter(advancementId -> advancementId.getNamespace().equals(modId)).map(resourceLocation -> ResourceLocationHelper.stripFileExtension(resourceLocation, ".json")).collect(Collectors.toSet()));
            }
            else
                blacklistedAdvancements.add(ResourceLocation.tryParse(id));
        }

        return blacklistedAdvancements;
    }

    private boolean loadNoAdvancements() {
        return ConfigManager.ADVANCEMENT_BLACKLIST.get().isEmpty() && ConfigManager.BLACKLIST_IS_WHITELIST.get();
    }


    private Multimap<ResourceLocation, ResourceLocation> getChildren(Map<ResourceLocation, JsonElement> mapIn) {
        Multimap<ResourceLocation, ResourceLocation> children = ArrayListMultimap.create();

        mapIn.forEach((id, jsonElement) -> {
            ResourceLocation parent;

            JsonElement parentField = jsonElement.getAsJsonObject().get("parent");
            parent = parentField != null ? ResourceLocation.tryParse(parentField.getAsString()) : null;

            if (parent != null) {
                children.put(parent, id);
            }
        });

        return children;
    }


    private void removeChildren(Map<ResourceLocation, JsonElement> mapIn, ResourceLocation resourceLocationIn, Multimap<ResourceLocation, ResourceLocation> children) {
        for (ResourceLocation child : children.get(resourceLocationIn)) {
            mapIn.remove(child);
            LOGGER.debug("Removed child advancement {} from parent {}", child, resourceLocationIn);
            removeChildren(mapIn, child, children);
        }
    }


    private Set<ResourceLocation> collectWhitelistWithAllParents(Map<ResourceLocation, JsonElement> advancements, Set<ResourceLocation> whitelist) {
        Set<ResourceLocation> allowedIds = new HashSet<>();

        for (ResourceLocation whitelistedId : whitelist) {
            ResourceLocation currentId = whitelistedId;

            while (currentId != null && advancements.containsKey(currentId)) {
                if (!allowedIds.add(currentId)) {
                    break;
                }

                JsonElement jsonElement = advancements.get(currentId);
                JsonElement parentField = jsonElement.getAsJsonObject().get("parent");

                currentId = parentField != null ? ResourceLocation.tryParse(parentField.getAsString()) : null;
            }
        }

        return allowedIds;
    }



    public void clear() {
        advancementIds.clear();
        this.customAdvancementsFuture = PartialCompletableFuture.completedFuture(new ConcurrentHashMap<>());
    }


    public void setSaveMode(boolean safeMode) {
        this.safeMode = safeMode;
    }

    public boolean isCustomAdvancement(ResourceLocation resourceLocation) {
        return this.customAdvancementsFuture.join().containsKey(resourceLocation);
    }

    public void sendBackgroundDataToClient(Consumer<CustomPacketPayload> sender, TextureHandler textureHandler) {
        this.customAdvancementsFuture.thenAccept(map -> {
            Map<ResourceLocation, JsonElement> backgroundInfos = new HashMap<>();
            Map<ResourceLocation, int[]> imageDimensions = textureHandler.getImageDimensions();
            map.entrySet().stream()
                    .filter(entry -> entry.getValue().getBackgroundInfo() != null)
                    .forEach(entry -> {
                        CustomAdvancement advancement = entry.getValue();
                        JsonElement backgroundInfo = advancement.getBackgroundInfo();

                        if(backgroundInfo.isJsonObject()) {
                            JsonObject jsonObject = backgroundInfo.getAsJsonObject();

                            if (jsonObject.has("location")) {
                                ResourceLocation textureId = ResourceLocation.tryParse(jsonObject.get("location").getAsString());

                                int[] dimensions = imageDimensions.get(textureId);

                                if (dimensions != null && dimensions.length == 2) {
                                    jsonObject.addProperty("imageWidth", dimensions[0]);
                                    jsonObject.addProperty("imageHeight", dimensions[1]);
                                }
                            }
                        }

                        backgroundInfos.put(advancement.getId(), backgroundInfo);
                    });

            sender.accept(new SyncBackgroundDataPayload(backgroundInfos));
        });
    }


    public boolean isTextureUsed(ResourceLocation textureId) {
        return this.customAdvancementsFuture.join().entrySet().stream().anyMatch((entry) -> {
            JsonElement background = entry.getValue().getBackgroundInfo();
            return background != null && ((background.isJsonObject() && background.getAsJsonObject().has("location") && background.getAsJsonObject().get("location").getAsString().equals(textureId.toString())) || (background.isJsonPrimitive() && background.getAsString().equals(textureId.toString())));
        });
    }
}