package de.thedead2.customadvancements.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import de.thedead2.customadvancements.generator.IAdvancementGenerator;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.NotNull;

public class BasicInputScreen extends Screen {

    protected final Screen parent;
    protected final Minecraft minecraft;
    protected IAdvancementGenerator generator;

    public BasicInputScreen(Screen parent, Minecraft minecraft, IAdvancementGenerator generator) {
        super(GameNarrator.NO_TITLE);
        this.parent = parent;
        this.minecraft = minecraft;
        this.generator = generator;
    }

    @Override
    public void init() {
        super.init();
    }

    @Override
    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        this.renderDirtBackground(0);
        super.render(poseStack, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        if(parent != null){
            assert minecraft != null;
            this.minecraft.setScreen(parent);
        }
        else {
            super.onClose();
        }
    }

    protected void setGenerator(IAdvancementGenerator generatorIn){this.generator = generatorIn;}
}
