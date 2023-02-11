package de.thedead2.customadvancements.client.gui.generator;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import de.thedead2.customadvancements.client.gui.BasicInputScreen;
import de.thedead2.customadvancements.client.gui.FrameAndItemSelectionScreen;
import de.thedead2.customadvancements.client.gui.components.CheckBox;
import de.thedead2.customadvancements.client.gui.components.FakeAdvancementWidget;
import de.thedead2.customadvancements.util.handler.AdvancementHandler;
import de.thedead2.customadvancements.util.handler.CrashHandler;
import net.minecraft.ResourceLocationException;
import net.minecraft.advancements.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.Level;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static de.thedead2.customadvancements.util.ModHelper.*;

public class ClientAdvancementGenerator extends BasicInputScreen {

    private final int textPaddingUp = this.minecraft.font.lineHeight + PADDING;

    protected Advancement.Builder builder;
    protected ResourceLocation parentId;
    protected AdvancementRewards rewards;
    protected Map<String, Criterion> criteria;
    protected String[][] requirements;

    protected ResourceLocation advancementId;
    private final Map<ResourceLocation, Advancement> advancements = new HashMap<>();
    private boolean isSaved = false;

    private Advancement original;


    public ClientAdvancementGenerator(AdvancementGeneratorGUI parent, Advancement editAdvancement, FakeAdvancementWidget widget) {
        this(parent, editAdvancement.getDisplay(), editAdvancement.getParent() != null ? editAdvancement.getParent().getId() : null, widget);
        this.requirements = editAdvancement.getRequirements();
        this.criteria = editAdvancement.getCriteria();
        this.rewards = editAdvancement.getRewards();
        this.advancementId = editAdvancement.getId();
        this.original = editAdvancement;
        loginfos();
    }

    public ClientAdvancementGenerator(AdvancementGeneratorGUI parent, DisplayInfo display, ResourceLocation parentId, FakeAdvancementWidget widget) {
        super(parent, display, widget, () -> Minecraft.getInstance().screen.width - Minecraft.getInstance().screen.width/3 - 2, () -> Minecraft.getInstance().screen.height - 2, () -> new int[]{Minecraft.getInstance().screen.width / 3, 2});
        this.builder = Advancement.Builder.advancement();
        this.parentId = parentId;
        this.minecraft.getSingleplayerServer().getAdvancements().getAllAdvancements().forEach(advancement -> this.advancements.put(advancement.getId(), advancement));

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

    @Override
    public void updateDisplayInfo(ItemStack itemStack, Component title, Component description, ResourceLocation background, FrameType frame, boolean showToast, boolean announceToChat, boolean hidden) {
        super.updateDisplayInfo(itemStack, title, description, background, frame, showToast, announceToChat, hidden);
        if(this.advancementId == null){
            this.advancementId = new ResourceLocation(MOD_ID, this.display.getTitle().getString().toLowerCase().replace(" ", "_"));
        }
    }

    public boolean isMouseOverFrame(double mouseX, double mouseY){
        int leftXCorner = this.framePos[0];
        int rightXCorner = leftXCorner + FRAME_LENGTH;
        int topYCorner = this.framePos[1];
        int bottomYCorner = topYCorner + FRAME_LENGTH;

        return mouseX >= leftXCorner && mouseX <= rightXCorner && mouseY >= topYCorner && mouseY <= bottomYCorner;
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        if(this.isMouseOverFrame(pMouseX, pMouseY)){
            this.minecraft.setScreen(new FrameAndItemSelectionScreen(this, this.display, this.widget, () -> this.screenWidth, () -> this.screenHeight, () -> this.screenTopLeftCorner));
        }
        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }

    @Override
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
        this.checkBoxes.forEach(CheckBox::reset);
    }




