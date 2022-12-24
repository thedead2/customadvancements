package de.thedead2.customadvancements.util;

import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.loading.moddiscovery.ModFile;

public abstract class ModHelper {

    public static final String MOD_VERSION = "1.16.5-1.0.1.0";
    public static final String MOD_ID = "customadvancements";
    public static final String MOD_NAME = "Custom Advancements";
    public static final ModFile THIS_MOD_FILE = ModList.get().getModFileById(MOD_ID).getFile();

    public static final String GAME_DIR = FMLPaths.GAMEDIR.get().toString();
    public static final String DIR_PATH = GAME_DIR + "/" + MOD_ID;
    public static final String TEXTURES_PATH = DIR_PATH + "/" + "textures";

    public static final FileHandler FILE_HANDLER = new FileHandler();
    public static final JsonHandler JSON_HANDLER = new JsonHandler();
}
