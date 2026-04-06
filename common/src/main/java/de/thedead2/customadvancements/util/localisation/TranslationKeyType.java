package de.thedead2.customadvancements.util.localisation;


//USES???
public enum TranslationKeyType {

    CHAT,

    ADVANCEMENT;


    public enum AdvancementKeySubType implements TranslationKeySubType {

        TITLE,

        DESCRIPTION
    }


    public interface TranslationKeySubType {}
}
