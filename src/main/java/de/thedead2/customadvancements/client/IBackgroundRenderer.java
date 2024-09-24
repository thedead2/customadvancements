package de.thedead2.customadvancements.client;

import net.minecraft.client.gui.GuiGraphics;


@FunctionalInterface
public interface IBackgroundRenderer {

    void drawBg(GuiGraphics guiGraphics, int xMin, int xMax, int yMin, int yMax, int scrollX, int scrollY);
}
