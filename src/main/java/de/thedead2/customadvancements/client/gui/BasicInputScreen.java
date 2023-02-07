package de.thedead2.customadvancements.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import de.thedead2.customadvancements.client.gui.components.CheckBox;
import de.thedead2.customadvancements.client.gui.components.EditButton;
import de.thedead2.customadvancements.client.gui.components.FakeAdvancementWidget;
import de.thedead2.customadvancements.client.gui.generator.AdvancementGeneratorGUI;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.advancements.FrameType;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.advancements.AdvancementWidgetType;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static de.thedead2.customadvancements.client.gui.components.FakeAdvancementWidget.ICON_X;
import static de.thedead2.customadvancements.client.gui.components.FakeAdvancementWidget.ICON_Y;
import static de.thedead2.customadvancements.util.ModHelper.LOGGER;
import static de.thedead2.customadvancements.util.ModHelper.MOD_ID;

public class BasicInputScreen extends Screen {

    protected final Screen parent;
    protected final Minecraft minecraft = Minecraft.getInstance();
    protected final Set<CheckBox> checkBoxes = new HashSet<>();
    protected final Map<String, EditBox> editBoxes = new HashMap<>();
    protected final FakeAdvancementWidget widget;

    protected DisplayInfo display;
    protected ResourceLocation parentId;

    protected int screenWidth;
    protected int screenHeight;
    protected int[] screenTopLeftCorner;
    protected int[] screenTopRightCorner;
    protected int[] screenBottomLeftCorner;
    protected int[] screenBottomRightCorner;

    protected int[] framePos;
    protected int[] editBox;

    protected static final int EDIT_BUTTON_LENGTH = 20;
    protected static final int PADDING_RIGHT = 10;
    protected static final int PADDING = 2;
    protected static final int FRAME_LENGTH = 26;
    protected static final int TOP_OFFSET = 26;

    protected static final Component DEFAULT_DESCRIPTION = Component.literal("Example Description");
    protected static final Component DEFAULT_TITLE = Component.literal("Example Title");
    protected static final ItemStack DEFAULT_ITEM = new ItemStack(Items.DIAMOND);
    protected static final FrameType DEFAULT_FRAME = FrameType.TASK;
    protected static final ResourceLocation DEFAULT_BACKGROUND = new ResourceLocation( "textures/gui/advancements/stone.png");

    protected final Supplier<Integer> screenWidthSupplier;
    protected final Supplier<Integer> screenHeightSupplier;
    protected final Supplier<int[]> screenStartPositionSupplier;


    public BasicInputScreen(Screen parent, DisplayInfo display, FakeAdvancementWidget widget, Supplier<Integer> screenWidthSupplier, Supplier<Integer> screenHeightSupplier, Supplier<int[]> screenStartPositionSupplier) {
        super(GameNarrator.NO_TITLE);
        this.parent = parent;
        this.display = display;
        this.widget = widget;
        this.screenWidthSupplier = screenWidthSupplier;
        this.screenHeightSupplier = screenHeightSupplier;
        this.screenStartPositionSupplier = screenStartPositionSupplier;
    }

    public void updateDisplayInfo(ItemStack itemStack, Component title, Component description, ResourceLocation background, FrameType frame, boolean showToast, boolean announceToChat, boolean hidden) {
        if(this.display != null){
            this.display = new DisplayInfo(itemStack == null ? this.display.getIcon() : itemStack, title == null ? this.display.getTitle() : title, description == null ? this.display.getDescription() : description, background == null ? (this.parentId != null ? null: this.display.getBackground()) : background, frame == null ? this.display.getFrame() : frame, showToast, announceToChat, hidden);
        }
        else {
            this.display = new DisplayInfo(itemStack == null ? DEFAULT_ITEM : itemStack, title == null ? DEFAULT_TITLE : title, description == null ? DEFAULT_DESCRIPTION : description, background == null ? (this.parentId != null ? null: DEFAULT_BACKGROUND) : background, frame == null ? DEFAULT_FRAME : frame, showToast, announceToChat, hidden);
        }
        LOGGER.debug("Updated DisplayInfo with: {}, {}, {}, {}, {}, {}, {}, {}", this.display.getIcon(), this.display.getTitle(), this.display.getDescription(), this.display.getBackground(), this.display.getFrame(), this.display.shouldShowToast(), this.display.shouldAnnounceChat(), this.display.isHidden());
    }


