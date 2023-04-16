package de.thedead2.customadvancements.util;

import de.thedead2.customadvancements.util.handler.FileHandler;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;

import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public abstract class ResourceManagerExtender {

    private static final Map<ResourceLocation, File> RESOURCES = new HashMap<>();
    private static final String FAKE_PACK_ID = "CA_Extended";

    public static Optional<Resource> handleResourceRequest(ResourceLocation resourceLocation){
        if(isValidResourceLocation(resourceLocation)){
            return RESOURCES.containsKey(resourceLocation) ? Optional.of(new Resource(FAKE_PACK_ID, () -> Files.newInputStream(RESOURCES.get(resourceLocation).toPath()))) : Optional.empty();
        }
        return Optional.empty();
    }

    public static void addResource(ResourceLocation resourceLocation, File file){
        if(resourceLocation == null)
            resourceLocation = FileHandler.getId(file.getPath());
        RESOURCES.put(resourceLocation, file);
    }

    private static boolean isValidResourceLocation (ResourceLocation resourceLocation) {
        return resourceLocation.getNamespace().equals(ModHelper.MOD_ID);
    }

    public static File getResource(ResourceLocation resourceLocation) {
        return RESOURCES.get(resourceLocation);
    }

    public static int getResourcesCount(){
        return RESOURCES.size();
    }

    public static void clear(){
        RESOURCES.clear();
    }
}