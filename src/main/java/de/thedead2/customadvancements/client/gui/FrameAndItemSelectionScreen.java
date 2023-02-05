package de.thedead2.customadvancements.client.gui;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class FrameAndItemSelectionScreen extends Screen {

    protected FrameAndItemSelectionScreen(Component pTitle) {
        super(pTitle);
    }

    /* public boolean isMouseOverFrame(double mouseX, double mouseY){
        int leftXCorner = this.FRAME_POS[0];
        int rightXCorner = leftXCorner + FRAME_WIDTH;
        int topYCorner = this.FRAME_POS[1];
        int bottomYCorner = topYCorner + FRAME_WIDTH;

        return mouseX >= leftXCorner && mouseX <= rightXCorner && mouseY >= topYCorner && mouseY <= bottomYCorner;
    }

    public void renderItemAndFrameSelection(PoseStack poseStack, int mouseX, int mouseY, float partialTick){
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, FakeAdvancementWidget.WIDGETS_LOCATION);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableBlend();

        this.renderRectangleFromImage(poseStack, screenTopLeftCorner[0], screenTopLeftCorner[1], screenWidth - 10, screenHeight/2, 10, 200, 26, 0, 52);

        this.drawIcon(poseStack, this.FRAME_POS[0] , this.FRAME_POS[1], this.display);

        int frames = 1;
        for(FrameType frameType : FrameType.values()){
            this.blit(poseStack, screenTopLeftCorner[0] + (FRAME_WIDTH + PADDING) * frames, screenTopLeftCorner[1] + 10, frameType.getTexture(), 128 + AdvancementWidgetType.OBTAINED.getIndex() * FRAME_WIDTH, FRAME_WIDTH, FRAME_WIDTH);
            frames++;
        }

        int columns = 1;
        int rows = 1;
        for (Item item : ForgeRegistries.ITEMS){
            if(columns < 10){
                this.minecraft.getItemRenderer().renderAndDecorateFakeItem(item.getDefaultInstance(), screenTopLeftCorner[0] + FRAME_WIDTH/2 + PADDING + (ICON_X + 17) * columns, screenTopLeftCorner[1] + 10 + FRAME_WIDTH + 2 + (ICON_Y + 17) * rows);
                columns++;
            }
            else {
                columns = 1;
                rows++;
                this.minecraft.getItemRenderer().renderAndDecorateFakeItem(item.getDefaultInstance(), screenTopLeftCorner[0] + FRAME_WIDTH/2 + PADDING + (ICON_X + 17) * columns, screenTopLeftCorner[1] + 10 + FRAME_WIDTH + 2 + (ICON_Y + 17) * rows);
            }
        }
    }
    */
}