    public <T, R, V extends Boolean> void updateDisplayInfo(@NotNull T t, @Nullable R r, @Nullable V v){
        if(this.display != null){
            if(t instanceof ItemStack){
                this.updateDisplayInfo((ItemStack) t, null, null, null, null, this.display.shouldShowToast(), this.display.shouldAnnounceChat(), this.display.isHidden());
            }
            else if (t instanceof Items) {
                this.updateDisplayInfo(new ItemStack((ItemLike) t), null, null, null, null, this.display.shouldShowToast(), this.display.shouldAnnounceChat(), this.display.isHidden());
            }
            else if (t instanceof ResourceLocation) {
                this.updateDisplayInfo(null, null, null, (ResourceLocation) t, null, this.display.shouldShowToast(), this.display.shouldAnnounceChat(), this.display.isHidden());
            }
            else if (t instanceof FrameType) {
                this.updateDisplayInfo(null, null, null, null, (FrameType) t, this.display.shouldShowToast(), this.display.shouldAnnounceChat(), this.display.isHidden());
            }
            else if (t instanceof Component && r instanceof Component) {
                this.updateDisplayInfo(null, (Component) t, (Component) r, null, null, this.display.shouldShowToast(), this.display.shouldAnnounceChat(), this.display.isHidden());
            }
            else if (t instanceof Boolean && r instanceof Boolean && v != null) {
                this.updateDisplayInfo(null, null, null, null, null, (Boolean) t, (Boolean) r, v);
            }
        }
        else {
            if(t instanceof ItemStack){
                this.updateDisplayInfo((ItemStack) t, null, null, null, null, true, true, false);
            }
            else if (t instanceof Items) {
                this.updateDisplayInfo(new ItemStack((ItemLike) t), null, null, null, null, true, true, false);
            }
            else if (t instanceof ResourceLocation) {
                this.updateDisplayInfo(null, null, null, (ResourceLocation) t, null, true, true, false);
            }
            else if (t instanceof FrameType) {
                this.updateDisplayInfo(null, null, null, null, (FrameType) t, true, true, false);
            }
            else if (t instanceof Component && r instanceof Component) {
                this.updateDisplayInfo(null, (Component) t, (Component) r, null, null, true, true, false);
            }
            else if (t instanceof Boolean && r instanceof Boolean && v != null) {
                this.updateDisplayInfo(null, null, null, null, null, (Boolean) t, (Boolean) r, v);
            }
        }
    }

    public <T> void updateDisplayInfo(@NotNull T t){
        this.updateDisplayInfo(t, null, null);
    }

    public <T, R> void updateDisplayInfo(@NotNull T t, @Nullable R r){
        this.updateDisplayInfo(t, r, null);
    }


    public int getFontWidth(String in){
        return this.minecraft.font.width(in);
    }

    public void addCheckBox(CheckBox checkBox){
        this.addRenderableWidget(checkBox);
        this.checkBoxes.add(checkBox);
    }

    public void addEditBox(int pX, int pY, String key, Supplier<String> input, Consumer<EditBox> action){
        int editBoxWidth = this.display != null ? getFontWidth(input.get()) + 10 : editBox[0];
        EditBox editBox = new EditBox(this.font, pX, pY, editBoxWidth, this.editBox[1], Component.empty());
        editBox.setMaxLength(Integer.MAX_VALUE);
        editBox.setVisible(false);
        if(this.display != null){
            editBox.setValue(input.get());
        }
        this.addRenderableWidget(editBox);

        this.addRenderableWidget(new EditButton(screenTopRightCorner[0] - EDIT_BUTTON_LENGTH - PADDING_RIGHT, pY - 3, EDIT_BUTTON_LENGTH -1, EDIT_BUTTON_LENGTH -1, Component.empty(), pButton -> {
            if(!editBox.isVisible()){
                editBox.setVisible(true);
            }
            else {
                action.accept(editBox);
                editBox.setVisible(false);
            }
        }));

        this.editBoxes.put(key, editBox);
    }


