package de.thedead2.customadvancements.util;

import de.thedead2.customadvancements.util.core.CrashHandler;
import de.thedead2.customadvancements.util.core.FileHandler;
import de.thedead2.customadvancements.util.core.ModHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.SimpleResource;
import org.apache.logging.log4j.Level;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public abstract class ResourceManagerExtender {

    private static final Map<ResourceLocation, File> RESOURCES = new HashMap<>();
    private static final String FAKE_PACK_ID = "CA_Extended";

    public static Optional<Resource> handleResourceRequest(ResourceLocation resourceLocation){
        if(isValidResourceLocation(resourceLocation)){
            try {
                return RESOURCES.containsKey(resourceLocation) ? Optional.of(new SimpleResource(FAKE_PACK_ID, resourceLocation, Files.newInputStream(RESOURCES.get(resourceLocation).toPath()), null)) : Optional.empty();
            } catch (IOException e) {
                CrashHandler.getInstance().addCrashDetails("Unable to create inputstream from file!", Level.ERROR, e);
                return Optional.empty();
            }
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
