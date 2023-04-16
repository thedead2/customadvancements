package de.thedead2.customadvancements.util.language;

public enum TranslationKeyType {
    CHAT,
    ADVANCEMENT;

    public interface TranslationKeySubType{}

    public enum AdvancementKeySubType implements TranslationKeySubType{
        TITLE,
        DESCRIPTION;
    }
}
