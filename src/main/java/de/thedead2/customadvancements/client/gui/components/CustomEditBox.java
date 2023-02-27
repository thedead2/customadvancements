package de.thedead2.customadvancements.client.gui.components;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class CustomEditBox extends EditBox {

    private final Supplier<Integer> color;

    public CustomEditBox(Font pFont, int pX, int pY, int pWidth, int pHeight, Component pMessage, Supplier<Integer> color) {
        super(pFont, pX, pY, pWidth, pHeight, pMessage);
        this.color = color;
    }

    @Override
    public void render(@NotNull PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        if(this.isVisible()){
            super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
        }
        else {
            var font = Minecraft.getInstance().font;
            font.drawShadow(pPoseStack, this.getValue(), this.x, this.y + 2, this.color.get());
        }
    }
}
