package de.thedead2.customadvancements.client.gui;

import net.minecraft.client.gui.GuiGraphics;

@FunctionalInterface
public interface RootRenderer {

    void draw(GuiGraphics guiGraphics, int scrollX, int scrollY);
}
