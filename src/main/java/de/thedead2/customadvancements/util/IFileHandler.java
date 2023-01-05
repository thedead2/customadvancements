package de.thedead2.customadvancements.util;

import java.io.File;

import static de.thedead2.customadvancements.util.ModHelper.DIR_PATH;

public interface IFileHandler {

    void readFiles(File directory);

    static String getId(String filePath){
        String subString = filePath.replace(DIR_PATH + "/", "");
        return subString.replaceFirst("/", ":");
    }
}
