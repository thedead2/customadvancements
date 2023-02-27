package de.thedead2.customadvancements.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import de.thedead2.customadvancements.client.gui.components.*;
import de.thedead2.customadvancements.client.gui.generator.AdvancementGeneratorGUI;
import de.thedead2.customadvancements.client.gui.generator.ClientAdvancementGenerator;
import de.thedead2.customadvancements.util.ModHelper;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.advancements.FrameType;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.advancements.AdvancementWidgetType;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static de.thedead2.customadvancements.client.gui.components.FakeAdvancementWidget.ICON_X;
import static de.thedead2.customadvancements.client.gui.components.FakeAdvancementWidget.ICON_Y;
import static de.thedead2.customadvancements.util.ModHelper.LOGGER;

public class BasicInputScreen extends Screen {

    protected final ClientAdvancementGenerator generator;
    protected final AdvancementGeneratorGUI mainGui;
    protected final Minecraft minecraft = Minecraft.getInstance();
    protected final Map<String, CheckBox> checkBoxes = new HashMap<>();
    protected final Map<String, EditBoxWithEditButton> editBoxes = new HashMap<>();
    protected final Map<String, DropDownList<?>> dropDownLists = new HashMap<>();
    protected final Map<String, Button> mainButtons = new HashMap<>();
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
    //protected static final ItemStack DEFAULT_ITEM = new ItemStack(Items.DIAMOND);
    protected static final FrameType DEFAULT_FRAME = FrameType.TASK;
    protected static final ResourceLocation DEFAULT_BACKGROUND = new ResourceLocation( "textures/gui/advancements/backgrounds/stone.png");

    protected final Supplier<Integer> screenWidthSupplier;
    protected final Supplier<Integer> screenHeightSupplier;
    protected final Supplier<int[]> screenStartPositionSupplier;

    int i = 0;
    ItemStack item = getRandomItem().getDefaultInstance();


