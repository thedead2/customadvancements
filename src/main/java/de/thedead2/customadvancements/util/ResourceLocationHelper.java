package de.thedead2.customadvancements.util;

import net.minecraft.resources.ResourceLocation;

import java.util.regex.Matcher;

import static de.thedead2.customadvancements.util.core.ModHelper.DIR_PATH;
import static de.thedead2.customadvancements.util.core.ModHelper.PATH_SEPARATOR;


public class ResourceLocationHelper {

    public static ResourceLocation createIdFromPath(String filePath) {
        return new ResourceLocation(filePath.replace(String.valueOf(DIR_PATH), "")
                                            .replaceAll(Matcher.quoteReplacement(String.valueOf(PATH_SEPARATOR)), "/")
                                            .replaceFirst("/", "")
                                            .replaceFirst("/", ":")
        );
    }


    public static ResourceLocation stripFileExtension(ResourceLocation id, String fileExtension) {
        return new ResourceLocation(id.getNamespace(), id.getPath().replace(fileExtension, ""));
    }


    public static boolean containsInPath(ResourceLocation id, String path) {
        return id.getPath().contains(path);
    }
}
