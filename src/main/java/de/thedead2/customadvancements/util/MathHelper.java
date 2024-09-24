package de.thedead2.customadvancements.util;


public class MathHelper {

    public static long ticksToMillis(float ticks) {
        return secondsToMillis(ticksToSeconds(ticks));
    }


    public static long secondsToMillis(long seconds) {
        return seconds * 1000L;
    }


    public static long ticksToSeconds(float ticks) {
        return (long) (ticks / 20);
    }


    public static int millisToTicks(long millis) {
        return secondsToTicks(Math.toIntExact(millisToSeconds(millis)));
    }


    public static int secondsToTicks(float seconds) {
        return Math.round(seconds * 20);
    }


    public static long millisToSeconds(long millis) {
        return millis / 1000L;
    }
}