    public BasicInputScreen(ClientAdvancementGenerator generator, AdvancementGeneratorGUI gui, DisplayInfo display, @NotNull FakeAdvancementWidget widget, Supplier<Integer> screenWidthSupplier, Supplier<Integer> screenHeightSupplier, Supplier<int[]> screenStartPositionSupplier) {
        super(GameNarrator.NO_TITLE);
        this.generator = generator;
        this.mainGui = gui;
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
            this.display = new DisplayInfo(itemStack == null ? getRandomItem().getDefaultInstance() : itemStack, title == null ? DEFAULT_TITLE : title, description == null ? DEFAULT_DESCRIPTION : description, background == null ? (this.parentId != null ? null: DEFAULT_BACKGROUND) : background, frame == null ? DEFAULT_FRAME : frame, showToast, announceToChat, hidden);
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

            if(this.generator != null && this.generator.advancementId != null){
                this.mainGui.updateAdvancementDisplay(this.generator.advancementId, this.generator.parentId, this.display);
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

    public void addCheckBox(String id, CheckBox checkBox){
        this.addRenderableWidget(checkBox);
        this.checkBoxes.put(id, checkBox);
    }

    public void addDropDownList(Collection<?> entries, String key, int x, int y, int listHeight, Supplier<String> input, Supplier<Integer> color, Consumer<EditBox> onSave){
        EditBox editBox1 = this.addEditBox(x, y, key, input, color, onSave);
        DropDownList<?> list = new DropDownList<>(entries, editBox1, listHeight);
        this.addRenderableWidget(list);
        this.dropDownLists.put(key, list);
    }

    public EditBox addEditBox(int pX, int pY, String key, Supplier<String> input, Supplier<Integer> color, Consumer<EditBox> action){
        int temp = this.display != null ? getFontWidth(input.get()) + 10 : editBox[0];
        int editBoxWidth = temp <= this.screenWidth/2 && temp >= 50 ? temp : this.screenWidth/2;
        CustomEditBox editBox = new CustomEditBox(this.font, pX, pY, editBoxWidth, this.editBox[1], Component.empty(), color);
        editBox.setMaxLength(Integer.MAX_VALUE);
        editBox.setVisible(false);
        try {
            editBox.setValue(input.get());
        }
        catch (NullPointerException ignored){}


        this.addRenderableWidget(editBox);

        EditButton editButton = new EditButton(screenTopRightCorner[0] - EDIT_BUTTON_LENGTH - PADDING_RIGHT, pY - 3, EDIT_BUTTON_LENGTH -1, EDIT_BUTTON_LENGTH -1, Component.empty(), pButton -> {
            if(!editBox.isVisible()){
                editBox.setVisible(true);
            }
            else {
                action.accept(editBox);
                editBox.setVisible(false);
            }
        });

        this.addRenderableWidget(editButton);
        this.editBoxes.put(key, new EditBoxWithEditButton(editBox, editButton));

        return editBox;
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
        if (this.widget != null && this.mainGui != null) {
            int offsetX = -(Mth.floor(this.widget.tab.scrollX) + this.widget.getX() + AdvancementGeneratorGUI.WINDOW_WIDTH / 2 - FakeAdvancementWidget.FRAME_WIDTH - PADDING);
            this.mainGui.setScreenOffset(offsetX, 0);
            this.mainGui.setRenderTooltips(false);
            this.mainGui.render(poseStack, mouseX, mouseY, partialTick);
        }

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, FakeAdvancementWidget.WIDGETS_LOCATION);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableBlend();

        this.renderRectangleFromImage(poseStack, screenTopLeftCorner[0], screenTopLeftCorner[1], screenWidth, screenHeight, 10, 200, 26, 0, 52);
        this.renderRectangleFromImage(poseStack, screenTopLeftCorner[0], screenTopLeftCorner[1], screenWidth, TOP_OFFSET, 10, 200, 26, 0, 0);

        this.drawIcon(poseStack, this.framePos[0] , this.framePos[1]);

        for(Widget widget : this.renderables) {
            if(widget instanceof Button button){
                if(this.isInScreen(button)){
                    button.render(poseStack, mouseX, mouseY, partialTick);
                }
            }
            else {
                widget.render(poseStack, mouseX, mouseY, partialTick);
            }
        }
    }

    public void initMainButtons(){
        int[] mainButton = {this.screenWidth/3 - (FRAME_LENGTH /2) + 2, 20};

        Button saveButton = new Button(screenBottomLeftCorner[0] + PADDING + FRAME_LENGTH /2, screenBottomLeftCorner[1] - mainButton[1] - 15, mainButton[0], mainButton[1], Component.literal("Save"), button -> {
            this.save();
            this.onClose();
        }, (pButton, pPoseStack, pMouseX, pMouseY) -> this.onMainButtonTooltip("save", pPoseStack, pMouseX, pMouseY));
        this.mainButtons.put("save", saveButton);
        Button exitButton = new Button(screenBottomLeftCorner[0] + PADDING + FRAME_LENGTH /2 + mainButton[0] + 2, screenBottomLeftCorner[1] - mainButton[1] - 15, mainButton[0], mainButton[1], Component.literal("Exit"), button -> this.onClose());
        this.mainButtons.put("exit", exitButton);
        Button resetButton = new Button(screenBottomLeftCorner[0] + PADDING + FRAME_LENGTH /2 + (mainButton[0] + 2) * 2, screenBottomLeftCorner[1] - mainButton[1] - 15, mainButton[0], mainButton[1], Component.literal("Reset"), button -> this.reset());
        this.mainButtons.put("reset", resetButton);

        this.addRenderableWidget(saveButton);
        this.addRenderableWidget(exitButton);
        this.addRenderableWidget(resetButton);
    }

    protected void drawIcon(PoseStack pPoseStack, int pX, int pY){
        if(this.display != null){
            this.blit(pPoseStack, pX, pY, this.display.getFrame().getTexture(), 128 + AdvancementWidgetType.OBTAINED.getIndex() * FRAME_LENGTH, FRAME_LENGTH, FRAME_LENGTH);
            this.minecraft.getItemRenderer().renderAndDecorateFakeItem(this.display.getIcon(), (int) (pX + ICON_X/1.5), pY + ICON_Y);
        }
        else {
            this.blit(pPoseStack, pX, pY, DEFAULT_FRAME.getTexture(), 128 + AdvancementWidgetType.OBTAINED.getIndex() * FRAME_LENGTH, FRAME_LENGTH, FRAME_LENGTH);
            i++;
            if(i > 40){
                item = getRandomItem().getDefaultInstance();
                i = 0;
            }
            if(this.widget.getAdvancement().getId().toString().equals(ModHelper.MOD_ID + ":" + "fake_root_advancement")) {
                this.widget.tab.drawRandomIcon((int) (pX + ICON_X/1.5) - 70, pY + ICON_Y + 19, this.itemRenderer);
            }
            else {
                this.minecraft.getItemRenderer().renderAndDecorateFakeItem(item, (int) (pX + ICON_X/1.5), pY + ICON_Y);
            }
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

    protected boolean isInScreen(Button button){
        int widgetX = button.x;
        int widgetY = button.y;
        int[] mainButton = {this.screenWidth/3 - (FRAME_LENGTH /2) + 2, 20};

        return widgetX >= screenTopLeftCorner[0] && widgetX <= screenTopRightCorner[0] && widgetY >= screenTopLeftCorner[1] && widgetY <= screenBottomLeftCorner[1] - mainButton[1] - 15;
    }

    public void save(){
        this.onClose();
    }

    public void reset(){}

    public void onMainButtonTooltip(String buttonId, PoseStack poseStack, int mouseX, int mouseY) {}

    public static Item getRandomItem(){
        Random random = new Random();
        List<Item> items = ForgeRegistries.ITEMS.getValues().stream().toList();
        int randomInt = random.nextInt(items.size());
        return items.get(randomInt);
    }

    @Override
    public void onClose() {
        if(this.generator != null){
            this.minecraft.setScreen(this.generator);
        }
        else {
            super.onClose();
        }
    }

    protected record EditBoxWithEditButton(EditBox editBox, EditButton editButton){}
}
