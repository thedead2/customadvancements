package de.thedead2.customadvancements.client;

import com.google.gson.JsonElement;
import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;


public record GradientColor(int red, int green, int blue, int alpha, float stopPercent) implements Comparable<GradientColor> {

    public GradientColor(float red, float green, float blue, float alpha, float stopPercent) {
        this((int) red * 255, (int) green * 255, (int) blue * 255, (int) alpha * 255, stopPercent);
    }


    public GradientColor(int color, float stopPercent) {
        this((color >> 16 & 255), (color >> 8 & 255), (color & 255), (color >> 24 & 255), stopPercent);
    }


    public static GradientColor fromJson(JsonElement jsonElement) {
        String s = jsonElement.getAsString();
        String[] strings = Strings.splitList(s);
        int[] colors = new int[4];
        float stopPercent = 0;

        for (int i = 0; i < strings.length; i++) {
            if (i < 4) {
                colors[i] = Integer.parseInt(strings[i]);
            }
            else {
                stopPercent = Float.parseFloat(strings[i]);
            }
        }

        return new GradientColor(colors[0], colors[1], colors[2], colors[3], stopPercent);
    }


    @Override
    public int compareTo(@NotNull GradientColor o) {
        return Float.compare(this.stopPercent, o.stopPercent);
    }
}
