package de.thedead2.customadvancements.util;

import java.io.File;

import static de.thedead2.customadvancements.util.ModHelper.MOD_ID;

public interface IFileHandler {

    void readFiles(File directory);

    static String getId(String filePath) {
        return filePath.substring(filePath.lastIndexOf(MOD_ID)).replaceFirst("/", ":");
    }

    static String getGameAdvancementsId(String filePath){
        String subString = filePath.replace(ModHelper.GAME_ADVANCEMENTS_PATH + "/", "");
        return subString.replaceFirst("/", ":");
    }
}