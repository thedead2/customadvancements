package de.thedead2.customadvancements.client.gui.generator;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import de.thedead2.customadvancements.client.ClientRegistrationHandler;
import de.thedead2.customadvancements.client.gui.BasicInputScreen;
import de.thedead2.customadvancements.client.gui.CriteriaInputScreen;
import de.thedead2.customadvancements.client.gui.FrameAndItemSelectionScreen;
import de.thedead2.customadvancements.client.gui.RewardsInputScreen;
import de.thedead2.customadvancements.client.gui.components.CheckBox;
import de.thedead2.customadvancements.client.gui.components.FakeAdvancementWidget;
import de.thedead2.customadvancements.util.ModHelper;
import de.thedead2.customadvancements.util.handler.AdvancementHandler;
import de.thedead2.customadvancements.util.handler.CrashHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.ResourceLocationException;
import net.minecraft.advancements.*;
import net.minecraft.advancements.critereon.ImpossibleTrigger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.Level;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.IOException;
import java.util.*;

import static de.thedead2.customadvancements.util.ModHelper.*;

public class ClientAdvancementGenerator extends BasicInputScreen {

    private final int textPaddingUp = this.minecraft.font.lineHeight + PADDING + 3;

    public ResourceLocation parentId;
    protected AdvancementRewards rewards;
    protected Map<String, Criterion> criteria = new HashMap<>();
    protected String[][] requirements;

    public ResourceLocation advancementId;
    private final Map<ResourceLocation, Advancement> advancements = new HashMap<>();

    private Advancement original;
    private static final Collection<Advancement> BUILD_ADVANCEMENTS = new HashSet<>();


    public ClientAdvancementGenerator(AdvancementGeneratorGUI parent, Advancement editAdvancement, FakeAdvancementWidget widget) {
        this(parent, editAdvancement.getDisplay(), editAdvancement.getParent() != null ? editAdvancement.getParent().getId() : null, widget);
        this.requirements = editAdvancement.getRequirements();
        this.criteria = editAdvancement.getCriteria();
        this.rewards = editAdvancement.getRewards();
        this.advancementId = editAdvancement.getId(); // Implemented
        this.original = editAdvancement;
        loginfos();
    }

    public ClientAdvancementGenerator(AdvancementGeneratorGUI parent, DisplayInfo display, ResourceLocation parentId, FakeAdvancementWidget widget) {
        super(null, parent, display, widget, () -> Minecraft.getInstance().screen.width - Minecraft.getInstance().screen.width/3 - 2, () -> Minecraft.getInstance().screen.height - 2, () -> new int[]{Minecraft.getInstance().screen.width / 3, 2});
        this.parentId = parentId; //Implemented
        this.minecraft.getSingleplayerServer().getAdvancements().getAllAdvancements().forEach(advancement -> this.advancements.put(advancement.getId(), advancement));

        if(this.advancementId == null){
            loginfos();
        }

    }

    public static void saveBuildAdvancements() {
        BUILD_ADVANCEMENTS.forEach(advancement -> {
            try {
                AdvancementHandler.writeAdvancementToFile(advancement);
            }
            catch (IOException e) {
                ModHelper.LOGGER.error("Unable to write advancement {} to file!", advancement.getId());
                CrashHandler.getInstance().addCrashDetails("Unable to write advancement to file!", Level.ERROR, e);
                e.printStackTrace();
            }
        });
        BUILD_ADVANCEMENTS.clear();
        reloadAll(Minecraft.getInstance().getSingleplayerServer());
    }

    private void loginfos(){
        LOGGER.debug("requirements: " + Arrays.deepToString(this.requirements));
        LOGGER.debug("criteria: " + this.criteria);
        LOGGER.debug("rewards: " + this.rewards);
    }

