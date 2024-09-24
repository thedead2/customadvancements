package de.thedead2.customadvancements.util;

import de.thedead2.customadvancements.util.core.CrashHandler;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.Level;

import java.util.regex.Matcher;

import static de.thedead2.customadvancements.util.core.ModHelper.DIR_PATH;
import static de.thedead2.customadvancements.util.core.ModHelper.PATH_SEPARATOR;



public class ResourceLocationHelper {

    public static ResourceLocation createIdFromPath(String filePath) {
        return createIdFromPath(filePath, false);
    }


    public static ResourceLocation createIdFromPath(String filePath, boolean onlyWrap) { //TODO: onlyWrap for what??
        try {
            String subString = filePath.replace(String.valueOf(DIR_PATH), "");

            if (!onlyWrap) {
                subString = subString.replaceAll(Matcher.quoteReplacement(String.valueOf(PATH_SEPARATOR)), "/");
                subString = subString.replaceFirst("/", "");
                subString = subString.replaceFirst("/", ":");
            }

            return new ResourceLocation(subString);
        }
        catch (Throwable throwable) {
            CrashHandler.getInstance().handleException("Unable to create ID!", throwable, Level.ERROR);

            throw throwable;
        }
    }


    public static ResourceLocation stripFileExtension(ResourceLocation id, String fileExtension) {
        return new ResourceLocation(id.getNamespace(), id.getPath().replace(fileExtension, ""));
    }


    public static boolean containsPath(ResourceLocation id, String path) {
        return id.getPath().contains(path);
    }

}
