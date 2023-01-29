package de.thedead2.customadvancements.client.gui.components;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import de.thedead2.customadvancements.client.gui.AdvancementGeneratorGUI;
import de.thedead2.customadvancements.generator.ClientAdvancementGenerator;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.StringSplitter;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.advancements.AdvancementWidgetType;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.List;
import java.util.Objects;

import static de.thedead2.customadvancements.util.ModHelper.LOGGER;

public class FakeAdvancementWidget extends GuiComponent {
    private static final ResourceLocation WIDGETS_LOCATION = new ResourceLocation("textures/gui/advancements/widgets.png");
    private static final int BUTTON_HEIGHT = 20;
    private static final int HEIGHT = 26;
    private static final int BOX_X = 0;
    private static final int BOX_WIDTH = 200;
    private static final int FRAME_WIDTH = 26;
    private static final int ICON_X = 8;
    private static final int ICON_Y = 5;
    private static final int ICON_WIDTH = 26;
    private static final int TITLE_PADDING_LEFT = 3;
    private static final int TITLE_PADDING_RIGHT = 5;
    private static final int TITLE_X = 32;
    private static final int TITLE_Y = 9;
    private static final int TITLE_MAX_WIDTH = 163;
    private static int LINE_HEIGHT;
    private static final int[] TEST_SPLIT_OFFSETS = new int[]{0, 10, -10, 25, -25};
    private final FakeAdvancementTab tab;
    private final Advancement advancement;
    private final DisplayInfo display;
    private final FormattedCharSequence title;
    private final List<FormattedCharSequence> description;
    private final Minecraft minecraft;
    @Nullable
    private FakeAdvancementWidget parent;
    private final List<FakeAdvancementWidget> children = Lists.newArrayList();

    private final int x;
    private final int y;// what are x and y doing?

    private final int tooltipWidth;
    private final int tooltipHeight;
    private final ResourceLocation resourceLocation;

    public boolean drawingTooltip = false;
    public EditButton editButton;
    public AddButton addButton;

    public FakeAdvancementWidget(FakeAdvancementTab pTab, Minecraft pMinecraft, Advancement pAdvancement, DisplayInfo pDisplay) {
        this.tab = pTab;
        this.advancement = pAdvancement;
        this.display = pDisplay;
        this.minecraft = pMinecraft;
        this.title = Language.getInstance().getVisualOrder(pMinecraft.font.substrByWidth(pDisplay.getTitle(), TITLE_MAX_WIDTH));
        this.x = Mth.floor(pDisplay.getX() * 28.0F);
        this.y = Mth.floor(pDisplay.getY() * 27.0F);
        int i = pAdvancement.getMaxCriteraRequired();
        int j = String.valueOf(i).length();
        int k = i > 1 ? pMinecraft.font.width("  ") + pMinecraft.font.width("0") * j * 2 + pMinecraft.font.width("/") : 0;
        int l = 29 + pMinecraft.font.width(this.title) + k;
        this.description = Language.getInstance().getVisualOrder(this.findOptimalLines(ComponentUtils.mergeStyles(pDisplay.getDescription().copy(), Style.EMPTY.withColor(pDisplay.getFrame().getChatColor())), l));

        for(FormattedCharSequence formattedcharsequence : this.description) {
            l = Math.max(l, pMinecraft.font.width(formattedcharsequence));
        }

        this.resourceLocation = this.advancement.getId();
        l = Math.max(l, pMinecraft.font.width(this.resourceLocation.toString()));

        this.tooltipWidth = l + TITLE_PADDING_LEFT + TITLE_PADDING_RIGHT;
        LINE_HEIGHT = this.minecraft.font.lineHeight;

        this.tooltipHeight = TITLE_X + this.description.size() * LINE_HEIGHT + LINE_HEIGHT + BUTTON_HEIGHT + TITLE_PADDING_RIGHT;
    }

    private static float getMaxWidth(StringSplitter pManager, List<FormattedText> pText) {
        return (float)pText.stream().mapToDouble(pManager::stringWidth).max().orElse(0.0D);
    }

