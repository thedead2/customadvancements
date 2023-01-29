package de.thedead2.customadvancements.client.gui;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import de.thedead2.customadvancements.client.ClientRegistrationHandler;
import de.thedead2.customadvancements.client.gui.components.FakeAdvancementTab;
import de.thedead2.customadvancements.generator.ClientAdvancementGenerator;
import de.thedead2.customadvancements.util.ModHelper;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.advancements.AdvancementTabType;
import net.minecraft.client.multiplayer.ClientAdvancements;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundSeenAdvancementsPacket;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Map;

public class AdvancementGeneratorGUI extends Screen implements ClientAdvancements.Listener {

    //First Screen
    private static final ResourceLocation WINDOW_LOCATION = new ResourceLocation("textures/gui/advancements/window.png");
    private static final ResourceLocation TABS_LOCATION = new ResourceLocation("textures/gui/advancements/tabs.png");
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
    private static final Component TITLE = Component.translatable("gui.customadvancements.advancement_generator.title");
    private final ClientAdvancements advancements;
    private final Map<Advancement, FakeAdvancementTab> tabs = Maps.newLinkedHashMap();
    @Nullable
    private FakeAdvancementTab selectedTab;
    private boolean isScrolling;
    private static int tabPage, maxPages;


    private final Screen parent;

    public AdvancementGeneratorGUI(ClientAdvancements advancementsIn, Screen parent, Minecraft minecraft) {
        super(TITLE);
        this.advancements = advancementsIn;
        this.parent = parent;
        this.minecraft = minecraft;
    }


        protected void init() {
            this.tabs.clear();
            this.selectedTab = null;
            this.advancements.setListener(this);
            if (this.selectedTab == null && !this.tabs.isEmpty()) {
                this.advancements.setSelectedTab(this.tabs.values().iterator().next().getAdvancement(), true);
            } else {
                this.advancements.setSelectedTab(this.selectedTab == null ? null : this.selectedTab.getAdvancement(), true);
            }
            if (this.tabs.size() > AdvancementTabType.MAX_TABS) {
                int guiLeft = (this.width - WINDOW_WIDTH) / 2;
                int guiTop = (this.height - WINDOW_HEIGHT) / 2;

                addRenderableWidget(new net.minecraft.client.gui.components.Button(guiLeft,            guiTop - 50, 20, 20, Component.literal("<"), b -> tabPage = Math.max(tabPage - 1, 0       )));
                addRenderableWidget(new net.minecraft.client.gui.components.Button(guiLeft + 252 - 20, guiTop - 50, 20, 20, Component.literal(">"), b -> tabPage = Math.min(tabPage + 1, maxPages)));
                maxPages = this.tabs.size() / AdvancementTabType.MAX_TABS;
            }
        }

        public void removed() {
            this.advancements.setListener(null);
            ClientPacketListener clientpacketlistener = this.minecraft.getConnection();
            if (clientpacketlistener != null) {
                clientpacketlistener.send(ServerboundSeenAdvancementsPacket.closedScreen());
            }

        }

        public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
            int OFFSET_X = (this.width - WINDOW_WIDTH) / 2;
            int OFFSET_Y = (this.height - WINDOW_HEIGHT) / 2;

            if (pButton == 0) {
                for(FakeAdvancementTab advancementtab : this.tabs.values()) {
                    if (advancementtab.getPage() == tabPage && advancementtab.isMouseOver(OFFSET_X, OFFSET_Y, pMouseX, pMouseY)) {
                        if(advancementtab.getAdvancement().getId().toString().equals(ModHelper.MOD_ID + ":" + "fake_root_advancement")){
                            this.minecraft.setScreen(new ClientAdvancementGenerator(this, this.minecraft, (ResourceLocation) null));
                        }
                        else {
                            this.advancements.setSelectedTab(advancementtab.getAdvancement(), true);
                        }
                        break;
                    }
                }
            }
            double buttonMouseX = pMouseX - OFFSET_X - 9;
            double buttonMouseY = pMouseY - OFFSET_Y - 18;
            if (this.selectedTab.getActiveWidget() != null && this.selectedTab.getActiveWidget().editButton.isMouseOver(buttonMouseX, buttonMouseY)){
                ModHelper.LOGGER.debug("Test!");
                this.selectedTab.getActiveWidget().editButton.onClick(buttonMouseX, buttonMouseY);
            }
            else if(this.selectedTab.getActiveWidget() != null && this.selectedTab.getActiveWidget().addButton.isMouseOver(buttonMouseX, buttonMouseY)){
                this.selectedTab.getActiveWidget().addButton.onClick(buttonMouseX, buttonMouseY);
            }

