package de.thedead2.customadvancements.util.core;

import de.thedead2.customadvancements.util.language.TranslationKeyType;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

import static de.thedead2.customadvancements.util.core.ModHelper.*;

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

    public static Component chatMessage(String translationKeyName, ChatFormatting color, Object... additionalArgs){
        Object[] additionalArgs2 = new Object[additionalArgs.length + 1];
        System.arraycopy(additionalArgs, 0, additionalArgs2, 1, additionalArgs.length);
        additionalArgs2[0] = "[" + MOD_NAME + "]: ";
        return newTranslatableComponent(translationKeyName, color, additionalArgs2);
    }

    public static Component newTranslatableComponent(String translationKey, ChatFormatting color, Object... additionalArgs){
        return new TranslatableComponent(chatTranslationKeyFor(translationKey), additionalArgs).withStyle(color);
    }

    public static Component chatMessage(String translationKeyName, Object... additionalArgs){
        return chatMessage(translationKeyName, ChatFormatting.WHITE, additionalArgs);
    }

    public static Component chatLink(String link, ChatFormatting color){
        return new TextComponent(link).withStyle(ChatFormatting.UNDERLINE, color).withStyle(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, link)));
    }
}
