package de.thedead2.customadvancements.client.gui.generator;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import de.thedead2.customadvancements.client.gui.components.EditButton;
import de.thedead2.customadvancements.client.gui.components.FakeAdvancementWidget;
import de.thedead2.customadvancements.util.handler.AdvancementHandler;
import net.minecraft.ResourceLocationException;
import net.minecraft.advancements.*;
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

import java.awt.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static de.thedead2.customadvancements.client.gui.components.FakeAdvancementWidget.ICON_X;
import static de.thedead2.customadvancements.client.gui.components.FakeAdvancementWidget.ICON_Y;
import static de.thedead2.customadvancements.util.ModHelper.*;

public class ClientAdvancementGenerator extends Screen {

    private final Minecraft minecraft = Minecraft.getInstance();
    private final int EDIT_BUTTON_LENGTH = 20;
    private final int PADDING_RIGHT = 10;
    private final int PADDING = 2;
    public final int screenWidth = this.minecraft.screen.width - this.minecraft.screen.width/3 - PADDING;
    public final int screenHeight = this.minecraft.screen.height - PADDING;
    private final int FRAME_WIDTH = 26;
    private final int[] screenTopLeftCorner = {this.minecraft.screen.width/3, 2};
    private final int[] screenTopRightCorner = {screenTopLeftCorner[0] + this.screenWidth, screenTopLeftCorner[1]};
    private final int[] screenBottomLeftCorner = {screenTopLeftCorner[0], screenTopLeftCorner[1] + this.screenHeight};
    private final int[] screenBottomRightCorner = {screenTopLeftCorner[0] + this.screenWidth, screenTopLeftCorner[1] + this.screenHeight};
    private final int[] EDIT_BOX = {screenTopLeftCorner[0], this.minecraft.font.lineHeight + 4};
    private final int textPaddingUp = this.minecraft.font.lineHeight + PADDING;

    private Advancement.Builder builder;
    protected ResourceLocation parentId;
    protected DisplayInfo display;
    protected AdvancementRewards rewards;
    protected Map<String, Criterion> criteria;
    protected String[][] requirements;

    protected ResourceLocation advancementId;
    private final Map<ResourceLocation, Advancement> advancements = new HashMap<>();
    private final FakeAdvancementWidget widget;
    private boolean editTitle = false;
    private boolean editDescription = false;
    private boolean editId = false;
    private boolean editBgId = false;
    private boolean isSaved = false;
    private final Component defaultDescription = Component.literal("Example Description");
    private final Component defaultTitle = Component.literal("Example Title");

    private final AdvancementGeneratorGUI parent;
    private Advancement original;

    public ClientAdvancementGenerator(AdvancementGeneratorGUI parent, Advancement editAdvancement, FakeAdvancementWidget widget) {
        this(parent, editAdvancement.getParent() != null ? editAdvancement.getParent().getId() : null, widget);
        this.display = editAdvancement.getDisplay();
        this.requirements = editAdvancement.getRequirements();
        this.criteria = editAdvancement.getCriteria();
        this.rewards = editAdvancement.getRewards();
        this.advancementId = editAdvancement.getId();
        this.original = editAdvancement;
        loginfos();
    }

    public ClientAdvancementGenerator(AdvancementGeneratorGUI parent, ResourceLocation parentId, FakeAdvancementWidget widget) {
        super(GameNarrator.NO_TITLE);
        this.parent = parent;
        this.builder = Advancement.Builder.advancement();
        this.parentId = parentId;
        this.minecraft.getSingleplayerServer().getAdvancements().getAllAdvancements().forEach(advancement -> this.advancements.put(advancement.getId(), advancement));
        this.widget = widget;

        if(this.advancementId == null){
            loginfos();
        }
    }

    private void loginfos(){
        LOGGER.debug("Width: " + this.minecraft.screen.width);
        LOGGER.debug("Height: " + this.minecraft.screen.height);
        LOGGER.debug("parent screen: " + this.parent);
        LOGGER.debug("minecraft: " + this.minecraft);
        LOGGER.debug("advancement builder" + this.builder);
        LOGGER.debug("parent id: " + this.parentId);
        //LOGGER.debug("advancements collection: " + this.advancements);
        LOGGER.debug("display info: " + this.display);
        LOGGER.debug("requirements: " + Arrays.deepToString(this.requirements));
        LOGGER.debug("criteria: " + this.criteria);
        LOGGER.debug("rewards: " + this.rewards);
        LOGGER.debug("id: " + this.advancementId);
    }

