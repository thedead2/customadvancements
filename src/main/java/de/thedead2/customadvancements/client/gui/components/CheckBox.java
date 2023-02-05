package de.thedead2.customadvancements.client.gui.components;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import de.thedead2.customadvancements.util.ModHelper;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Consumer;

public class CheckBox extends Button {

    private static final ResourceLocation CHECKBOX_CHECKED = new ResourceLocation(ModHelper.MOD_ID, "textures/gui/button/checkbox_checked.png");
    private static final ResourceLocation CHECKBOX_UNCHECKED = new ResourceLocation(ModHelper.MOD_ID, "textures/gui/button/checkbox_unchecked.png");

    private boolean value;
    private final boolean defaultValue;

    private final Consumer<CheckBox> checkBoxConsumer;

    public CheckBox(int pX, int pY, int pWidth, int pHeight, Component pMessage, boolean value, Consumer<CheckBox> checkBoxConsumer) {
        super(pX, pY, pWidth, pHeight, pMessage, (pButton -> {}));
        this.value = value;
        this.defaultValue = value;
        this.checkBoxConsumer = checkBoxConsumer;
    }

    @Override
    public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        if(this.isHoveredOrFocused()){
            RenderSystem.setShaderTexture(0, !this.value ? CHECKBOX_CHECKED : CHECKBOX_UNCHECKED);
        }
        else {
            RenderSystem.setShaderTexture(0, this.value ? CHECKBOX_CHECKED : CHECKBOX_UNCHECKED);
        }
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
        if(this.isMouseOver(pMouseX, pMouseY) && this.active){
            this.value = !this.value;
            this.checkBoxConsumer.accept(this);
        }
    }

    public boolean getValue(){
        return this.value;
    }

    public void reset(){
        this.value = this.defaultValue;
    }
}