    private List<FormattedText> findOptimalLines(Component pComponent, int pMaxWidth) {
        StringSplitter stringsplitter = this.minecraft.font.getSplitter();
        List<FormattedText> list = null;
        float f = Float.MAX_VALUE;

        for(int i : TEST_SPLIT_OFFSETS) {
            List<FormattedText> list1 = stringsplitter.splitLines(pComponent, pMaxWidth - i, Style.EMPTY);
            float f1 = Math.abs(getMaxWidth(stringsplitter, list1) - (float)pMaxWidth);
            if (f1 <= 10.0F) {
                return list1;
            }

            if (f1 < f) {
                f = f1;
                list = list1;
            }
        }

        return list;
    }

    @Nullable
    private FakeAdvancementWidget getFirstVisibleParent(Advancement pAdvancement) {
        do {
            pAdvancement = pAdvancement.getParent();
        } while(pAdvancement != null && pAdvancement.getDisplay() == null);

        return pAdvancement != null && pAdvancement.getDisplay() != null ? this.tab.getWidget(pAdvancement) : null;
    }

    public void drawConnectivity(PoseStack pPoseStack, int pX, int pY, boolean pDropShadow) {
        if (this.parent != null) {
            int i = pX + this.parent.x + 13;
            int j = pX + this.parent.x + HEIGHT + 4;
            int k = pY + this.parent.y + 13;
            int l = pX + this.x + 13;
            int i1 = pY + this.y + 13;
            int j1 = pDropShadow ? -16777216 : -1;
            if (pDropShadow) {
                this.hLine(pPoseStack, j, i, k - 1, j1);
                this.hLine(pPoseStack, j + 1, i, k, j1);
                this.hLine(pPoseStack, j, i, k + 1, j1);
                this.hLine(pPoseStack, l, j - 1, i1 - 1, j1);
                this.hLine(pPoseStack, l, j - 1, i1, j1);
                this.hLine(pPoseStack, l, j - 1, i1 + 1, j1);
                this.vLine(pPoseStack, j - 1, i1, k, j1);
                this.vLine(pPoseStack, j + 1, i1, k, j1);
            } else {
                this.hLine(pPoseStack, j, i, k, j1);
                this.hLine(pPoseStack, l, j, i1, j1);
                this.vLine(pPoseStack, j, i1, k, j1);
            }
        }

        for(FakeAdvancementWidget advancementwidget : this.children) {
            advancementwidget.drawConnectivity(pPoseStack, pX, pY, pDropShadow);
        }

    }

    public void draw(PoseStack pPoseStack, int pX, int pY) {
            AdvancementWidgetType advancementwidgettype = AdvancementWidgetType.OBTAINED;

            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(BOX_X, WIDGETS_LOCATION);
            this.blit(pPoseStack, pX + this.x + TITLE_PADDING_LEFT, pY + this.y, this.display.getFrame().getTexture(), 128 + advancementwidgettype.getIndex() * FRAME_WIDTH, FRAME_WIDTH, FRAME_WIDTH);
            this.minecraft.getItemRenderer().renderAndDecorateFakeItem(this.display.getIcon(), pX + this.x + ICON_X, pY + this.y + ICON_Y);

        for(FakeAdvancementWidget advancementwidget : this.children) {
            advancementwidget.draw(pPoseStack, pX, pY);
        }

    }

    public int getTooltipWidth() {
        return this.tooltipWidth;
    }

    public void addChild(FakeAdvancementWidget pAdvancementWidget) {
        this.children.add(pAdvancementWidget);
    }

