package de.thedead2.customadvancements.client.generator.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import de.thedead2.customadvancements.client.generator.AdvancementGenerator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class AdvancementCreationScreen extends BasicInputScreen {


    public AdvancementCreationScreen(Screen parent, Minecraft minecraft, AdvancementGenerator generator) {
        super(parent, minecraft, generator);
    }

    @Override
    public void init() {
        super.init();

        EditBox textField = new EditBox(this.font, 50, 50, 450, 25, Component.literal("Test"));
        this.addRenderableWidget(textField);
        this.addRenderableWidget(new Button(50, 100, 450, 35, Component.literal("Press"), Button::onPress));
    }

    @Override
    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        super.render(poseStack, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        super.generator.build();
        super.onClose();
    }

    /*@Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        switch (pButton) {
            case 0:
                super.minecraft.setScreen(new ResourceLocationInputScreen(this, super.minecraft, super.generator));
            case 1:
                super.minecraft.setScreen(new DisplayInputScreen(this, super.minecraft, super.generator));
            case 2:
                super.minecraft.setScreen(new RewardsInputScreen(this, super.minecraft, super.generator));
            case 3:
                super.minecraft.setScreen(new CriteriaInputScreen(this, super.minecraft, super.generator));
            case 4:
                super.minecraft.setScreen(new RequirementsInputScreen(this, super.minecraft, super.generator));
        }
        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }*/
}