    @Override
    public void init() {
        this.screenWidth = this.screenWidthSupplier.get();
        this.screenHeight = this.screenHeightSupplier.get();
        this.screenTopLeftCorner = this.screenStartPositionSupplier.get();
        this.screenTopRightCorner = new int[]{screenTopLeftCorner[0] + this.screenWidth, screenTopLeftCorner[1]};
        this.screenBottomLeftCorner = new int[]{screenTopLeftCorner[0], screenTopLeftCorner[1] + this.screenHeight};
        this.screenBottomRightCorner = new int[]{screenTopLeftCorner[0] + this.screenWidth, screenTopLeftCorner[1] + this.screenHeight};
        this.framePos = new int[]{this.screenTopLeftCorner[0] - 3, this.screenTopLeftCorner[1]};
        this.editBox = new int[]{this.screenTopLeftCorner[0], this.minecraft.font.lineHeight + 4};

        this.initMainButtons();
    }

    @Override
    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        if (this.widget != null && this.parent instanceof AdvancementGeneratorGUI) {
            int offsetX = -(Mth.floor(this.widget.tab.scrollX) + this.widget.getX() + AdvancementGeneratorGUI.WINDOW_WIDTH / 2 - FakeAdvancementWidget.FRAME_WIDTH - PADDING);
            ((AdvancementGeneratorGUI) this.parent).setScreenOffset(offsetX, 0);
            ((AdvancementGeneratorGUI) this.parent).setRenderTooltips(false);
            this.parent.render(poseStack, mouseX, mouseY, partialTick);
        }
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, FakeAdvancementWidget.WIDGETS_LOCATION);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableBlend();

        this.renderRectangleFromImage(poseStack, screenTopLeftCorner[0], screenTopLeftCorner[1], screenWidth, screenHeight, 10, 200, 26, 0, 52);
        this.renderRectangleFromImage(poseStack, screenTopLeftCorner[0], screenTopLeftCorner[1], screenWidth, TOP_OFFSET, 10, 200, 26, 0, 0);

        this.drawIcon(poseStack, this.framePos[0] , this.framePos[1]);
        super.render(poseStack, mouseX, mouseY, partialTick);
    }

    public void initMainButtons(){
        int[] mainButton = {this.screenWidth/3 - (FRAME_LENGTH /2) + 2, 20};

        this.addRenderableWidget(new Button(screenBottomLeftCorner[0] + PADDING + FRAME_LENGTH /2, screenBottomLeftCorner[1] - mainButton[1] - 15, mainButton[0], mainButton[1], Component.literal("Save"), button -> {
            this.save();
            this.onClose();
        }));
        this.addRenderableWidget(new Button(screenBottomLeftCorner[0] + PADDING + FRAME_LENGTH /2 + mainButton[0] + 2, screenBottomLeftCorner[1] - mainButton[1] - 15, mainButton[0], mainButton[1], Component.literal("Exit"), button -> this.onClose()));
        this.addRenderableWidget(new Button(screenBottomLeftCorner[0] + PADDING + FRAME_LENGTH /2 + (mainButton[0] + 2) * 2, screenBottomLeftCorner[1] - mainButton[1] - 15, mainButton[0], mainButton[1], Component.literal("Reset"), button -> this.reset()));
    }

    protected void drawIcon(PoseStack pPoseStack, int pX, int pY){
        if(this.display != null){
            this.blit(pPoseStack, pX, pY, this.display.getFrame().getTexture(), 128 + AdvancementWidgetType.OBTAINED.getIndex() * FRAME_LENGTH, FRAME_LENGTH, FRAME_LENGTH);
            this.minecraft.getItemRenderer().renderAndDecorateFakeItem(this.display.getIcon(), (int) (pX + ICON_X/1.5), pY + ICON_Y);
        }
        else {
            this.blit(pPoseStack, pX, pY, FrameType.TASK.getTexture(), 128 + AdvancementWidgetType.OBTAINED.getIndex() * FRAME_LENGTH, FRAME_LENGTH, FRAME_LENGTH);
            this.minecraft.getItemRenderer().renderAndDecorateFakeItem(new ItemStack(Items.DIAMOND), (int) (pX + ICON_X/1.5), pY + ICON_Y);
        }
    }

    protected void renderRectangleFromImage(PoseStack pPoseStack, int pX, int pY, int pWidth, int pHeight, int pPadding, int pUWidth, int pVHeight, int pUOffset, int pVOffset) {
        this.blit(pPoseStack, pX, pY, pUOffset, pVOffset, pPadding, pPadding);
        this.renderRepeating(pPoseStack, pX + pPadding, pY, pWidth - pPadding - pPadding, pPadding, pUOffset + pPadding, pVOffset, pUWidth - pPadding - pPadding, pVHeight);
        this.blit(pPoseStack, pX + pWidth - pPadding, pY, pUOffset + pUWidth - pPadding, pVOffset, pPadding, pPadding);
        this.blit(pPoseStack, pX, pY + pHeight - pPadding, pUOffset, pVOffset + pVHeight - pPadding, pPadding, pPadding);
        this.renderRepeating(pPoseStack, pX + pPadding, pY + pHeight - pPadding, pWidth - pPadding - pPadding, pPadding, pUOffset + pPadding, pVOffset + pVHeight - pPadding, pUWidth - pPadding - pPadding, pVHeight);
        this.blit(pPoseStack, pX + pWidth - pPadding, pY + pHeight - pPadding, pUOffset + pUWidth - pPadding, pVOffset + pVHeight - pPadding, pPadding, pPadding);
        this.renderRepeating(pPoseStack, pX, pY + pPadding, pPadding, pHeight - pPadding - pPadding, pUOffset, pVOffset + pPadding, pUWidth, pVHeight - pPadding - pPadding);
        this.renderRepeating(pPoseStack, pX + pPadding, pY + pPadding, pWidth - pPadding - pPadding, pHeight - pPadding - pPadding, pUOffset + pPadding, pVOffset + pPadding, pUWidth - pPadding - pPadding, pVHeight - pPadding - pPadding);
        this.renderRepeating(pPoseStack, pX + pWidth - pPadding, pY + pPadding, pPadding, pHeight - pPadding - pPadding, pUOffset + pUWidth - pPadding, pVOffset + pPadding, pUWidth, pVHeight - pPadding - pPadding);
    }

    protected void renderRepeating(PoseStack pPoseStack, int pX, int pY, int pBorderToU, int pBorderToV, int pUOffset, int pVOffset, int pUWidth, int pVHeight) {
        for(int i = 0; i < pBorderToU; i += pUWidth) {
            int j = pX + i;
            int k = Math.min(pUWidth, pBorderToU - i);

            for(int l = 0; l < pBorderToV; l += pVHeight) {
                int i1 = pY + l;
                int j1 = Math.min(pVHeight, pBorderToV - l);
                this.blit(pPoseStack, j, i1, pUOffset, pVOffset, k, j1);
            }
        }
    }

    public void save(){}

    public void reset(){}

    @Override
    public void onClose() {
        if(this.parent != null){
            if(this.parent instanceof AdvancementGeneratorGUI){
                ((AdvancementGeneratorGUI) this.parent).setRenderTooltips(true);
            }
            this.minecraft.setScreen(this.parent);
        }
        else {
            super.onClose();
        }
    }
}
