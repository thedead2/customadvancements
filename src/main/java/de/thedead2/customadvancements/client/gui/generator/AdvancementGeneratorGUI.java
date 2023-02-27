package de.thedead2.customadvancements.client.gui.generator;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import de.thedead2.customadvancements.client.ClientRegistrationHandler;
import de.thedead2.customadvancements.client.gui.components.FakeAdvancementTab;
import de.thedead2.customadvancements.client.gui.components.FakeAdvancementWidget;
import de.thedead2.customadvancements.client.gui.generator.ClientAdvancementGenerator;
import de.thedead2.customadvancements.util.ModHelper;
import de.thedead2.customadvancements.util.handler.AdvancementHandler;
import de.thedead2.customadvancements.util.handler.CrashHandler;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.advancements.AdvancementTabType;
import net.minecraft.client.multiplayer.ClientAdvancements;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundSeenAdvancementsPacket;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.Level;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.*;

import static de.thedead2.customadvancements.util.ModHelper.MOD_ID;
import static de.thedead2.customadvancements.util.ModHelper.reloadAll;

public class AdvancementGeneratorGUI extends Screen implements ClientAdvancements.Listener {

    //First Screen
    private static final ResourceLocation WINDOW_LOCATION = new ResourceLocation(ModHelper.MOD_ID, "textures/gui/advancements/window.png");
    private static final ResourceLocation TABS_LOCATION = new ResourceLocation(ModHelper.MOD_ID, "textures/gui/advancements/tabs.png");
    public static final int WINDOW_WIDTH = 252;
    public static final int WINDOW_HEIGHT = 140;
    private static final int WINDOW_INSIDE_X = 9;
    private static final int WINDOW_INSIDE_Y = 18;
    public static final int WINDOW_INSIDE_WIDTH = 234;
    public static final int WINDOW_INSIDE_HEIGHT = 113;
    private static final int WINDOW_TITLE_X = 8;
    private static final int WINDOW_TITLE_Y = 6;
    public static final int BACKGROUND_TILE_WIDTH = 16;
    public static final int BACKGROUND_TILE_HEIGHT = 16;
    public static final int BACKGROUND_TILE_COUNT_X = 14;
    public static final int BACKGROUND_TILE_COUNT_Y = 7;
    public int screenOffsetX;
    public int screenOffsetY;
    private static final Component TITLE = Component.translatable("gui.customadvancements.advancement_generator.title");
    private ClientAdvancements advancements;
    private final Map<Advancement, FakeAdvancementTab> tabs = Maps.newLinkedHashMap();
    @Nullable
    private FakeAdvancementTab selectedTab;
    private boolean isScrolling;
    private static int tabPage, maxPages;

    public boolean renderTooltips = true;


    private final Screen parent;

    public AdvancementGeneratorGUI(ClientAdvancements advancementsIn, Screen parent, Minecraft minecraft) {
        super(TITLE);
        this.advancements = advancementsIn;
        this.parent = parent;
        this.minecraft = minecraft;
    }


    @Override
    public void init() {
        this.tabs.clear();
        this.selectedTab = null;
        this.advancements.setListener(this);
        this.screenOffsetX = (this.width - WINDOW_WIDTH) / 2;
        this.screenOffsetY = (this.height - WINDOW_HEIGHT) / 2;

        if (this.selectedTab == null && !this.tabs.isEmpty()) {
            this.advancements.setSelectedTab(this.tabs.values().iterator().next().getAdvancement(), true);
        }
        else {
            this.advancements.setSelectedTab(this.selectedTab == null ? null : this.selectedTab.getAdvancement(), true);
        }

        if (this.tabs.size() > AdvancementTabType.MAX_TABS) {
            addRenderableWidget(new net.minecraft.client.gui.components.Button(this.screenOffsetX,            this.screenOffsetY - 50, 20, 20, Component.literal("<"), b -> tabPage = Math.max(tabPage - 1, 0)));
            addRenderableWidget(new net.minecraft.client.gui.components.Button(this.screenOffsetX + WINDOW_WIDTH - 20, this.screenOffsetY - 50, 20, 20, Component.literal(">"), b -> tabPage = Math.min(tabPage + 1, maxPages)));
            maxPages = this.tabs.size() / AdvancementTabType.MAX_TABS;
        }
    }