    public void drawHover(PoseStack pPoseStack, int pX, int pY, float pFade, int pWidth, int pHeight, int pMouseX, int pMouseY, float pPartialTick) {
        boolean tooltipWiderThanScreen = pWidth + pX + this.x + this.tooltipWidth + HEIGHT >= this.tab.getScreen().width;
        boolean tooltipLongerThanScreen = 113 - pY - this.y - HEIGHT <= 6 + this.description.size() * LINE_HEIGHT;

        int tooltipWidthDividedBy2 = this.tooltipWidth / 2;

        AdvancementWidgetType advancementwidgettype = AdvancementWidgetType.OBTAINED;
        AdvancementWidgetType advancementwidgettype1 = AdvancementWidgetType.OBTAINED;
        AdvancementWidgetType advancementwidgettype2 = AdvancementWidgetType.OBTAINED;

        int halfTooltipWidth = this.tooltipWidth - tooltipWidthDividedBy2;
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(BOX_X, WIDGETS_LOCATION);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableBlend();
        int yPos = pY + this.y;
        int xPos = pX + this.x;
        int tooltipStartPosition;

        if (tooltipWiderThanScreen) {
            tooltipStartPosition = xPos - this.tooltipWidth + ICON_WIDTH + 6;
        }
        else {
            tooltipStartPosition = xPos;
        }


        if (!this.description.isEmpty()) {
            if (tooltipLongerThanScreen) {
                this.render9Sprite(pPoseStack, tooltipStartPosition, yPos + HEIGHT - this.tooltipHeight, this.tooltipWidth, this.tooltipHeight, 10, BOX_WIDTH, HEIGHT, 0, 52);
            }
            else {
                this.render9Sprite(pPoseStack, tooltipStartPosition, yPos, this.tooltipWidth, this.tooltipHeight, 10, BOX_WIDTH, HEIGHT, 0, 52);
            }
        }

        this.blit(pPoseStack, tooltipStartPosition, yPos, BOX_X, advancementwidgettype.getIndex() * HEIGHT, tooltipWidthDividedBy2, HEIGHT);
        this.blit(pPoseStack, tooltipStartPosition + tooltipWidthDividedBy2, yPos, BOX_WIDTH - halfTooltipWidth, advancementwidgettype1.getIndex() * FRAME_WIDTH, halfTooltipWidth, FRAME_WIDTH);
        this.blit(pPoseStack, pX + this.x + TITLE_PADDING_LEFT, pY + this.y, this.display.getFrame().getTexture(), 128 + advancementwidgettype2.getIndex() * FRAME_WIDTH, FRAME_WIDTH, FRAME_WIDTH);

        if (tooltipWiderThanScreen) {
            this.minecraft.font.drawShadow(pPoseStack, this.title, (float)(tooltipStartPosition + TITLE_PADDING_RIGHT), (float)(pY + this.y + TITLE_Y), -1);
        } else {
            this.minecraft.font.drawShadow(pPoseStack, this.title, (float)(pX + this.x + TITLE_X), (float)(pY + this.y + TITLE_Y), -1);
        }

        float yPosDescription = 0F;
        if (tooltipLongerThanScreen) {
            for(int i = 0; i < this.description.size(); ++i) {
                yPosDescription = (float)(yPos + HEIGHT - this.tooltipHeight + 7 + i * 9);
                this.minecraft.font.draw(pPoseStack, this.description.get(i), (float)(tooltipStartPosition + TITLE_PADDING_RIGHT), yPosDescription, -5592406);
            }
        }
        else {
            for(int i = 0; i < this.description.size(); ++i) {
                yPosDescription = (float)(pY + this.y + TITLE_Y + 17 + i * 9);
                this.minecraft.font.draw(pPoseStack, this.description.get(i), (float)(tooltipStartPosition + TITLE_PADDING_RIGHT), yPosDescription, -5592406);
            }
        }

        int heightOffset = 2;

        this.editButton = new EditButton((tooltipStartPosition + TITLE_PADDING_RIGHT), (int) (yPosDescription + 2 + BUTTON_HEIGHT), BUTTON_HEIGHT, BUTTON_HEIGHT, Component.literal("Edit"), pButton -> {
            this.minecraft.setScreen(new ClientAdvancementGenerator(this.tab.getScreen(), this.minecraft, this.advancement));
        }, this);
        int addButtonOffset = 4;
        this.addButton = new AddButton((tooltipStartPosition + TITLE_PADDING_RIGHT + BUTTON_HEIGHT), (int) (yPosDescription + 2 + BUTTON_HEIGHT - addButtonOffset/2), BUTTON_HEIGHT + addButtonOffset, BUTTON_HEIGHT + addButtonOffset, Component.literal("Add"), pButton -> {
            this.minecraft.setScreen(new ClientAdvancementGenerator(this.tab.getScreen(), this.minecraft, this.advancement.getId()));
        }, this);


        this.minecraft.font.draw(pPoseStack, this.resourceLocation.toString(), (float)(tooltipStartPosition + TITLE_PADDING_RIGHT), yPosDescription + 10F, Color.CYAN.getRGB()); //renders resource location

        this.editButton.active = true;
        this.addButton.active = true;

        this.editButton.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
        this.addButton.render(pPoseStack, pMouseX, pMouseY, pPartialTick);


        this.minecraft.getItemRenderer().renderAndDecorateFakeItem(this.display.getIcon(), pX + this.x + ICON_X, pY + this.y + ICON_Y);
    }


