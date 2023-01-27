package de.thedead2.customadvancements.client.gui;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import de.thedead2.customadvancements.client.ClientRegistrationHandler;
import de.thedead2.customadvancements.client.gui.components.FakeAdvancementTab;
import de.thedead2.customadvancements.generator.ClientAdvancementGenerator;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.client.Minecraft;
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
    /*public static final int WINDOW_WIDTH = 252;
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
    public static final int BACKGROUND_TILE_COUNT_Y = 7;*/
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
                int guiLeft = (this.width - 252) / 2;
                int guiTop = (this.height - 140) / 2;
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
            if (pButton == 0) {
                int i = (this.width - 252) / 2;
                int j = (this.height - 140) / 2;

                for(FakeAdvancementTab advancementtab : this.tabs.values()) {
                    if (advancementtab.getPage() == tabPage && advancementtab.isMouseOver(i, j, pMouseX, pMouseY)) {
                        this.advancements.setSelectedTab(advancementtab.getAdvancement(), true);
                        break;
                    }
                }
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
            int i = (this.width - 252) / 2;
            int j = (this.height - 140) / 2;
            this.renderBackground(pPoseStack);
            if (maxPages != 0) {
                net.minecraft.network.chat.Component page = Component.literal(String.format("%d / %d", tabPage + 1, maxPages + 1));
                int width = this.font.width(page);
                this.font.drawShadow(pPoseStack, page.getVisualOrderText(), i + (252 / 2) - (width / 2), j - 44, -1);
            }
            this.renderInside(pPoseStack, pMouseX, pMouseY, i, j);
            this.renderWindow(pPoseStack, i, j);
            this.renderTooltips(pPoseStack, pMouseX, pMouseY, i, j, pPartialTick);
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

        private void renderInside(PoseStack pPoseStack, int pMouseX, int pMouseY, int pOffsetX, int pOffsetY) {
            FakeAdvancementTab advancementtab = this.selectedTab;
            if (advancementtab != null) {
                PoseStack posestack = RenderSystem.getModelViewStack();
                posestack.pushPose();
                posestack.translate(pOffsetX + 9, pOffsetY + 18, 0.0D);
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

        public void renderWindow(PoseStack pPoseStack, int pOffsetX, int pOffsetY) {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.enableBlend();
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, WINDOW_LOCATION);
            this.blit(pPoseStack, pOffsetX, pOffsetY, 0, 0, 252, 140);
            if (this.tabs.size() > 1) {
                RenderSystem.setShaderTexture(0, TABS_LOCATION);

                for(FakeAdvancementTab advancementtab : this.tabs.values()) {
                    if (advancementtab.getPage() == tabPage)
                        advancementtab.drawTab(pPoseStack, pOffsetX, pOffsetY, advancementtab == this.selectedTab);
                }

                RenderSystem.defaultBlendFunc();

                for(FakeAdvancementTab advancementtab1 : this.tabs.values()) {
                    if (advancementtab1.getPage() == tabPage)
                        advancementtab1.drawIcon(pOffsetX, pOffsetY, this.itemRenderer);
                }

                RenderSystem.disableBlend();
            }

            this.font.draw(pPoseStack, TITLE, (float)(pOffsetX + 8), (float)(pOffsetY + 6), 4210752);
        }

        private void renderTooltips(PoseStack pPoseStack, int pMouseX, int pMouseY, int pOffsetX, int pOffsetY, float pPartialTick) {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            if (this.selectedTab != null) {
                PoseStack posestack = RenderSystem.getModelViewStack();
                posestack.pushPose();
                posestack.translate(pOffsetX + 9, pOffsetY + 18, 400.0D);
                RenderSystem.applyModelViewMatrix();
                RenderSystem.enableDepthTest();
                this.selectedTab.drawTooltips(pPoseStack, pMouseX - pOffsetX - 9, pMouseY - pOffsetY - 18, pOffsetX, pOffsetY, pPartialTick);
                RenderSystem.disableDepthTest();
                posestack.popPose();
                RenderSystem.applyModelViewMatrix();
            }

            if (this.tabs.size() > 1) {
                for(FakeAdvancementTab advancementtab : this.tabs.values()) {
                    if (advancementtab.getPage() == tabPage && advancementtab.isMouseOver(pOffsetX, pOffsetY, pMouseX, pMouseY)) {
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

    public void createNewAdvancement(Advancement parentAdvancement){
        this.minecraft.setScreen(new ClientAdvancementGenerator(this, this.minecraft, parentAdvancement));
    }
}