    @Override
    public void updateDisplayInfo(ItemStack itemStack, Component title, Component description, ResourceLocation background, FrameType frame, boolean showToast, boolean announceToChat, boolean hidden) {
        super.updateDisplayInfo(itemStack, title, description, background, frame, showToast, announceToChat, hidden);
        if(this.advancementId == null){
            this.advancementId = new ResourceLocation(MOD_ID, this.display.getTitle().getString().toLowerCase().replace(" ", "_"));
        }
        this.mainGui.updateAdvancementDisplay(this.advancementId, this.parentId, this.display);
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
            this.minecraft.setScreen(new FrameAndItemSelectionScreen(this, this.mainGui, this.display, this.widget, this.screenWidthSupplier, this.screenHeightSupplier, this.screenStartPositionSupplier));
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
            this.criteria = new HashMap<>();
            this.rewards = null;
            this.advancementId = null;
        }
        this.checkBoxes.forEach((s, checkBox) -> checkBox.reset());
    }




    public void addRewards(AdvancementRewards rewards) {
        this.rewards = rewards;
        LOGGER.debug(this.rewards);
    }


    @Override
    public void save(){
        Advancement.Builder builder = Advancement.Builder.advancement();
        this.editBoxes.values().forEach(editBoxWithEditButton -> {
            if(editBoxWithEditButton.editBox().isVisible()){
                editBoxWithEditButton.editButton().onPress();
            }
        });
        if(this.parentId != null){
            builder.parent(this.parentId);
            builder.parent(this.advancements.get(this.parentId));
        }

        if(this.display != null){
            builder.display(this.display);

            if(this.advancementId == null){
                this.advancementId = new ResourceLocation(MOD_ID, this.display.getTitle().getString().toLowerCase().replace(" ", "_"));
            }
        }

        if(this.rewards != null){
            builder.rewards(this.rewards);
        }

        if(!this.criteria.isEmpty()){
            this.criteria.forEach(builder::addCriterion);
        }
        else {
            builder.addCriterion("impossible", new ImpossibleTrigger.TriggerInstance());
        }

        if(this.requirements != null){
            builder.requirements(this.requirements);
        }

        if(this.advancementId == null){
            LOGGER.error("Couldn't save advancement with unknown Id!");
            this.onClose();
            return;
        }
        this.build(builder);
        super.save();
    }


    public void build(Advancement.Builder builder) {
        try {
            Advancement finishedAdvancement = builder.build(this.advancementId);

            this.advancements.forEach((resourceLocation, advancement) -> {
                if(!finishedAdvancement.equals(advancement)){
                    LOGGER.error("Duplicate Advancements: {} | {} ", finishedAdvancement.getId(), advancement.getId());
                    return;
                }
            });

            if(!BUILD_ADVANCEMENTS.add(finishedAdvancement)){
                BUILD_ADVANCEMENTS.remove(finishedAdvancement);
                BUILD_ADVANCEMENTS.add(finishedAdvancement);
            }

            LOGGER.info("Created Advancement {} successfully!", finishedAdvancement.getId());
        }
        catch (NullPointerException e){
            LOGGER.error("Couldn't create Advancement {} with Advancement.Builder: {}", this.advancementId, builder);
            CrashHandler.getInstance().addCrashDetails("Couldn't create advancement from advancement builder!", Level.WARN, e);
            e.printStackTrace();
        }
    }

    public void initEditBoxes(){
        this.addEditBox(screenTopLeftCorner[0] + FRAME_LENGTH + PADDING, screenTopLeftCorner[1] + 6, "title", () -> this.display.getTitle().getString(), Color.white::getRGB, (editBox) -> {
            Component advancementTitle = Component.literal(editBox.getValue());
            this.updateDisplayInfo(advancementTitle, this.display != null ? this.display.getDescription() : DEFAULT_DESCRIPTION);
        });
        this.addEditBox(screenTopLeftCorner[0] + PADDING + FRAME_LENGTH /2, screenTopLeftCorner[1] + 26 + PADDING - 2, "description", () -> this.display.getDescription().getString(), () -> this.display != null ? this.display.getFrame().getChatColor().getColor() : Integer.valueOf(-5592406), (editBox) -> {
            Component advancementDescription = Component.literal(editBox.getValue());
            this.updateDisplayInfo(this.display != null ? this.display.getTitle() : DEFAULT_TITLE, advancementDescription);
        });
        if(this.original == null){
            this.addEditBox(screenTopLeftCorner[0] + PADDING + FRAME_LENGTH /2 + (this.advancementId != null ? getFontWidth(this.advancementId.getNamespace()) : getFontWidth(MOD_ID)) + getFontWidth(":") + 2, screenTopLeftCorner[1] + 26 + PADDING + textPaddingUp - 2, "id", () -> this.advancementId.getPath(), Color.CYAN::getRGB, (editBox) -> {
                try {
                    this.advancementId = new ResourceLocation(this.advancementId != null ? this.advancementId.getNamespace() : MOD_ID, editBox.getValue());
                }
                catch (ResourceLocationException e){
                    LOGGER.error("Unable to create ResourceLocation with input: " + editBox.getValue());
                }
            });
        }
        if(this.parentId == null) {
            this.addEditBox(screenTopLeftCorner[0] + PADDING + FRAME_LENGTH /2, screenTopLeftCorner[1] + 26 + PADDING + textPaddingUp * 3 -2, "bgId", () -> this.display.getBackground().toString(), Color.pink::getRGB, (editBox) -> {
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
        CheckBox showToast = new CheckBox(screenTopLeftCorner[0] + PADDING + FRAME_LENGTH /2, screenTopLeftCorner[1] + 26 + PADDING + textPaddingUp * 4, checkbox, checkbox, Component.literal("Show Toast:") ,this.display == null || this.display.shouldShowToast(), checkBox -> {
            if (this.display != null) {
                this.updateDisplayInfo(checkBox.getValue(), this.display.shouldAnnounceChat(), this.display.isHidden());
            }
            else {
                this.updateDisplayInfo(checkBox.getValue(), true, false);
            }
        });
        CheckBox announceToChat = new CheckBox(screenTopLeftCorner[0] + PADDING + FRAME_LENGTH /2 + showToast.getWidth() + 6, screenTopLeftCorner[1] + 26 + PADDING + textPaddingUp * 4, checkbox, checkbox, Component.literal("Announce to Chat:"), this.display == null || this.display.shouldAnnounceChat(), checkBox -> {
            if(this.display != null){
                this.updateDisplayInfo(this.display.shouldShowToast(), checkBox.getValue(), this.display.isHidden());
            }
            else {
                this.updateDisplayInfo(true, checkBox.getValue(), false);
            }
        });
        int xPos = screenTopLeftCorner[0] + PADDING + FRAME_LENGTH /2 + showToast.getWidth() + announceToChat.getWidth() + 12;
        int yPos = screenTopLeftCorner[1] + 26 + PADDING + textPaddingUp * 4;

        if((xPos + checkbox + getFontWidth("Is Hidden:") + 1) > screenTopRightCorner[0]) {
            xPos = screenTopLeftCorner[0] + PADDING + FRAME_LENGTH /2;
            yPos = (int) (screenTopLeftCorner[1] + 26 + PADDING + textPaddingUp * 5.25f);
        }

        CheckBox isHidden = new CheckBox(xPos, yPos, checkbox, checkbox, Component.literal("Is Hidden:"), this.display != null && this.display.isHidden(), checkBox -> {
            if(this.display != null){
                this.updateDisplayInfo(this.display.shouldShowToast(), this.display.shouldAnnounceChat(), checkBox.getValue());
            }
            else {
                this.updateDisplayInfo(true, true, checkBox.getValue());
            }
        });
        this.addCheckBox("showToast", showToast);
        this.addCheckBox("announceToChat",announceToChat);
        this.addCheckBox("isHidden", isHidden);
    }


    public void initAdditionalButtons(){
        int[] mainButton = {this.screenWidth/2 - (FRAME_LENGTH /2) + 2 - 5, 20};
        int yPos = this.screenTopLeftCorner[1] + this.checkBoxes.get("isHidden").y + 25;

        Button criteriaButton = new Button(screenTopLeftCorner[0] + PADDING + FRAME_LENGTH /2, yPos, mainButton[0], mainButton[1], Component.literal("Edit Criteria"), button -> this.minecraft.setScreen(new CriteriaInputScreen(this, this.mainGui, this.display, this.widget, this.screenWidthSupplier, this.screenHeightSupplier, this.screenStartPositionSupplier)));
        this.mainButtons.put("criteria", criteriaButton);
        this.addRenderableWidget(criteriaButton);

        Button rewardsButton = new Button(screenBottomLeftCorner[0] + PADDING + FRAME_LENGTH /2 + (mainButton[0] + 2), yPos, mainButton[0], mainButton[1], Component.literal("Edit Rewards"), button -> this.minecraft.setScreen(this.rewards != null ? new RewardsInputScreen(this, this.mainGui, this.display, this.widget, this.screenWidthSupplier, this.screenHeightSupplier, this.screenStartPositionSupplier, this.rewards) : new RewardsInputScreen(this, this.mainGui, this.display, this.widget, this.screenWidthSupplier, this.screenHeightSupplier, this.screenStartPositionSupplier)));
        this.mainButtons.put("rewards", rewardsButton);
        this.addRenderableWidget(rewardsButton);
    }


    @Override
    public void init() {
        CrashHandler.getInstance().setActiveAdvancement(this.original);
        super.init();
        this.initEditBoxes();
        this.initCheckBoxes();
        this.initAdditionalButtons();
    }

    @Override
    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        super.render(poseStack, mouseX, mouseY, partialTick);
        if(this.advancementId == null || this.criteria.isEmpty()){
            Button saveButton = this.mainButtons.get("save");
            saveButton.active = false;
        }
        int yPos = this.checkBoxes.get("isHidden").y + 15;
        this.hLine(poseStack, this.screenTopLeftCorner[0] + PADDING_RIGHT,this.screenTopRightCorner[0] - PADDING_RIGHT, yPos, Color.DARK_GRAY.getRGB());
        this.renderTextComponents(poseStack);
    }

    public void renderTextComponents(PoseStack poseStack){
        /*if(!this.editBoxes.get("title").editBox().isVisible()){
            this.minecraft.font.drawShadow(poseStack, this.display != null ? this.display.getTitle() : DEFAULT_TITLE, screenTopLeftCorner[0] + FRAME_LENGTH + PADDING, screenTopLeftCorner[1] + 9, -1);
        }
        if(!this.editBoxes.get("description").editBox().isVisible()){
            this.minecraft.font.drawShadow(poseStack, this.display != null ? this.display.getDescription() : DEFAULT_DESCRIPTION, screenTopLeftCorner[0] + PADDING + FRAME_LENGTH /2, screenTopLeftCorner[1] + 26 + PADDING, this.display != null ? this.display.getFrame().getChatColor().getColor() : -5592406);
        }*/

        this.minecraft.font.drawShadow(poseStack, this.advancementId != null ? Component.literal(this.advancementId.getNamespace() + ":") : Component.literal(MOD_ID + ":"), screenTopLeftCorner[0] + PADDING + FRAME_LENGTH /2, screenTopLeftCorner[1] + 26 + PADDING + textPaddingUp, Color.CYAN.getRGB()); //renders resource location

        if(this.editBoxes.get("id") == null){
            this.minecraft.font.drawShadow(poseStack, this.advancementId != null ? Component.literal(this.advancementId.getPath()) : (this.display != null ? Component.literal(this.display.getTitle().getString().toLowerCase().replace(" ", "_")) : Component.empty()), screenTopLeftCorner[0] + PADDING + FRAME_LENGTH /2 + (this.advancementId != null ? getFontWidth(this.advancementId.getNamespace()) : getFontWidth(MOD_ID)) + getFontWidth(":"), screenTopLeftCorner[1] + 26 + PADDING + textPaddingUp, Color.CYAN.getRGB()); //renders resource location
        }
        this.minecraft.font.drawShadow(poseStack, this.parentId != null ? Component.literal(this.parentId.toString()) : Component.empty(), screenTopLeftCorner[0] + PADDING + FRAME_LENGTH /2, screenTopLeftCorner[1] + 26 + PADDING + textPaddingUp * 2, Color.PINK.getRGB()); //renders resource location

        if(this.editBoxes.get("bgId") != null && !this.editBoxes.get("bgId").editBox().isVisible()){
            if(this.display != null && this.display.getBackground() != null) {
                ResourceLocation resourcelocation = this.display.getBackground();
                RenderSystem.setShader(GameRenderer::getPositionTexShader);
                RenderSystem.setShaderTexture(0, Objects.requireNonNullElse(resourcelocation, TextureManager.INTENTIONAL_MISSING_TEXTURE));
                RenderSystem.defaultBlendFunc();
                blit(poseStack, screenTopLeftCorner[0] + PADDING + FRAME_LENGTH /2, (int) (screenTopLeftCorner[1] + 26 + PADDING + textPaddingUp * 2.75), 0.0F, 0.0F, 16, 16, 16, 16);
                //this.minecraft.font.drawShadow(poseStack, Component.literal(this.display.getBackground().toString()), screenTopLeftCorner[0] + PADDING + FRAME_LENGTH /2 + 18, screenTopLeftCorner[1] + 26 + PADDING + textPaddingUp * 3, Color.WHITE.getRGB()); //renders resource location
            }
        }
    }

    @Override
    public void onMainButtonTooltip(String buttonId, PoseStack poseStack, int mouseX, int mouseY) {
        if(this.mainButtons.get(buttonId) != null && buttonId.equals("save")){
            if (this.advancementId == null) {
                this.renderTooltip(poseStack, Component.literal("Can't save advancement with unknown id!").withStyle(ChatFormatting.DARK_GRAY), mouseX, mouseY);
            }
            else if (this.criteria.isEmpty()) {
                this.renderTooltip(poseStack, Component.literal("Can't save advancement with unknown criteria!").withStyle(ChatFormatting.RED), mouseX, mouseY);
            }
        }
    }

    @Override
    public void onClose() {
        this.mainGui.updateAdvancements(ClientRegistrationHandler.getFakeAdvancements(BUILD_ADVANCEMENTS), null);
        this.mainGui.setRenderTooltips(true);
        this.minecraft.setScreen(this.mainGui);
        CrashHandler.getInstance().setActiveAdvancement(null);
    }
}