    protected void render9Sprite(PoseStack pPoseStack, int pX, int pY, int pWidth, int pHeight, int pPadding, int pUWidth, int pVHeight, int pUOffset, int pVOffset) {
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

    public boolean isMouseOver(int pX, int pY, int pMouseX, int pMouseY, int pWidth, int pHeight) {
        int leftXCorner;
        int rightXCorner;
        int topYCorner;
        int bottomYCorner;

        boolean tooltipWiderThanScreen = (pWidth + pX + this.x + this.tooltipWidth + HEIGHT) >= this.tab.getScreen().width;
        boolean tooltipLongerThanScreen = (113 - pY - this.y - HEIGHT) <= (6 + this.description.size() * LINE_HEIGHT);

        int yPos = pY + this.y;
        int xPos = pX + this.x;

        if ((pMouseX > 0 && pMouseX < 234 && pMouseY > 0 && pMouseY < 113) || drawingTooltip) {
            if (drawingTooltip) { //check for orientation of the displayed hover text
                leftXCorner = tooltipWiderThanScreen ? (xPos - this.tooltipWidth + ICON_WIDTH + 6) : xPos;
                rightXCorner = leftXCorner + this.tooltipWidth;
                topYCorner = tooltipLongerThanScreen ? (yPos + HEIGHT - this.tooltipHeight) : yPos;
                bottomYCorner = topYCorner + this.tooltipHeight;
            }
            else {
                leftXCorner = xPos;
                rightXCorner = leftXCorner + FRAME_WIDTH;
                topYCorner = yPos;
                bottomYCorner = topYCorner + HEIGHT;
                drawingTooltip = true;
            }

            int screenHeight = this.tab.getScreen().height;
            int screenWidth = this.tab.getScreen().width;

            int screenLeftXCorner = pWidth - (screenWidth / 2);
            int screenRightXCorner = screenLeftXCorner + screenWidth;
            int screenTopYCorner = pHeight - (screenHeight / 2);
            int screenBottomYCorner = screenTopYCorner + screenHeight;

            boolean mouseInArea = pMouseX >= leftXCorner && pMouseX <= rightXCorner && pMouseY >= topYCorner && pMouseY <= bottomYCorner;
            boolean mouseInScreen = pMouseX >= screenLeftXCorner && pMouseX <= screenRightXCorner && pMouseY >= screenTopYCorner && pMouseY <= screenBottomYCorner;

            if (mouseInArea) {
                this.tab.setActiveWidget(this);
            }
            else {
                drawingTooltip = false;
                this.tab.setActiveWidget(null);
            }

            return mouseInArea;
        }
        else {
            return false;
        }
    }

    public void attachToParent() {
        if (this.parent == null && this.advancement.getParent() != null) {
            this.parent = this.getFirstVisibleParent(this.advancement);
            if (this.parent != null) {
                this.parent.addChild(this);
            }
        }

    }

    public int getY() {
        return this.y;
    }

    public int getX() {
        return this.x;
    }
}