            return super.mouseClicked(pMouseX, pMouseY, pButton);
        }

        public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
            if (ClientRegistrationHandler.ADVANCEMENT_GENERATOR_KEY.get().matches(pKeyCode, pScanCode)) {
                this.onClose();
                return true;
            }
            else {
                return super.keyPressed(pKeyCode, pScanCode, pModifiers);
            }
        }


        public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        int OFFSET_X = (this.width - WINDOW_WIDTH) / 2;
        int OFFSET_Y = (this.height - WINDOW_HEIGHT) / 2;

        this.renderBackground(pPoseStack);
            if (maxPages != 0) {
                net.minecraft.network.chat.Component page = Component.literal(String.format("%d / %d", tabPage + 1, maxPages + 1));
                int width = this.font.width(page);
                this.font.drawShadow(pPoseStack, page.getVisualOrderText(), OFFSET_X + (252 / 2) - (width / 2), OFFSET_Y - 44, -1);
            }
            this.renderInside(pPoseStack, pMouseX, pMouseY, OFFSET_X, OFFSET_Y);
            this.renderWindow(pPoseStack, OFFSET_X, OFFSET_Y);
            this.renderTooltips(pPoseStack, pMouseX, pMouseY, OFFSET_X, OFFSET_Y, pPartialTick);
        }


        public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
            if (pButton != 0) {
                this.isScrolling = false;
                return false;
            } else {
                if (!this.isScrolling) {
                    this.isScrolling = true;
                } else if (this.selectedTab != null) {
                    this.selectedTab.scroll(pDragX, pDragY);
                }

                return true;
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
                throw new RuntimeException("Unable to create Advancement Generator GUI as there are no advancements to display!");
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
                    if (advancementtab.getPage() == tabPage)
                        advancementtab.drawTab(pPoseStack, offsetX, offsetY, advancementtab == this.selectedTab);
                }

                RenderSystem.defaultBlendFunc();

                for(FakeAdvancementTab advancementtab1 : this.tabs.values()) {
                    if (advancementtab1.getPage() == tabPage)
                        advancementtab1.drawIcon(offsetX, offsetY, this.itemRenderer);
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

        public void onAddAdvancementRoot(Advancement pAdvancement) {
            FakeAdvancementTab advancementtab = FakeAdvancementTab.create(this.minecraft, this, this.tabs.size(), pAdvancement);
            if (advancementtab != null) {
                this.tabs.put(pAdvancement, advancementtab);
            }
        }

        public void onRemoveAdvancementRoot(Advancement pAdvancement) {
        }

        public void onAddAdvancementTask(Advancement pAdvancement) {
            FakeAdvancementTab advancementtab = this.getTab(pAdvancement);
            if (advancementtab != null) {
                advancementtab.addAdvancement(pAdvancement);
            }
        }

        public void onRemoveAdvancementTask(Advancement pAdvancement) {
        }

        public void onUpdateAdvancementProgress(Advancement pAdvancement, AdvancementProgress pProgress) {
        }

        public void onSelectedTabChanged(@Nullable Advancement pAdvancement) {
            this.selectedTab = this.tabs.get(pAdvancement);
        }

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


    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
        this.minecraft.mouseHandler.grabMouse();
    }

}