    @Override
    public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        this.renderBackground(pPoseStack);

        if (maxPages != 0) {
            Component page = Component.literal(String.format("%d / %d", tabPage + 1, maxPages + 1));
            int width = this.font.width(page);
            this.font.drawShadow(pPoseStack, page.getVisualOrderText(), this.screenOffsetX + (252 / 2) - (width / 2), this.screenOffsetY - 44, -1);
        }

        this.renderInside(pPoseStack, pMouseX, pMouseY, this.screenOffsetX, this.screenOffsetY);
        this.renderWindow(pPoseStack, this.screenOffsetX, this.screenOffsetY);
        if(renderTooltips){
            this.renderTooltips(pPoseStack, pMouseX, pMouseY, this.screenOffsetX, this.screenOffsetY, pPartialTick);
        }
    }


    private void renderInside(PoseStack pPoseStack, int pMouseX, int pMouseY, int offsetX, int offsetY) {
        FakeAdvancementTab advancementtab = this.selectedTab;
        if (advancementtab != null) {
            PoseStack posestack = RenderSystem.getModelViewStack();
            posestack.pushPose();
            posestack.translate(offsetX + 9, offsetY + 18, 0.0D);
            RenderSystem.applyModelViewMatrix();
            advancementtab.drawContents(pPoseStack);
            posestack.popPose();
            RenderSystem.applyModelViewMatrix();
            RenderSystem.depthFunc(515);
            RenderSystem.disableDepthTest();
        }
        else {
            throw new RuntimeException("Unable to create Advancement Generator GUI as there are no advancements to display! That was unexpected!");
        }
    }


    public void renderWindow(PoseStack pPoseStack, int offsetX, int offsetY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, WINDOW_LOCATION);
        this.blit(pPoseStack, offsetX, offsetY, 0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
        if (this.tabs.size() > 1) {
            RenderSystem.setShaderTexture(0, TABS_LOCATION);
            for(FakeAdvancementTab advancementtab : this.tabs.values()) {
                if (advancementtab.getPage() == tabPage){
                    advancementtab.drawTab(pPoseStack, offsetX, offsetY, advancementtab == this.selectedTab);
                }
            }

            RenderSystem.defaultBlendFunc();

            for(FakeAdvancementTab advancementtab1 : this.tabs.values()) {
                if (advancementtab1.getPage() == tabPage){
                    if(advancementtab1.getAdvancement().getId().toString().equals(ModHelper.MOD_ID + ":" + "fake_root_advancement")){
                        advancementtab1.drawRandomIcon(offsetX, offsetY, this.itemRenderer);
                    }
                    else {
                        advancementtab1.drawIcon(offsetX, offsetY, this.itemRenderer);
                    }
                }
            }
            RenderSystem.disableBlend();
        }

        this.font.draw(pPoseStack, TITLE, (float)(offsetX + 8), (float)(offsetY + 6), 4210752);
    }


    private void renderTooltips(PoseStack pPoseStack, int pMouseX, int pMouseY, int offsetX, int offsetY, float pPartialTick) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        if (this.selectedTab != null) {
            PoseStack posestack = RenderSystem.getModelViewStack();
            posestack.pushPose();
            posestack.translate(offsetX + 9, offsetY + 18, 400.0D);
            RenderSystem.applyModelViewMatrix();
            RenderSystem.enableDepthTest();
            this.selectedTab.drawTooltips(pPoseStack, pMouseX - offsetX - 9, pMouseY - offsetY - 18, offsetX, offsetY, pPartialTick);
            RenderSystem.disableDepthTest();
            posestack.popPose();
            RenderSystem.applyModelViewMatrix();
        }

        if (this.tabs.size() > 1) {
            for(FakeAdvancementTab advancementtab : this.tabs.values()) {
                if (advancementtab.getPage() == tabPage && advancementtab.isMouseOver(offsetX, offsetY, pMouseX, pMouseY) && advancementtab.getActiveWidget() == null) {
                    this.renderTooltip(pPoseStack, advancementtab.getTitle(), pMouseX, pMouseY);
                }
            }
        }
    }


    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        int offsetX = (this.width - WINDOW_WIDTH) / 2;
        int offsetY = (this.height - WINDOW_HEIGHT) / 2;

        if (pButton == 0) {
            for(FakeAdvancementTab advancementtab : this.tabs.values()) {
                if (advancementtab.getPage() == tabPage && advancementtab.isMouseOver(offsetX, offsetY, pMouseX, pMouseY)) {
                    if(advancementtab.getAdvancement().getId().toString().equals(ModHelper.MOD_ID + ":" + "fake_root_advancement")){
                        this.advancements.setSelectedTab(advancementtab.getAdvancement(), true);
                        this.minecraft.setScreen(new ClientAdvancementGenerator(this, null, null, advancementtab.getWidget(advancementtab.getAdvancement())));
                    }
                    else {
                        this.advancements.setSelectedTab(advancementtab.getAdvancement(), true);
                    }
                    break;
                }
            }
        }

        double buttonMouseX = pMouseX - offsetX - 9;
        double buttonMouseY = pMouseY - offsetY - 18;
        if (this.selectedTab.getActiveWidget() != null && this.selectedTab.getActiveWidget().editButton != null && this.selectedTab.getActiveWidget().editButton.isMouseOver(buttonMouseX, buttonMouseY)){
            this.selectedTab.getActiveWidget().editButton.onClick(buttonMouseX, buttonMouseY);
        }
        else if(this.selectedTab.getActiveWidget() != null && this.selectedTab.getActiveWidget().addButton.isMouseOver(buttonMouseX, buttonMouseY)){
            this.selectedTab.getActiveWidget().addButton.onClick(buttonMouseX, buttonMouseY);
        }
        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }

    @Override
    public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
        if (pButton != 0) {
            this.isScrolling = false;
            return false;
        }
        else {
            if (!this.isScrolling) {
                this.isScrolling = true;
            }
            else if (this.selectedTab != null) {
                this.selectedTab.scroll(pDragX, pDragY);
            }
            return true;
        }
    }

    @Override
    public boolean isMouseOver(double pMouseX, double pMouseY) {
        int screenLeftXCorner = 0;
        int screenRightXCorner = screenLeftXCorner + WINDOW_INSIDE_WIDTH ;
        int screenTopYCorner = 0;
        int screenBottomYCorner = screenTopYCorner + WINDOW_INSIDE_HEIGHT;

        return pMouseX >= screenLeftXCorner && pMouseX <= screenRightXCorner && pMouseY >= screenTopYCorner && pMouseY <= screenBottomYCorner;
    }


    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        if (ClientRegistrationHandler.ADVANCEMENT_GENERATOR_KEY.get().matches(pKeyCode, pScanCode)) {
            this.onClose();
            return true;
        }
        else {
            return super.keyPressed(pKeyCode, pScanCode, pModifiers);
        }
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
        this.minecraft.mouseHandler.grabMouse();
        ClientAdvancementGenerator.saveBuildAdvancements();
    }

    @Override
    public void removed() {
        this.advancements.setListener(null);
        ClientPacketListener clientpacketlistener = this.minecraft.getConnection();
        if (clientpacketlistener != null) {
            clientpacketlistener.send(ServerboundSeenAdvancementsPacket.closedScreen());
        }
    }


    @Override
    public void onAddAdvancementRoot(Advancement pAdvancement) {
        FakeAdvancementTab advancementtab = FakeAdvancementTab.create(this.minecraft, this, this.tabs.size(), pAdvancement);
        if (advancementtab != null) {
            this.tabs.put(pAdvancement, advancementtab);
        }
    }

    @Override
    public void onAddAdvancementTask(Advancement pAdvancement) {
        FakeAdvancementTab advancementtab = this.getTab(pAdvancement);
        if (advancementtab != null) {
            advancementtab.addAdvancement(pAdvancement);
        }
    }

    @Override
    public void onSelectedTabChanged(@Nullable Advancement pAdvancement) {
        this.selectedTab = this.tabs.get(pAdvancement);
    }

    @Override
    public void onAdvancementsCleared() {
        this.tabs.clear();
        this.selectedTab = null;
    }

    @Nullable
    private FakeAdvancementTab getTab(Advancement pAdvancement) {
        while(pAdvancement.getParent() != null) {
            pAdvancement = pAdvancement.getParent();
        }

        return this.tabs.get(pAdvancement);
    }

    public void setRenderTooltips(boolean renderTooltips){this.renderTooltips = renderTooltips;}

    public void setScreenOffset(int x, int y){
        this.screenOffsetX = (this.width - WINDOW_WIDTH) / 2 + x;
        this.screenOffsetY = (this.height - WINDOW_HEIGHT) / 2 + y;
    }

    public void resetScreen(){
        this.setScreenOffset((this.width - WINDOW_WIDTH) / 2, (this.height - WINDOW_HEIGHT) / 2);
        this.renderTooltips = true;
    }

    public void updateAdvancements(ClientAdvancements advancementsIn, Advancement changedAdvancementWidget){
        FakeAdvancementTab temp = this.selectedTab;
        FakeAdvancementWidget activeWidget = temp != null ? temp.getActiveWidget() : null;
        this.advancements = advancementsIn;
        this.init();
        Advancement temp2;
        if(changedAdvancementWidget != null){
            temp2 = changedAdvancementWidget.getParent() == null ? changedAdvancementWidget : (temp == null ? advancementsIn.getAdvancements().get(new ResourceLocation(MOD_ID, "fake_root_advancement")) : temp.getAdvancement());
        }
        else {
            temp2 = temp == null ? advancementsIn.getAdvancements().get(new ResourceLocation(MOD_ID, "fake_root_advancement")) : temp.getAdvancement();
        }
        this.advancements.setSelectedTab(temp2, true);
        if(changedAdvancementWidget != null){
            activeWidget = this.selectedTab.getWidget(changedAdvancementWidget);
        }
        if(this.selectedTab != null) {
            this.selectedTab.setActiveWidget(activeWidget);
        }
    }

    public void updateAdvancementDisplay(@NotNull ResourceLocation id, @Nullable ResourceLocation parentId, DisplayInfo displayInfo){
        Advancement advancement = this.advancements.getAdvancements().get(id);
        Advancement.Builder builder;
        boolean changeActiveWidget;
        if(advancement != null && !advancement.getId().equals(new ResourceLocation(MOD_ID, "fake_root_advancement"))){
            builder = advancement.deconstruct().display(displayInfo);
            changeActiveWidget = false;
        }
        else {
            builder = Advancement.Builder.advancement();
            if(parentId != null){
                builder.parent(parentId);
            }
            builder.display(displayInfo);
            changeActiveWidget = true;
        }

        if(this.advancements.getAdvancements().get(builder.parentId) != null){
            builder.parent(Objects.requireNonNull(this.advancements.getAdvancements().get(builder.parentId)));
        }
        advancement = builder.build(id);
        Collection<Advancement> temp2 = new HashSet<>();
        temp2.add(advancement);
        this.updateAdvancements(ClientRegistrationHandler.getFakeAdvancements(temp2), changeActiveWidget ? advancement : null);
    }


    public void onRemoveAdvancementTask(Advancement pAdvancement) {}

    public void onUpdateAdvancementProgress(Advancement pAdvancement, AdvancementProgress pProgress) {}

    public void onRemoveAdvancementRoot(Advancement pAdvancement) {}
}
