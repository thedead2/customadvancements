package de.thedead2.customadvancements.client;

import org.apache.logging.log4j.util.Strings;


public record ObjectPosition(float xPercent, float yPercent) {

    public static final ObjectPosition CENTERED = new ObjectPosition(0.5f, 0.5f);


    public static ObjectPosition fromString(String objectPosition) {
        String[] strings = Strings.splitList(objectPosition.replace('%', ' '));

        return new ObjectPosition(Float.parseFloat(strings[0]) / 100, Float.parseFloat(strings[1]) / 100);
    }


    @Override
    public String toString() {
        return xPercent * 100 + "%, " + xPercent * 100 + "%";
    }
}
