package de.thedead2.customadvancements.client.gui.components;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class ItemButton extends Button {

    private final ItemStack itemStack;

    public ItemButton(int pX, int pY, ItemStack itemStack, OnPress pOnPress) {
        super(pX, pY, 16, 16, Component.empty(), pOnPress);
        this.itemStack = itemStack;
    }

    @Override
    public boolean isMouseOver(double pMouseX, double pMouseY) {
        int leftXCorner = this.x;
        int rightXCorner = leftXCorner + this.width;
        int topYCorner = this.y;
        int bottomYCorner = topYCorner + this.height;

        return pMouseX >= leftXCorner && pMouseX <= rightXCorner && pMouseY >= topYCorner && pMouseY <= bottomYCorner;
    }

    @Override
    public void renderButton(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        Minecraft.getInstance().getItemRenderer().renderAndDecorateFakeItem(this.itemStack, this.x, this.y);
    }

    @Override
    public void onClick(double pMouseX, double pMouseY) {
        if(this.isMouseOver(pMouseX, pMouseY) && this.active){
            this.onPress();
        }
    }
}
