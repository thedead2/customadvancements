package de.thedead2.customadvancements.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.datafixers.util.Pair;
import de.thedead2.customadvancements.client.gui.components.FakeAdvancementWidget;
import de.thedead2.customadvancements.client.gui.generator.AdvancementGeneratorGUI;
import de.thedead2.customadvancements.client.gui.generator.ClientAdvancementGenerator;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.advancements.FrameType;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.advancements.AdvancementWidgetType;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static net.minecraft.client.gui.screens.inventory.AbstractContainerScreen.renderSlotHighlight;

public class FrameAndItemSelectionScreen extends BasicInputScreen {

    private final ItemStack originalIcon;
    private final FrameType originalFrame;
    private static final int SCROLLER_WIDTH = 12;
    private static final int SCROLLER_HEIGHT = 15;
    static final SimpleContainer CONTAINER = new SimpleContainer(60);
    /** 0 -> Top, 1 -> Bottom **/
    private float scrollOffs;
    /** True if the scrollbar is being dragged */
    private boolean scrolling;
    private final ItemContainer menu;
    private int topPos;
    private int leftPos;
    protected Slot hoveredSlot;

    public FrameAndItemSelectionScreen(ClientAdvancementGenerator parent, AdvancementGeneratorGUI gui, DisplayInfo display, FakeAdvancementWidget widget, Supplier<Integer> screenWidthSupplier, Supplier<Integer> screenHeightSupplier, Supplier<int[]> screenStartPositionSupplier) {
        super(parent, gui, display, widget, screenWidthSupplier, screenHeightSupplier, screenStartPositionSupplier);
        this.originalIcon = this.display != null ? this.display.getIcon() : ClientAdvancementGenerator.getRandomItem().getDefaultInstance();
        this.originalFrame = this.display != null ? this.display.getFrame() : ClientAdvancementGenerator.DEFAULT_FRAME;
        this.menu = new ItemContainer();
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        if(this.hoveredSlot != null){
            this.updateDisplayInfo(this.hoveredSlot.getItem());
        }
        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }

