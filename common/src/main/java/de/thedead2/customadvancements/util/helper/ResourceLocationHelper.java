package de.thedead2.customadvancements.util.helper;

import net.minecraft.resources.ResourceLocation;

import java.util.regex.Matcher;

import static de.thedead2.customadvancements.util.core.ModHelper.DIR_PATH;
import static de.thedead2.customadvancements.util.core.ModHelper.PATH_SEPARATOR;


public class ResourceLocationHelper {

    public static ResourceLocation createIdFromFilepath(String filePath) {
        return ResourceLocation.tryParse(filePath.replace(String.valueOf(DIR_PATH), "")
                                            .replaceAll(Matcher.quoteReplacement(String.valueOf(PATH_SEPARATOR)), "/")
                                            .replaceFirst("/", "")
                                            .replaceFirst("/", ":")
        );
    }

    public static ResourceLocation stripFileExtension(ResourceLocation id, String fileExtension) {
        return ResourceLocation.tryBuild(id.getNamespace(), id.getPath().replace(fileExtension, ""));
    }

    public static ResourceLocation addFileExtension(ResourceLocation id, String fileExtension) {
        return ResourceLocation.tryBuild(id.getNamespace(), id.getPath() + fileExtension);
    }

    public static boolean containsInPath(ResourceLocation id, String path) {
        return id.getPath().contains(path);
    }

}
