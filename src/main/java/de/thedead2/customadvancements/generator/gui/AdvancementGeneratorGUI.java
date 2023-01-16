package de.thedead2.customadvancements.generator.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import net.minecraft.client.multiplayer.ClientAdvancements;
import org.jetbrains.annotations.NotNull;

public class AdvancementGeneratorGUI extends AdvancementsScreen {

    //First Screen
    //private final AdvancementGenerator generator;

    public AdvancementGeneratorGUI(ClientAdvancements advancementsIn/*, AdvancementGenerator generator*/) {
        super(advancementsIn);
//        this.generator = generator;
    }

    public void init() {
        super.init();
    }

    public void removed() {
        super.removed();
    }

    @Override
    public void onClose() {
        super.onClose();
    }

    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }

    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        return super.keyPressed(pKeyCode, pScanCode, pModifiers);
    }

    public void render(@NotNull PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
    }

    public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
        return super.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
    }

}
