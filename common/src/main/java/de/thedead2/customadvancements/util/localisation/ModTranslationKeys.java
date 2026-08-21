package de.thedead2.customadvancements.util.localisation;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import static de.thedead2.customadvancements.util.core.ModHelper.MOD_ID;
import static de.thedead2.customadvancements.util.core.ModHelper.MOD_NAME;


public class ModTranslationKeys {
    
    public static final String RELOAD_FAILED_MESSAGE = chatTranslationKeyFor("reload_failed");
    public static final String RELOAD_SUCCESS_MESSAGE = chatTranslationKeyFor("reload_success");
    public static final String RELOAD_START_MESSAGE = chatTranslationKeyFor("reload_start");
    public static final String GENERATE_IDS_MESSAGE = chatTranslationKeyFor("generate_ids");
    public static final String GENERATE_IDS_SUCCESS_MESSAGE = chatTranslationKeyFor("generate_ids_success");
    public static final String GENERATE_IDS_FAILED_MESSAGE = chatTranslationKeyFor("generate_ids_failed");
    public static final String GENERATE_ADVANCEMENTS_MESSAGE = chatTranslationKeyFor("generate_advancements");
    public static final String GENERATE_ADVANCEMENTS_SUCCESS_MESSAGE = chatTranslationKeyFor("generate_advancements_success");
    public static final String GENERATE_ADVANCEMENTS_FAILED_MESSAGE = chatTranslationKeyFor("generate_advancements_failed");
    public static final String GENERATE_ADVANCEMENT_FILE_FAILED_MESSAGE = chatTranslationKeyFor("generate_advancement_file_failed");
    public static final String GENERATE_ADVANCEMENT_FILE_SUCCESS_MESSAGE = chatTranslationKeyFor("generate_advancement_file_success");
    public static final String GENERATE_ADVANCEMENT_FILE_ALREADY_EXISTS_MESSAGE = chatTranslationKeyFor("generate_advancement_file_already_exists");
    public static final String ADVANCEMENTS_RESET_MESSAGE = chatTranslationKeyFor("advancements_reset");


    public static Component chatMessage(String translationKeyName, Object... additionalArgs) {
        return chatMessage(translationKeyName, ChatFormatting.WHITE, additionalArgs);
    }


    public static Component chatMessage(String translationKey, ChatFormatting color, Object... additionalArgs) {
        Object[] additionalArgs2 = new Object[additionalArgs.length + 1];
        System.arraycopy(additionalArgs, 0, additionalArgs2, 1, additionalArgs.length);
        additionalArgs2[0] = "[" + MOD_NAME + "]: ";

        return Component.translatable(translationKey, additionalArgs2).withStyle(color);
    }

    private static String chatTranslationKeyFor(String name) {
        return translationKeyFor("chat", name);
    }

    private static String translationKeyFor(String type, String name) {
        String key = MOD_ID + "." + type + "." + name;
        return key.toLowerCase();
    }
}
