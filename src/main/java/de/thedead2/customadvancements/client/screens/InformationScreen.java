package de.thedead2.customadvancements.client.screens;

import de.thedead2.betterui.components.TextBox;
import de.thedead2.betterui.fonts.formatting.FontFormatting;
import de.thedead2.betterui.fonts.formatting.FormattedString;
import de.thedead2.betterui.util.Alignment;
import de.thedead2.betterui.util.Area;
import de.thedead2.betterui.util.RenderUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.awt.*;


public class InformationScreen extends Screen {

    private final String text;

    private final Screen parent;

    Area test = new Area(() -> (float) RenderUtil.getMousePos().x, () -> (float) RenderUtil.getMousePos().y, () -> 0, () -> 20, () -> 40, () -> 0);

    public InformationScreen(Screen parent, String content) {
        super(Component.empty());
        this.text = content;
        this.parent = parent;
    }


    @Override
    public void render(@NotNull GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        this.renderDirtBackground(pGuiGraphics);

        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        Area buttonArea = new Area(0, 0, 1, 0.65f * this.width, 0.075f * this.height, 0f).alignWithOffset(Alignment.BOTTOM_CENTERED, Area.SCREEN(), 0, -25);

        pGuiGraphics.fill((int) test.getX(), (int) test.getY(), (int) test.getXMax(), (int) test.getYMax(), test.doAreasIntersect2D(buttonArea) ? Color.RED.getRGB() : Color.WHITE.getRGB());
    }


    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }


    @Override
    protected void init() {
        this.addRenderableWidget(new TextBox(new Area(0, 30, 1, 0.75f * this.width, 0.7f * this.height, 0f).align(Alignment.CENTERED, Area.SCREEN()), new FormattedString(this.text, FontFormatting.defaultFormatting())));
        Area buttonArea = new Area(0, 0, 1, 0.65f * this.width, 0.075f * this.height, 0f).alignWithOffset(Alignment.BOTTOM_CENTERED, Area.SCREEN(), 0, -25);
        this.addRenderableWidget(Button.builder(Component.literal("OK"), (a) -> this.minecraft.setScreen(this.parent)).pos((int) buttonArea.getX(), (int) buttonArea.getY()).size((int) buttonArea.getWidth(), (int) buttonArea.getHeight()).build());

    }
}
