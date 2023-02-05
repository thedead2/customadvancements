package de.thedead2.customadvancements.client.gui.components;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import static de.thedead2.customadvancements.util.ModHelper.MOD_ID;

public class EditButton extends Button {

    private FakeAdvancementWidget widget;
    private static final ResourceLocation EDIT_BUTTON = new ResourceLocation(MOD_ID, "textures/gui/button/edit_button.png");
    private static final ResourceLocation EDIT_BUTTON_HOVERED = new ResourceLocation(MOD_ID, "textures/gui/button/edit_button_hovered.png");

    public EditButton(int pX, int pY, int pWidth, int pHeight, Component pMessage, OnPress pOnPress, FakeAdvancementWidget widget) {
        this(pX, pY, pWidth, pHeight, pMessage, pOnPress);
        this.widget = widget;
    }

    public EditButton(int pX, int pY, int pWidth, int pHeight, Component pMessage, OnPress pOnPress) {
        super(pX, pY, pWidth, pHeight, pMessage, pOnPress);
    }


    @Override
    public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, isHoveredOrFocused() ? EDIT_BUTTON_HOVERED : EDIT_BUTTON);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        int iconX = this.x;
        int iconY = this.y;
        float brightness = this.active ? 1.0F : 0.5F;

        RenderSystem.setShaderColor(brightness, brightness, brightness, this.alpha);
        blit(poseStack, iconX, iconY, this.getBlitOffset(), 0, 0, this.width, this.height, this.height, this.width);

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
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
    public void onClick(double pMouseX, double pMouseY) {
        if(this.isMouseOver(pMouseX, pMouseY) && this.widget != null && this.widget.drawingTooltip && this.active){
            this.onPress();
        }
        else if(this.isMouseOver(pMouseX, pMouseY) && this.widget == null && this.active){
            this.onPress();
        }
    }
}
