package de.thedead2.customadvancements.advancements;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.google.gson.JsonElement;
import de.thedead2.customadvancements.util.ResourceLocationHelper;
import de.thedead2.customadvancements.util.core.ConfigManager;
import de.thedead2.customadvancements.util.core.CrashHandler;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static de.thedead2.customadvancements.util.core.ModHelper.CUSTOM_ADVANCEMENTS;
import static de.thedead2.customadvancements.util.core.ModHelper.LOGGER;


public class CustomAdvancementManager {

    public static final Set<ResourceLocation> ADVANCEMENT_IDS = new HashSet<>();

    private static final Map<ResourceLocation, JsonElement> ADVANCEMENTS = new HashMap<>();

    private static boolean SAFE_MODE = false;


    public static void modifyAdvancementData(Map<ResourceLocation, JsonElement> mapIn) {
        if (SAFE_MODE) {
            LOGGER.warn("Safe Mode is enabled! Skipping advancement load...");

            return;
        }


        long startTime = System.currentTimeMillis();

        try {
            AtomicInteger numAdvancementsRemoved = new AtomicInteger();
            AtomicInteger numAdvancementsLoaded = new AtomicInteger();

            modification:
            {
                if (!ConfigManager.DISABLE_STANDARD_ADVANCEMENT_LOAD.get()) {
                    ADVANCEMENTS.putAll(mapIn);
                }
                ADVANCEMENT_IDS.addAll(mapIn.keySet());

                if (removeAllAdvancementsIfNeeded(numAdvancementsRemoved)) {
                    break modification;
                }

                loadCustomAdvancements(numAdvancementsLoaded, numAdvancementsRemoved);
                removeRecipeAdvancementsIfNeeded(numAdvancementsRemoved);
                removeListedAdvancementsIfNeeded(numAdvancementsRemoved);
            }

            mapIn.clear();
            mapIn.putAll(ADVANCEMENTS);

            LOGGER.info("Modifying Advancement data took {} ms. Added {} custom advancements, removed {} advancements.", System.currentTimeMillis() - startTime, numAdvancementsLoaded.get(), numAdvancementsRemoved.get());
        }
        catch (Throwable e) {
            CrashReport crashReport = new CrashReport("Error while modifying advancement data!", e);
            CrashHandler.getInstance().printCrashReport(crashReport);

            throw new ReportedException(crashReport);
        }
    }


    private static void loadCustomAdvancements(AtomicInteger numAdvancementsAdded, AtomicInteger numAdvancementsRemoved) {
        if (CUSTOM_ADVANCEMENTS.isEmpty() || ConfigManager.NO_ADVANCEMENTS.get()) {
            return;
        }

        LOGGER.info("Starting to load custom advancements");

        if (ConfigManager.DISABLE_STANDARD_ADVANCEMENT_LOAD.get()) {
            ignoreMissingAdvancements(numAdvancementsRemoved);
        }

        for (Map.Entry<ResourceLocation, CustomAdvancement> entry : CUSTOM_ADVANCEMENTS.entrySet()) {
            ResourceLocation id = entry.getKey();

            if (ResourceLocationHelper.containsPath(id, "recipes/") && ConfigManager.NO_RECIPE_ADVANCEMENTS.get()) {
                LOGGER.debug("Skipped recipe advancement: {}", id);
                numAdvancementsRemoved.getAndIncrement();
                continue;
            }

            if (ADVANCEMENT_IDS.contains(id)) { //TODO: Still useful?
                LOGGER.error("Duplicate id '{}' for advancement: {}", id, entry.getValue().getFileName());
                continue;
            }

            CustomAdvancement advancement = entry.getValue();
            id = ResourceLocationHelper.stripFileExtension(id, ".json");

            ADVANCEMENTS.put(id, advancement.getJsonObject());

            LOGGER.debug("Loaded {} into Advancement Manager!", id);
            numAdvancementsAdded.getAndIncrement();
        }
    }


    private static void removeRecipeAdvancementsIfNeeded(AtomicInteger counter) {
        if (!ConfigManager.NO_RECIPE_ADVANCEMENTS.get()) {
            return;
        }

        LOGGER.info("Starting to remove recipe advancements...");

        ADVANCEMENT_IDS.stream().filter(id -> ResourceLocationHelper.containsPath(id, "recipes/")).forEach(id -> removeAdvancement(ADVANCEMENTS, id, counter));
    }


    private static void removeBlacklistedAdvancements(ImmutableSet<ResourceLocation> blacklistedAdvancements, AtomicInteger counter) {
        LOGGER.info("Starting to remove blacklisted advancements...");

        Multimap<ResourceLocation, ResourceLocation> children = getChildren(ADVANCEMENTS);

        for (ResourceLocation blacklistedAdvancement : blacklistedAdvancements) {
            removeAdvancement(ADVANCEMENTS, blacklistedAdvancement, counter);

            removeChildren(ADVANCEMENTS, blacklistedAdvancement, children, counter);
        }
    }


    private static void removeNoneWhitelistedAdvancements(ImmutableSet<ResourceLocation> blacklistedAdvancements, AtomicInteger counter) {
        LOGGER.info("Starting to remove none whitelisted advancements...");

        Map<ResourceLocation, ResourceLocation> parents = getParents();
        Set<ResourceLocation> mapKeySet = new HashSet<>(ADVANCEMENTS.keySet());

        for (ResourceLocation advancement : mapKeySet) {
            if (!blacklistedAdvancements.contains(advancement) && !parents.containsValue(advancement)) {
                removeAdvancement(ADVANCEMENTS, advancement, counter);
            }
        }
    }


