package de.thedead2.customadvancements.datagen.dataprovider;

import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.LanguageProvider;

import java.util.HashMap;
import java.util.Map;

import static de.thedead2.customadvancements.util.language.TranslationKeyProvider.chatTranslationKeyFor;

public class ModLanguageProvider extends LanguageProvider {

    private final String lang;
    private final Map<String, String> keyMap = new HashMap<>();

    public ModLanguageProvider(DataGenerator generator, String modId, String locale) {
        super(generator, modId, locale);
        this.lang = locale;
    }

    @Override
    protected void addTranslations() {
        this.gatherKeyMapData();
        keyMap.forEach(this::add);
    }

    private void gatherKeyMapData(){
            if (lang.equals("en_us")) {
                keyMap.put(chatTranslationKeyFor("reload_failed_message"), "%s Failed to execute reload!");
                keyMap.put(chatTranslationKeyFor("mod_outdated_message"), "%s Mod is outdated! Please update using the link below:");
                keyMap.put(chatTranslationKeyFor("beta_warn_message"), "%s You're currently using a Beta Version of the mod! Please note that using this beta is at your own risk!");
                keyMap.put(chatTranslationKeyFor("beta_outdated_message"), "%s This Beta Version is outdated! Please update using the link below:");
                keyMap.put(chatTranslationKeyFor("reload_started"), "%s Reloading...");
                keyMap.put(chatTranslationKeyFor("reload_successful"), "%s Reload complete!");
                keyMap.put(chatTranslationKeyFor("generating_rl_file"), "%s Starting to write resource locations to file...");
                keyMap.put(chatTranslationKeyFor("generating_rl_file_success"), "%s Finished!");
                keyMap.put(chatTranslationKeyFor("generating_rl_file_failed"), "%s Unable to write resource locations to file!");
                keyMap.put(chatTranslationKeyFor("generating_game_advancements"), "%s Starting to generate files for game advancements...");
                keyMap.put(chatTranslationKeyFor("generating_game_advancements_failed"), "%s Unable to write %s to file!");
                keyMap.put(chatTranslationKeyFor("generating_game_advancements_success"), "%s Generated %s files for game advancements successfully!");
                keyMap.put(chatTranslationKeyFor("generating_game_advancement_failed"), "%s Unable to generate file for: %s");
                keyMap.put(chatTranslationKeyFor("generating_game_advancement_already_exists"), "%s The file for %s already exist!");
                keyMap.put(chatTranslationKeyFor("generating_game_advancement_success"), "%s Successfully generated file for: %s");
                keyMap.put(chatTranslationKeyFor("advancements_reset"), "%2$s, your advancements have been reset!");
            }
    }
}
