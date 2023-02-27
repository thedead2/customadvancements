package de.thedead2.customadvancements.client.gui.components;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class CustomImageButton extends Button {

    private final ResourceLocation texture;
    private final ResourceLocation textureHovered;

    public CustomImageButton(int pX, int pY, int pWidth, int pHeight, ResourceLocation texture, ResourceLocation textureHovered, OnPress pOnPress) {
        super(pX, pY, pWidth, pHeight, Component.empty(), pOnPress);
        this.texture = texture;
        this.textureHovered = textureHovered;
    }

    @Override
    public void renderButton(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, isHoveredOrFocused() && isActive() ? this.textureHovered : this.texture);
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
}