    private static void removeAdvancement(Map<ResourceLocation, ?> mapIn, ResourceLocation advancement, AtomicInteger counter) {
        mapIn.remove(advancement);
        CrashHandler.getInstance().addRemovedAdvancement(advancement);
        counter.getAndIncrement();

        LOGGER.debug("Removed advancement: {}", advancement);
    }


    private static void removeListedAdvancementsIfNeeded(AtomicInteger counter) {
        ImmutableSet<ResourceLocation> blacklistedAdvancements = ConfigManager.getBlacklistedResourceLocations();

        if (!blacklistedAdvancements.isEmpty()) {
            if (ConfigManager.BLACKLIST_IS_WHITELIST.get()) {
                removeNoneWhitelistedAdvancements(blacklistedAdvancements, counter);
            }
            else {
                removeBlacklistedAdvancements(blacklistedAdvancements, counter);
            }
        }
        else if (ConfigManager.BLACKLIST_IS_WHITELIST.get()) {
            removeAllAdvancementsIfNeeded(counter);
        }
    }


    private static boolean removeAllAdvancementsIfNeeded(AtomicInteger counter) {
        if (ConfigManager.NO_ADVANCEMENTS.get() || ConfigManager.getBlacklistedResourceLocations().isEmpty() && ConfigManager.BLACKLIST_IS_WHITELIST.get()) {
            LOGGER.info("Starting to remove all advancements...");

            counter.set(ADVANCEMENTS.size());
            CrashHandler.getInstance().addRemovedAdvancements(ADVANCEMENTS.keySet());

            ADVANCEMENTS.clear();

            return true;
        }

        return false;
    }


    private static Multimap<ResourceLocation, ResourceLocation> getChildren(Map<ResourceLocation, ?> mapIn) {
        Multimap<ResourceLocation, ResourceLocation> children = ArrayListMultimap.create();

        for (ResourceLocation id : mapIn.keySet()) {
            ResourceLocation parent;
            Object obj = mapIn.get(id);

            if (obj instanceof JsonElement jsonElement) {
                JsonElement parentField = jsonElement.getAsJsonObject().get("parent");
                parent = parentField != null ? ResourceLocation.tryParse(parentField.getAsString()) : null;
            }
            else if (obj instanceof CustomAdvancement customAdvancement) {
                parent = customAdvancement.getParent();
            }
            else {
                throw new RuntimeException("Unexpected input: Map<ResourceLocation, " + mapIn.get(id).getClass().getName() + ">!");
            }

            if (parent != null) {
                children.put(parent, id);
            }
        }

        return children;
    }


    private static Map<ResourceLocation, ResourceLocation> getParents() {
        Map<ResourceLocation, ResourceLocation> parents = new HashMap<>();

        for (ResourceLocation resourceLocation : ADVANCEMENTS.keySet()) {
            getParent(parents, resourceLocation, true);
        }

        return parents;
    }


    private static void getParent(Map<ResourceLocation, ResourceLocation> parents, ResourceLocation id, boolean checkForBlacklist) {
        JsonElement parent = ADVANCEMENTS.get(id);

        if (parent == null) {
            LOGGER.warn("Unknown advancement in blacklist/ whitelist with id: {}", id);

            return;
        }

        parent = parent.getAsJsonObject().get("parent");

        if (ConfigManager.getBlacklistedResourceLocations().contains(id) || !checkForBlacklist) {
            ResourceLocation parentResourceLocation = ResourceLocation.tryParse(parent.getAsString());

            parents.put(id, parentResourceLocation);

            getParent(parents, parentResourceLocation, false);
        }
    }


    private static void removeChildren(Map<ResourceLocation, ?> mapIn, ResourceLocation resourceLocationIn, Multimap<ResourceLocation, ResourceLocation> children, AtomicInteger counter) {
        for (ResourceLocation childAdvancement : children.get(resourceLocationIn)) {
            if (mapIn.containsKey(childAdvancement)) {
                removeAdvancement(mapIn, childAdvancement, counter);
            }

            removeChildren(mapIn, childAdvancement, children, counter);
        }
    }


    private static void ignoreMissingAdvancements(AtomicInteger counter) {
        Set<ResourceLocation> missingAdvancements = getMissingAdvancements();

        if (missingAdvancements.isEmpty()) {
            return;
        }

        Multimap<ResourceLocation, ResourceLocation> children = getChildren(CUSTOM_ADVANCEMENTS);

        for (ResourceLocation advancement : missingAdvancements) {
            removeAdvancement(CUSTOM_ADVANCEMENTS, advancement, counter);

            removeChildren(CUSTOM_ADVANCEMENTS, advancement, children, counter);
        }
    }


    private static Set<ResourceLocation> getMissingAdvancements() {
        Set<ResourceLocation> parentIds = new HashSet<>();
        Set<ResourceLocation> missingAdvancements = new HashSet<>();

        CUSTOM_ADVANCEMENTS.forEach((resourceLocation, advancement) -> parentIds.add(advancement.getParent()));

        if (!CUSTOM_ADVANCEMENTS.keySet().containsAll(parentIds)) {
            CUSTOM_ADVANCEMENTS.forEach((id, advancement) -> {
                ResourceLocation parent = advancement.getParent();
                if (parent != null && !CUSTOM_ADVANCEMENTS.containsKey(parent)) {
                    missingAdvancements.add(id);
                }
            });
        }
        return missingAdvancements;
    }


    public static void clearAll() {
        ADVANCEMENTS.clear();
        ADVANCEMENT_IDS.clear();
    }


    public static void setSaveMode(boolean safeMode) {
        SAFE_MODE = safeMode;
    }
}