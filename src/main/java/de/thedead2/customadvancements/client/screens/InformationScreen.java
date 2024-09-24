package de.thedead2.customadvancements.client.screens;

import de.thedead2.customadvancements.client.Alignment;
import de.thedead2.customadvancements.client.Area;
import de.thedead2.customadvancements.client.components.TextBox;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;


public class InformationScreen extends Screen {

    private final String text;

    private final Screen parent;


    public InformationScreen(Screen parent, String content) {
        super(Component.empty());
        this.text = content;
        this.parent = parent;
    }


    @Override
    public void render(@NotNull GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        this.renderDirtBackground(pGuiGraphics);

        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
    }


    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }


    @Override
    protected void init() {
        this.addRenderableWidget(new TextBox(new Area(Alignment.TOP_CENTERED, 0, 30, 1, 0.75f, 0.7f), this.font, this.text));
        Area buttonArea = new Area(Alignment.BOTTOM_CENTERED, 0, -25, 1, 0.65f, 0.075f);
        this.addRenderableWidget(Button.builder(Component.literal("OK"), (a) -> this.minecraft.setScreen(this.parent)).pos((int) buttonArea.getX(), (int) buttonArea.getY()).size((int) buttonArea.getWidth(), (int) buttonArea.getHeight()).build());
    }
}