    public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
        float f = (float) pDelta;
        this.scrollOffs = Mth.clamp(this.scrollOffs - f, 0.0F, 1.0F);
        this.menu.scrollTo(this.scrollOffs);
        return true;
    }

    public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
        if (this.scrolling) {
            int i = this.topPos + 18;
            int j = i + 112;
            this.scrollOffs = ((float)pMouseY - (float)i - 7.5F) / ((float)(j - i) - 15.0F);
            this.scrollOffs = Mth.clamp(this.scrollOffs, 0.0F, 1.0F);
            this.menu.scrollTo(this.scrollOffs);
            return true;
        } else {
            return super.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
        }
    }

    @Override
    public void save() {
        if(this.generator != null && this.display != null){
            this.generator.updateDisplayInfo(this.display.getIcon());
            this.generator.updateDisplayInfo(this.display.getFrame());
        }
    }

    @Override
    public void reset() {
        this.updateDisplayInfo(this.originalIcon);
        this.updateDisplayInfo(this.originalFrame);
    }

    @Override
    public void init() {
        super.init();
        this.topPos = this.screenTopLeftCorner[1] + TOP_OFFSET + 5 + FRAME_LENGTH + 10 ;
        this.leftPos = this.screenTopLeftCorner[0] + (FRAME_LENGTH + PADDING);

        int frames = 1;
        for(FrameType frameType : FrameType.values()){
            this.addRenderableWidget(new ImageButton(this.screenTopLeftCorner[0] + this.screenWidth/4 + (FRAME_LENGTH + PADDING) * frames, this.screenTopLeftCorner[1] + TOP_OFFSET + 5, FRAME_LENGTH, FRAME_LENGTH, frameType.getTexture(), 128 + AdvancementWidgetType.OBTAINED.getIndex() * FRAME_LENGTH, FRAME_LENGTH, FakeAdvancementWidget.WIDGETS_LOCATION, 256, 256, pButton -> {
                if(this.generator != null){
                    this.updateDisplayInfo(frameType);
                }
            }, (pButton, pPoseStack, pMouseX, pMouseY) -> {
                List<Component> list = new ArrayList<>();
                String name = frameType.getName();
                String temp = String.valueOf(name.charAt(0));
                name = name.replaceFirst(temp, temp.toUpperCase()) + ":";
                list.add(Component.literal(name));
                list.add(frameType.getDisplayName());

                this.renderComponentTooltip(pPoseStack, list, pMouseX, pMouseY);
            }, CommonComponents.EMPTY));
            frames++;
        }
    }



    @Override
    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        super.render(poseStack, mouseX, mouseY, partialTick);
        this.minecraft.font.drawShadow(poseStack, this.display != null ? this.display.getTitle() : DEFAULT_TITLE, screenTopLeftCorner[0] + FRAME_LENGTH + PADDING, screenTopLeftCorner[1] + 9, -1);

        this.minecraft.font.drawShadow(poseStack, "Frame:", this.leftPos, this.screenTopLeftCorner[1] + TOP_OFFSET + 5, Color.white.getRGB());
        this.hLine(poseStack, this.screenTopLeftCorner[0] + PADDING_RIGHT,this.screenTopRightCorner[0] - PADDING_RIGHT, this.screenTopLeftCorner[1] + TOP_OFFSET + 5 + FRAME_LENGTH + 7, Color.DARK_GRAY.getRGB());
        this.minecraft.font.drawShadow(poseStack, "Icon:", this.leftPos, this.topPos + 15, Color.white.getRGB());
        this.renderContainer(poseStack, mouseX, mouseY, partialTick);
        if (this.hoveredSlot != null && this.hoveredSlot.hasItem()) {
            this.renderTooltip(poseStack, this.hoveredSlot.getItem(), (int) (mouseX - screenWidth/1.6F), (int) (mouseY - screenHeight/3.5F));
        }
    }

    private void renderContainer(PoseStack poseStack, int mouseX, int mouseY, float partialTick){
        int i = this.leftPos;
        int j = this.topPos + 15;
        RenderSystem.disableDepthTest();
        PoseStack posestack = RenderSystem.getModelViewStack();
        posestack.pushPose();
        posestack.translate(i, j, 0.0D);
        RenderSystem.applyModelViewMatrix();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        this.hoveredSlot = null;
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        for(int k = 0; k < this.menu.slots.size(); ++k) {
            Slot slot = this.menu.slots.get(k);
            if (slot.isActive()) {
                RenderSystem.setShader(GameRenderer::getPositionTexShader);
                this.renderSlot(poseStack, slot);
            }

            if (this.isHovering(slot.x, slot.y, mouseX, mouseY) && slot.isActive()) {
                this.hoveredSlot = slot;
                int l = slot.x;
                int i1 = slot.y;
                renderSlotHighlight(poseStack, l, i1, this.getBlitOffset(), -2130706433);
            }
        }
    }

    protected boolean isHovering(int pX, int pY, double mouseX, double mouseY) {
        int i = this.leftPos;
        int j = this.topPos + 15;
        mouseX -= i;
        mouseY -= j;
        return mouseX >= (double)(pX - 1) && mouseX < (double)(pX + 16 + 1) && mouseY >= (double)(pY - 1) && mouseY < (double)(pY + 16 + 1);
    }

    private void renderSlot(PoseStack poseStack, Slot slot){
        int i = slot.x;
        int j = slot.y;
        ItemStack itemstack = slot.getItem();
        boolean flag1 = false;

        this.setBlitOffset(100);
        this.itemRenderer.blitOffset = 100.0F;
        if (itemstack.isEmpty() && slot.isActive()) {
            Pair<ResourceLocation, ResourceLocation> pair = slot.getNoItemIcon();
            if (pair != null) {
                TextureAtlasSprite textureatlassprite = this.minecraft.getTextureAtlas(pair.getFirst()).apply(pair.getSecond());
                RenderSystem.setShaderTexture(0, textureatlassprite.atlas().location());
                blit(poseStack, i, j, this.getBlitOffset(), 16, 16, textureatlassprite);
                flag1 = true;
            }
        }

        if (!flag1) {
            RenderSystem.enableDepthTest();
            this.itemRenderer.renderAndDecorateItem(this.minecraft.player, itemstack, i, j, slot.x + slot.y * 176);
            this.itemRenderer.renderGuiItemDecorations(this.font, itemstack, i, j, "");
        }

        this.itemRenderer.blitOffset = 0.0F;
        this.setBlitOffset(0);
    }

    static class ItemContainer extends AbstractContainerMenu{

        private final List<Item> items = ForgeRegistries.ITEMS.getValues().stream().toList();

        ItemContainer(){
            super(null, 0);
            for(int row = 0; row < 5; ++row) {
                for(int colum = 0; colum < 12; ++colum) {
                    this.addSlot(new Slot(CONTAINER, row * 9 + colum, 9 + colum * 18, 18 + row * 18));
                }
            }
            this.scrollTo(0.0F);
        }

        public void scrollTo(float pPos) {
            int i = (this.items.size() + 9 - 1) / 9 - 5;
            int j = (int)((double)(pPos * (float)i) + 0.5D);
            if (j < 0) {
                j = 0;
            }

            for(int row = 0; row < 5; ++row) {
                for(int column = 0; column < 12; ++column) {
                    int i1 = column + (row + j) * 12;
                    if (i1 >= 0 && i1 < this.items.size()) {
                        CONTAINER.setItem(column + row * 9, this.items.get(i1).getDefaultInstance());
                    }
                    else {
                        CONTAINER.setItem(column + row * 12, ItemStack.EMPTY);
                    }
                }
            }
        }

        @Override
        public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
            return null;
        }

        @Override
        public boolean stillValid(Player pPlayer) {
            return false;
        }
    }
}
