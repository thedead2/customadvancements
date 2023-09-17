package de.thedead2.customadvancements.util.core;

import de.thedead2.customadvancements.util.language.TranslationKeyType;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.event.ClickEvent;

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

    public static ITextComponent chatMessage(String translationKeyName, TextFormatting color, Object... additionalArgs){
        Object[] additionalArgs2 = new Object[additionalArgs.length + 1];
        System.arraycopy(additionalArgs, 0, additionalArgs2, 1, additionalArgs.length);
        additionalArgs2[0] = "[" + MOD_NAME + "]: ";
        return newTranslatableComponent(translationKeyName, color, additionalArgs2);
    }

    public static ITextComponent newTranslatableComponent(String translationKey, TextFormatting color, Object... additionalArgs){
        return new TranslationTextComponent(chatTranslationKeyFor(translationKey), additionalArgs).mergeStyle(color);
    }

    public static ITextComponent chatMessage(String translationKeyName, Object... additionalArgs){
        return chatMessage(translationKeyName, TextFormatting.WHITE, additionalArgs);
    }

    public static ITextComponent chatLink(String link, TextFormatting color){
        return new StringTextComponent(link).mergeStyle(TextFormatting.UNDERLINE, color).modifyStyle(style -> style.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, link)));
    }
}
