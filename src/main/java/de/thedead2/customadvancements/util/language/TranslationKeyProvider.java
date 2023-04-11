package de.thedead2.customadvancements.util.language;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;

import static de.thedead2.customadvancements.util.ModHelper.*;

public abstract class TranslationKeyProvider {

    public static String chatTranslationKeyFor(String name){
        return translationKeyFor(TranslationKeyType.CHAT, name);
    }

    public static String advancementTranslationKeyFor(String advancementName, TranslationKeyType.AdvancementKeySubType type){
        return translationKeyFor(TranslationKeyType.ADVANCEMENT, type, advancementName);
    }

    public static String translationKeyFor(TranslationKeyType type, String name){
        return translationKeyFor(type, null, name);
    }

    public static String translationKeyFor(TranslationKeyType type, TranslationKeyType.TranslationKeySubType subType, String name){
        String key = MOD_ID + "." + type + "." + (subType != null ? subType + "." : "") + name;
        return key.toLowerCase();
    }

    public static Component chatMessage(String translationKeyName, ChatFormatting color, String... additionalArgs){
        return Component.translatable(chatTranslationKeyFor(translationKeyName), "[" + MOD_NAME + "]: ", additionalArgs).withStyle(color);
    }

    public static Component chatMessage(String translationKeyName, String... additionalArgs){
        return chatMessage(translationKeyName, ChatFormatting.WHITE, additionalArgs);
    }

    public static Component chatLink(String link, ChatFormatting color){
        return Component.literal(link).withStyle(ChatFormatting.UNDERLINE, color).withStyle(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, link)));
    }
}
