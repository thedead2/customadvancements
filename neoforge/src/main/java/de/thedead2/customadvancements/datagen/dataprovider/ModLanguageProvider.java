package de.thedead2.customadvancements.datagen.dataprovider;

import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

import static de.thedead2.customadvancements.util.localisation.ModTranslationKeys.*;


public class ModLanguageProvider extends LanguageProvider {

    private final String lang;

    public ModLanguageProvider(PackOutput output, String modId, String locale) {
        super(output, modId, locale);
        this.lang = locale;
    }


    @Override
    protected void addTranslations() {
        if (this.lang.equals("en_us")) {
            this.add(RELOAD_FAILED_MESSAGE, "%s Failed to execute reload!");
            this.add(RELOAD_START_MESSAGE, "%s Reloading...");
            this.add(RELOAD_SUCCESS_MESSAGE, "%s Reload complete!");
            this.add(GENERATE_IDS_MESSAGE, "%s Starting to write ids to file...");
            this.add(GENERATE_IDS_SUCCESS_MESSAGE, "%s Finished!");
            this.add(GENERATE_IDS_FAILED_MESSAGE, "%s Unable to write ids to file!");
            this.add(GENERATE_ADVANCEMENTS_MESSAGE, "%s Starting to generate files for advancements...");
            this.add(GENERATE_ADVANCEMENTS_FAILED_MESSAGE, "%s Unable to write %s to file!");
            this.add(GENERATE_ADVANCEMENTS_SUCCESS_MESSAGE, "%s Generated %s advancement files successfully!");
            this.add(GENERATE_ADVANCEMENT_FILE_FAILED_MESSAGE, "%s Unable to generate file for: %s");
            this.add(GENERATE_ADVANCEMENT_FILE_ALREADY_EXISTS_MESSAGE, "%s The file for %s already exist!");
            this.add(GENERATE_ADVANCEMENT_FILE_SUCCESS_MESSAGE, "%s Successfully generated file for: %s");
            this.add(ADVANCEMENTS_RESET_MESSAGE, "%2$s, your advancements have been reset!");
        }
    }
}
