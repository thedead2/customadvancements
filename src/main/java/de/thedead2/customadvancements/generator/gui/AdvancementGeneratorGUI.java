package de.thedead2.customadvancements.generator.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import de.thedead2.customadvancements.generator.AdvancementGenerator;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.screens.Screen;

public class AdvancementGeneratorGUI extends Screen implements ContainerEventHandler {

    //First Screen
    private final AdvancementGenerator generator;

    public AdvancementGeneratorGUI(AdvancementGenerator generator) {
        super(GameNarrator.NO_TITLE);
        this.generator = generator;
        getMinecraft().setScreen(new AdvancementCreationScreen(this, getMinecraft(), this.generator));
    }

    public void init() {

    }

    public void removed() {


    }

    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {

        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }

    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        return super.keyPressed(pKeyCode, pScanCode, pModifiers);
    }

    public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {

    }

    public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
        return true;
    }

    private void renderInside(PoseStack pPoseStack, int pMouseX, int pMouseY, int pOffsetX, int pOffsetY) {

    }

    public void renderWindow(PoseStack pPoseStack, int pOffsetX, int pOffsetY) {

    }

    private void renderTooltips(PoseStack pPoseStack, int pMouseX, int pMouseY, int pOffsetX, int pOffsetY) {


    }


}