    public void updateDisplayInfo(ItemStack itemStack, Component title, Component description, ResourceLocation background, FrameType frame, boolean showToast, boolean announceToChat, boolean hidden) {
        LOGGER.info(background);
        if(this.display != null){
            this.display = new DisplayInfo(itemStack == null ? this.display.getIcon() : itemStack, title == null ? this.display.getTitle() : title, description == null ? this.display.getDescription() : description, background == null ? (this.parentId != null ? null: this.display.getBackground()) : background, frame == null ? this.display.getFrame() : frame, showToast, announceToChat, hidden);
        }
        else {
            this.display = new DisplayInfo(itemStack == null ? new ItemStack(Items.DIAMOND) : itemStack, title == null ? defaultTitle : title, description == null ? defaultDescription : description, background == null ? (this.parentId != null ? null: new ResourceLocation( "textures/gui/advancements/stone.png")) : background, frame == null ? FrameType.TASK : frame, showToast, announceToChat, hidden);
        }
        if(this.advancementId == null){
            this.advancementId = new ResourceLocation(MOD_ID, this.display.getTitle().getString().toLowerCase().replace(" ", "_"));
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

    public void reset(){
        if(this.original != null){
            this.display = this.original.getDisplay();
            this.requirements = this.original.getRequirements();
            this.criteria = this.original.getCriteria();
            this.rewards = this.original.getRewards();
            this.advancementId = this.original.getId();
        }
        else {
            this.display = null;
            this.requirements = null;
            this.criteria = null;
            this.rewards = null;
            this.advancementId = null;
        }
        this.builder = Advancement.Builder.advancement();
        this.isSaved = false;
    }


    public void createRewards() {
        AdvancementRewards.Builder rewardsBuilder = new AdvancementRewards.Builder();

        /*rewardsBuilder.addExperience();
        rewardsBuilder.addLootTable();
        rewardsBuilder.addRecipe();
        rewardsBuilder.runs(); //Function*/

        this.rewards = rewardsBuilder.build();
    }


    public void resolveParent() {

    }


    public void createCriteria() {

    }


    public void createRequirements() {

    }


    public void resolveId() {

    }

    public boolean areEqual(){
        if(this.original != null){
            boolean a = this.original.getParent() == null || this.parentId.equals(this.original.getParent().getId());
            //boolean b = this.display.getTitle().equals(this.original.getDisplay().getTitle());
            //boolean c = this.display.getDescription().equals(this.original.getDisplay().getDescription());
            boolean d = this.display.equals(this.original.getDisplay());
            boolean e = this.rewards.equals(this.original.getRewards());
            boolean f = this.criteria.equals(this.original.getCriteria());
            boolean g = Arrays.deepEquals(this.requirements, this.original.getRequirements());

            boolean h = this.advancementId.equals(this.original.getId());

            return a && d && e && f && g;
        }
        return false;
    }

    public void save(){
        if(!areEqual()){
            if(this.parentId != null){
                this.builder.parent(this.parentId);
                this.builder.parent(this.advancements.get(this.parentId));
            }

            if(this.display != null){
                this.builder.display(this.display);
            }

            if(this.rewards == null){
                this.rewards = AdvancementRewards.EMPTY;
                LOGGER.debug("Rewards are null!" + this.rewards);
            }
            this.builder.rewards(this.rewards);

            if(this.criteria != null){
                this.criteria.forEach(builder::addCriterion);
            }

            if(this.requirements != null){
                this.builder.requirements(this.requirements);
            }

            this.isSaved = true;
        }
    }


    public void build() {
        try {
            if(!this.isSaved){
                this.save();
            }
            Advancement finishedAdvancement = this.builder.build(this.advancementId);

            AdvancementHandler.writeAdvancementToFile(finishedAdvancement.getId(), this.builder.serializeToJson());

            LOGGER.info("Created Advancement {} successfully!", finishedAdvancement.getId());
        }
        catch (IllegalStateException | NullPointerException | IOException e){
            LOGGER.error("Couldn't create Advancement {} with Advancement.Builder: {}", this.advancementId, this.builder);
            e.printStackTrace();
        }
    }

    @Override
    public void init() {
        //Edit Box: for Title, Description, id, parentId, if parentID is null : background rs?

        EditBox titleInput = new EditBox(this.font, screenTopLeftCorner[0] + FRAME_WIDTH + PADDING, screenTopLeftCorner[1] + 6, this.display != null ? getFontWidth(this.display.getTitle().getString()) + 10 : EDIT_BOX[0], EDIT_BOX[1], Component.literal("Title"));
        titleInput.setMaxLength(Integer.MAX_VALUE);
        if(this.display != null){
            titleInput.setValue(this.display.getTitle().getString());
        }

        //Title EditButton
        this.addRenderableWidget(new EditButton(screenTopRightCorner[0] - EDIT_BUTTON_LENGTH - PADDING_RIGHT, screenTopRightCorner[1] + PADDING + 1, EDIT_BUTTON_LENGTH -1, EDIT_BUTTON_LENGTH -1, Component.literal("Edit"), pButton -> {
            if(!this.editTitle){
                this.addRenderableWidget(titleInput);
                this.editTitle = true;
                titleInput.visible = true;
            }
            else {
                Component advancementTitle = Component.literal(titleInput.getValue());
                this.updateDisplayInfo(advancementTitle, this.display != null ? this.display.getDescription() : defaultDescription);
                this.editTitle = false;
                titleInput.visible = false;
            }
        }));

        EditBox descriptionInput = new EditBox(this.font, screenTopLeftCorner[0] + PADDING + FRAME_WIDTH/2, screenTopLeftCorner[1] + 26 + PADDING - 2 /* - Half EditBox Offset*/, EDIT_BOX[0], EDIT_BOX[1], Component.literal("Description"));
        descriptionInput.setMaxLength(Integer.MAX_VALUE);
        if(this.display != null){
            descriptionInput.setValue(this.display.getDescription().getString());
        }

        this.addRenderableWidget(new EditButton(screenTopRightCorner[0] - EDIT_BUTTON_LENGTH - PADDING_RIGHT, screenTopRightCorner[1] + PADDING + 1 + 26/2, EDIT_BUTTON_LENGTH, EDIT_BUTTON_LENGTH, Component.literal("Edit"), pButton -> {
            if(!this.editDescription){
                this.addRenderableWidget(descriptionInput);
                this.editDescription = true;
                descriptionInput.visible = true;
            }
            else {
                Component advancementDescription = Component.literal(descriptionInput.getValue());
                this.updateDisplayInfo(this.display != null ? this.display.getTitle() : defaultTitle, advancementDescription);
                this.editDescription = false;
                descriptionInput.visible = false;
            }
        }));

        //ID
        EditBox advancementIdInput = new EditBox(this.font, screenTopLeftCorner[0] + PADDING + FRAME_WIDTH/2 + (this.advancementId != null ? getFontWidth(this.advancementId.getNamespace()) : getFontWidth(MOD_ID)) + getFontWidth(":") + 2, screenTopLeftCorner[1] + 26 + PADDING + textPaddingUp - 2, this.advancementId != null ? getFontWidth(this.advancementId.getPath()) + 10 : EDIT_BOX[0], EDIT_BOX[1], Component.literal("Advancement id"));
        advancementIdInput.setMaxLength(Integer.MAX_VALUE);
        if(this.advancementId != null){
            advancementIdInput.setValue(this.advancementId.getPath());
        }

        this.addRenderableWidget(new EditButton(screenTopRightCorner[0] - EDIT_BUTTON_LENGTH - PADDING_RIGHT, screenTopRightCorner[1] + PADDING + 1 + 26/2 + textPaddingUp, EDIT_BUTTON_LENGTH, EDIT_BUTTON_LENGTH, Component.literal("Edit"), pButton -> {
            if(!this.editId) {
                this.addRenderableWidget(advancementIdInput);
                this.editId = true;
                advancementIdInput.visible = true;
            }
            else {
                try {
                    this.advancementId = new ResourceLocation(this.advancementId != null ? this.advancementId.getNamespace() : MOD_ID, advancementIdInput.getValue());
                }
                catch (ResourceLocationException e){
                    LOGGER.error("Unable to create ResourceLocation with input: " + advancementIdInput.getValue());
                }
                this.editId = false;
                advancementIdInput.visible = false;
            }
        }));

        if(this.parentId == null) {
            EditBox backgroundInput = new EditBox(this.font, screenTopLeftCorner[0] + PADDING + FRAME_WIDTH/2, screenTopLeftCorner[1] + 26 + PADDING + textPaddingUp * 3 -2, EDIT_BOX[0], EDIT_BOX[1], Component.literal("Background Id"));
            backgroundInput.setMaxLength(Integer.MAX_VALUE);
            if(this.display != null && this.display.getBackground() != null){
                backgroundInput.setValue(this.display.getBackground().toString());
            }
            this.addRenderableWidget(new EditButton(screenTopRightCorner[0] - EDIT_BUTTON_LENGTH - PADDING_RIGHT, screenTopRightCorner[1] + PADDING + 1 + 26/2 + textPaddingUp * 3, EDIT_BUTTON_LENGTH, EDIT_BUTTON_LENGTH, Component.literal("Edit"), pButton -> {
                if(!this.editBgId){
                    this.addRenderableWidget(backgroundInput);
                    this.editBgId = true;
                    backgroundInput.visible = true;
                }
                else {
                    try {
                        this.updateDisplayInfo(new ResourceLocation(backgroundInput.getValue()));
                    }
                    catch (ResourceLocationException e){
                        LOGGER.error("Unable to create ResourceLocation with input: " + advancementIdInput.getValue());
                    }
                    this.editBgId = false;
                    backgroundInput.visible = false;
                }
            }));
        }

        int[] mainButton = {98, 20};

        this.addRenderableWidget(new Button(screenBottomLeftCorner[0] + PADDING + FRAME_WIDTH/2, screenBottomLeftCorner[1] - mainButton[1] - 15, mainButton[0], mainButton[1], Component.literal("Save"), button -> {
            this.save();
            this.onClose();
        }));

        this.addRenderableWidget(new Button(screenBottomLeftCorner[0] + PADDING + FRAME_WIDTH/2 + mainButton[0] + 2, screenBottomLeftCorner[1] - mainButton[1] - 15, mainButton[0], mainButton[1], Component.literal("Reset"), button -> {
            this.reset();
        }));

        //Edit Button: Title, Description, id, parentId, rewards, criteria, requirements

        /*this.addRenderableWidget(new EditButton(SCREEN_LEFT_TOP_CORNER[0] + PADDING_LEFT, 130, EDIT_BUTTON_LENGTH, EDIT_BUTTON_LENGTH, Component.literal("Edit"), pButton -> {LOGGER.debug("LOL");}));
        this.addRenderableWidget(new EditButton(SCREEN_LEFT_TOP_CORNER[0] + PADDING_LEFT, 130, EDIT_BUTTON_LENGTH, EDIT_BUTTON_LENGTH, Component.literal("Edit"), pButton -> {LOGGER.debug("LOL");}));
        this.addRenderableWidget(new EditButton(SCREEN_LEFT_TOP_CORNER[0] + PADDING_LEFT, 130, EDIT_BUTTON_LENGTH, EDIT_BUTTON_LENGTH, Component.literal("Edit"), pButton -> {LOGGER.debug("LOL");}));
*/
        super.init();
    }

    @Override
    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        if (this.widget != null){
            int posX = Mth.floor(this.widget.tab.scrollX);
            int posY = Mth.floor(this.widget.tab.scrollY);
            int offsetX = - (posX + this.widget.getX() + AdvancementGeneratorGUI.WINDOW_WIDTH/2 - FakeAdvancementWidget.FRAME_WIDTH - PADDING); //(posX + this.widget.getX() + AdvancementGeneratorGUI.WINDOW_WIDTH/2 - (FakeAdvancementWidget.FRAME_WIDTH + 3));
            //int offsetY = 0;//(posY + this.widget.getY() + AdvancementGeneratorGUI.WINDOW_HEIGHT/2 - 3);
            this.parent.setScreenOffset(offsetX, 0);

            this.parent.setRenderTooltips(false);
            this.parent.render(poseStack, mouseX, mouseY, partialTick);

            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, FakeAdvancementWidget.WIDGETS_LOCATION);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.enableBlend();

            this.renderRectangleFromImage(poseStack, screenTopLeftCorner[0], screenTopLeftCorner[1], screenWidth, screenHeight, 10, 200, 26, 0, 52);
            this.renderRectangleFromImage(poseStack, screenTopLeftCorner[0], screenTopLeftCorner[1], screenWidth, 26, 10, 200, 26, 0, 0);

            this.drawIcon(poseStack, screenTopLeftCorner[0] - 1, screenTopLeftCorner[1], this.display);

            if(!this.editTitle){
                this.minecraft.font.drawShadow(poseStack, this.display != null ? this.display.getTitle() : defaultTitle, screenTopLeftCorner[0] + FRAME_WIDTH + PADDING, screenTopLeftCorner[1] + 9, -1);
            }
            if(!this.editDescription){
                this.minecraft.font.drawShadow(poseStack, this.display != null ? this.display.getDescription() : defaultDescription, screenTopLeftCorner[0] + PADDING + FRAME_WIDTH/2, screenTopLeftCorner[1] + 26 + PADDING, this.display != null ? this.display.getFrame().getChatColor().getColor() : -5592406);
            }
            this.minecraft.font.drawShadow(poseStack, this.advancementId != null ? Component.literal(this.advancementId.getNamespace() + ":") : Component.literal(MOD_ID + ":"), screenTopLeftCorner[0] + PADDING + FRAME_WIDTH/2, screenTopLeftCorner[1] + 26 + PADDING + textPaddingUp, Color.CYAN.getRGB()); //renders resource location
            if(!this.editId){
                this.minecraft.font.drawShadow(poseStack, this.advancementId != null ? Component.literal(this.advancementId.getPath()) : (this.display != null ? Component.literal(this.display.getTitle().getString().toLowerCase().replace(" ", "_")) : Component.empty()), screenTopLeftCorner[0] + PADDING + FRAME_WIDTH/2 + (this.advancementId != null ? getFontWidth(this.advancementId.getNamespace()) : getFontWidth(MOD_ID)) + getFontWidth(":"), screenTopLeftCorner[1] + 26 + PADDING + textPaddingUp, Color.CYAN.getRGB()); //renders resource location
            }
            this.minecraft.font.drawShadow(poseStack, this.parentId != null ? Component.literal(this.parentId.toString()) : Component.empty(), screenTopLeftCorner[0] + PADDING + FRAME_WIDTH/2, screenTopLeftCorner[1] + 26 + PADDING + textPaddingUp * 2, Color.PINK.getRGB()); //renders resource location

            if(!this.editBgId){
                this.minecraft.font.drawShadow(poseStack, this.parentId == null && this.display != null && this.display.getBackground() != null ? Component.literal(this.display.getBackground().toString()) : Component.empty(), screenTopLeftCorner[0] + PADDING + FRAME_WIDTH/2, screenTopLeftCorner[1] + 26 + PADDING + textPaddingUp * 3, Color.blue.getRGB()); //renders resource location
            }

            super.render(poseStack, mouseX, mouseY, partialTick);
        }
        //this.renderDisplayInfo(poseStack);
    }

    protected void drawIcon(PoseStack pPoseStack, int pX, int pY,  DisplayInfo displayInfo){
        if(displayInfo != null){
            this.blit(pPoseStack, pX, pY, displayInfo.getFrame().getTexture(), 128 + AdvancementWidgetType.OBTAINED.getIndex() * FRAME_WIDTH, FRAME_WIDTH, FRAME_WIDTH);
            this.minecraft.getItemRenderer().renderAndDecorateFakeItem(displayInfo.getIcon(), (int) (pX + ICON_X/1.5), pY + ICON_Y);
        }
        else {
            this.blit(pPoseStack, pX, pY, FrameType.TASK.getTexture(), 128 + AdvancementWidgetType.OBTAINED.getIndex() * FRAME_WIDTH, FRAME_WIDTH, FRAME_WIDTH);
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

    @Override
    public void onClose() {
        this.parent.setRenderTooltips(true);
        this.build();
        reloadAll(this.minecraft.getSingleplayerServer());
        this.minecraft.setScreen(this.parent);
        super.onClose();
    }
}
