package de.thedead2.customadvancements.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import de.thedead2.customadvancements.generator.IAdvancementGenerator;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.advancements.AdvancementWidgetType;
import org.jetbrains.annotations.NotNull;

import static de.thedead2.customadvancements.client.gui.components.FakeAdvancementWidget.ICON_X;
import static de.thedead2.customadvancements.client.gui.components.FakeAdvancementWidget.ICON_Y;

public class BasicInputScreen extends Screen {

    protected final AdvancementGeneratorGUI parent;
    protected final Minecraft minecraft;
    protected IAdvancementGenerator generator;


    public BasicInputScreen(AdvancementGeneratorGUI parent, Minecraft minecraft, IAdvancementGenerator generator) {
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