    public void createRewards() {
        AdvancementRewards.Builder rewardsBuilder = new AdvancementRewards.Builder();

        /*rewardsBuilder.addExperience();
        rewardsBuilder.addLootTable();
        rewardsBuilder.addRecipe();
        rewardsBuilder.runs(); //Function*/

        this.rewards = rewardsBuilder.build();
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

    @Override
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
            this.build();
            reloadAll(this.minecraft.getSingleplayerServer());
            this.onClose();
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
            CrashHandler.getInstance().addCrashDetails("Couldn't create advancement from advancement builder!", Level.WARN, e);
            e.printStackTrace();
        }
    }

    public void initEditBoxes(){
        this.addEditBox(screenTopLeftCorner[0] + FRAME_LENGTH + PADDING, screenTopLeftCorner[1] + 6, "title", () -> this.display.getTitle().getString(), ((editBox) -> {
            Component advancementTitle = Component.literal(editBox.getValue());
            this.updateDisplayInfo(advancementTitle, this.display != null ? this.display.getDescription() : DEFAULT_DESCRIPTION);
        }));
        this.addEditBox(screenTopLeftCorner[0] + PADDING + FRAME_LENGTH /2, screenTopLeftCorner[1] + 26 + PADDING - 2, "description", () -> this.display.getDescription().getString(), (editBox) -> {
            Component advancementDescription = Component.literal(editBox.getValue());
            this.updateDisplayInfo(this.display != null ? this.display.getTitle() : DEFAULT_TITLE, advancementDescription);
        });
        this.addEditBox(screenTopLeftCorner[0] + PADDING + FRAME_LENGTH /2 + (this.advancementId != null ? getFontWidth(this.advancementId.getNamespace()) : getFontWidth(MOD_ID)) + getFontWidth(":") + 2, screenTopLeftCorner[1] + 26 + PADDING + textPaddingUp - 2, "id", () -> this.advancementId.getPath(), (editBox) -> {
            try {
                this.advancementId = new ResourceLocation(this.advancementId != null ? this.advancementId.getNamespace() : MOD_ID, editBox.getValue());
            }
            catch (ResourceLocationException e){
                LOGGER.error("Unable to create ResourceLocation with input: " + editBox.getValue());
            }
        });
        if(this.parentId == null) {
            this.addEditBox(screenTopLeftCorner[0] + PADDING + FRAME_LENGTH /2, screenTopLeftCorner[1] + 26 + PADDING + textPaddingUp * 3 -2, "bgId", () -> this.display.getBackground().toString(), (editBox) -> {
                try {
                    this.updateDisplayInfo(new ResourceLocation(editBox.getValue()));
                }
                catch (ResourceLocationException e){
                    LOGGER.error("Unable to create ResourceLocation with input: " + editBox.getValue());
                }
            });
        }
    }


    public void initCheckBoxes(){
        int checkbox = 15;

        this.addCheckBox(new CheckBox(screenTopLeftCorner[0] + PADDING + FRAME_LENGTH /2, screenTopLeftCorner[1] + 26 + PADDING + textPaddingUp * 4, checkbox, checkbox, Component.empty() ,this.display == null || this.display.shouldShowToast(), checkBox -> {
            if (this.display != null) {
                this.updateDisplayInfo(checkBox.getValue(), this.display.shouldAnnounceChat(), this.display.isHidden());
            }
            else {
                this.updateDisplayInfo(checkBox.getValue(), true, false);
            }
        }));
        this.addCheckBox(new CheckBox(screenTopLeftCorner[0] + PADDING + FRAME_LENGTH /2 + checkbox + 2, screenTopLeftCorner[1] + 26 + PADDING + textPaddingUp * 4, checkbox, checkbox, Component.empty(), this.display == null || this.display.shouldAnnounceChat(), checkBox -> {
            if(this.display != null){
                this.updateDisplayInfo(this.display.shouldShowToast(), checkBox.getValue(), this.display.isHidden());
            }
            else {
                this.updateDisplayInfo(true, checkBox.getValue(), false);
            }
        }));
        this.addCheckBox(new CheckBox(screenTopLeftCorner[0] + PADDING + FRAME_LENGTH /2 + checkbox*2 + 4, screenTopLeftCorner[1] + 26 + PADDING + textPaddingUp * 4, checkbox, checkbox, Component.empty(), this.display != null && this.display.isHidden(), checkBox -> {
            if(this.display != null){
                this.updateDisplayInfo(this.display.shouldShowToast(), this.display.shouldAnnounceChat(), checkBox.getValue());
            }
            else {
                this.updateDisplayInfo(true, true, checkBox.getValue());
            }
        }));
    }


    @Override
    public void init() {
        super.init();
        this.initEditBoxes();
        this.initCheckBoxes();
    }

    @Override
    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        super.render(poseStack, mouseX, mouseY, partialTick);
        this.renderTextComponents(poseStack);
    }

    public void renderTextComponents(PoseStack poseStack){
        if(!this.editBoxes.get("title").isVisible()){
            this.minecraft.font.drawShadow(poseStack, this.display != null ? this.display.getTitle() : DEFAULT_TITLE, screenTopLeftCorner[0] + FRAME_LENGTH + PADDING, screenTopLeftCorner[1] + 9, -1);
        }
        if(!this.editBoxes.get("description").isVisible()){
            this.minecraft.font.drawShadow(poseStack, this.display != null ? this.display.getDescription() : DEFAULT_DESCRIPTION, screenTopLeftCorner[0] + PADDING + FRAME_LENGTH /2, screenTopLeftCorner[1] + 26 + PADDING, this.display != null ? this.display.getFrame().getChatColor().getColor() : -5592406);
        }

        this.minecraft.font.drawShadow(poseStack, this.advancementId != null ? Component.literal(this.advancementId.getNamespace() + ":") : Component.literal(MOD_ID + ":"), screenTopLeftCorner[0] + PADDING + FRAME_LENGTH /2, screenTopLeftCorner[1] + 26 + PADDING + textPaddingUp, Color.CYAN.getRGB()); //renders resource location

        if(!this.editBoxes.get("id").isVisible()){
            this.minecraft.font.drawShadow(poseStack, this.advancementId != null ? Component.literal(this.advancementId.getPath()) : (this.display != null ? Component.literal(this.display.getTitle().getString().toLowerCase().replace(" ", "_")) : Component.empty()), screenTopLeftCorner[0] + PADDING + FRAME_LENGTH /2 + (this.advancementId != null ? getFontWidth(this.advancementId.getNamespace()) : getFontWidth(MOD_ID)) + getFontWidth(":"), screenTopLeftCorner[1] + 26 + PADDING + textPaddingUp, Color.CYAN.getRGB()); //renders resource location
        }
        this.minecraft.font.drawShadow(poseStack, this.parentId != null ? Component.literal(this.parentId.toString()) : Component.empty(), screenTopLeftCorner[0] + PADDING + FRAME_LENGTH /2, screenTopLeftCorner[1] + 26 + PADDING + textPaddingUp * 2, Color.PINK.getRGB()); //renders resource location

        if(this.editBoxes.get("bgId") != null && !this.editBoxes.get("bgId").isVisible()){
            this.minecraft.font.drawShadow(poseStack, this.parentId == null && this.display != null && this.display.getBackground() != null ? Component.literal(this.display.getBackground().toString()) : Component.empty(), screenTopLeftCorner[0] + PADDING + FRAME_LENGTH /2, screenTopLeftCorner[1] + 26 + PADDING + textPaddingUp * 3, Color.blue.getRGB()); //renders resource location
            if(this.display != null && this.display.getBackground() != null) {
                RenderSystem.setShader(GameRenderer::getPositionTexShader);
                RenderSystem.setShaderTexture(0, this.display.getBackground());
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                RenderSystem.enableBlend();
                //this.blit(poseStack,screenTopLeftCorner[0] + PADDING + FRAME_LENGTH /2, screenTopLeftCorner[1] + 26 + PADDING + textPaddingUp * 3, 0, 0, 25, 25);
            }
        }
    }


    @Override
    public void onClose() {
        super.onClose();
    }
}
