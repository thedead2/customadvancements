package de.thedead2.customadvancements.client.components;

import com.mojang.blaze3d.vertex.PoseStack;
import de.thedead2.customadvancements.client.Area;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;


//TODO: Add listener methods for potential header (mouseClicked, etc.)
public abstract class ScrollableScreenComponent extends ScreenComponent {

    protected final Area contentArea;

    protected final ScrollBar xScrollBar, yScrollBar;

    @Nullable
    private ScreenComponent header;

    private ScrollDirection scrollDirection;

    private boolean showOverflow;


    protected ScrollableScreenComponent(Area area, ScrollDirection scrollDirection, ScrollBar.Visibility visibility) {
        super(area);
        this.contentArea = area.copy();
        this.scrollDirection = scrollDirection;
        this.xScrollBar = new HorizontalScrollBar((scrollDirection == ScrollDirection.BOTH || scrollDirection == ScrollDirection.HORIZONTAL) ? visibility : ScrollBar.Visibility.NEVER, 2, this.scrollRate());
        this.yScrollBar = new VerticalScrollBar((scrollDirection == ScrollDirection.BOTH || scrollDirection == ScrollDirection.VERTICAL) ? visibility : ScrollBar.Visibility.NEVER, 2, this.scrollRate());
    }


    protected abstract double scrollRate();


    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return this.yScrollBar.mouseClicked(mouseX, mouseY, button) || this.xScrollBar.mouseClicked(mouseX, mouseY, button);
    }


    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return this.yScrollBar.mouseReleased(mouseX, mouseY, button) || this.xScrollBar.mouseReleased(mouseX, mouseY, button);
    }


    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return this.yScrollBar.mouseDragged(mouseX, mouseY, button, dragX, dragY) || this.xScrollBar.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }


    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        return switch (this.scrollDirection) {
            case VERTICAL -> this.yScrollBar.mouseScrolled(mouseX, mouseY, delta);
            case HORIZONTAL -> this.xScrollBar.mouseScrolled(mouseX, mouseY, delta);
            case BOTH -> {
                if (!Screen.hasAltDown()) {
                    yield this.yScrollBar.mouseScrolled(mouseX, mouseY, delta);
                }
                else {
                    yield this.xScrollBar.mouseScrolled(mouseX, mouseY, delta);
                }
            }
        };
    }


    public void setHeader(@Nullable ScreenComponent header) {
        this.header = header;

        if(this.header == null) {
            this.contentArea.set(this.area);

            return;
        }

        this.header.getArea().setPosition(this.area.getX(), this.area.getY(), this.area.getZ());

        if (this.header instanceof ScrollableScreenComponent scrollableScreenComponent) {
            scrollableScreenComponent.contentArea.setPosition(this.area.getX(), this.area.getY(), this.area.getZ());
        }

        this.contentArea.setHeight(this.area.getHeight() - header.getHeight());
        this.contentArea.setY(this.area.getY() + header.getHeight());
    }


    public void setScrollAmount(ScrollDirection target, float amount) {
        switch (target) {
            case VERTICAL -> this.yScrollBar.setScrollAmount(amount);
            case HORIZONTAL -> this.xScrollBar.setScrollAmount(amount);
            case BOTH -> {
                this.yScrollBar.setScrollAmount(amount);
                this.xScrollBar.setScrollAmount(amount);
            }
        }
    }


    @Override
    public void render(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        PoseStack poseStack = guiGraphics.pose();

        if (this.header != null) {
            this.header.render(guiGraphics, mouseX, mouseY, partialTick);
        }

        if (!this.showOverflow) {
            guiGraphics.enableScissor(Math.round(this.contentArea.getInnerX() - 1), Math.round(this.contentArea.getInnerY()), Math.round(this.contentArea.getInnerXMax() + 1), Math.round(this.contentArea.getInnerYMax() + 1));
        }

        poseStack.pushPose();
        this.checkScrollAmounts();

        switch (this.scrollDirection) {
            case VERTICAL -> poseStack.translate(0, -this.yScrollBar.getScrollAmount(), 0);
            case HORIZONTAL -> poseStack.translate(-this.xScrollBar.getScrollAmount(), 0, 0);
            case BOTH -> poseStack.translate(-this.xScrollBar.getScrollAmount(), -this.yScrollBar.getScrollAmount(), 0);
        }

        this.renderContents(guiGraphics, mouseX, mouseY, partialTick);
        poseStack.popPose();

        if (!this.showOverflow) {
            guiGraphics.disableScissor();
        }

        this.renderDecorations(guiGraphics, mouseX, mouseY, partialTick);
        this.xScrollBar.render(guiGraphics, mouseX, mouseY, partialTick);
        this.yScrollBar.render(guiGraphics, mouseX, mouseY, partialTick);
    }


    private void checkScrollAmounts() {
        double yScrollAmount = this.yScrollBar.getScrollAmount();

        if (this.contentHeight() < yScrollAmount) {
            this.yScrollBar.setScrollAmount(yScrollAmount - this.contentHeight());
        }
    }


    protected abstract void renderContents(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick);


    protected void renderDecorations(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
    }


    protected float contentHeight() {
        return this.contentArea.getInnerHeight();
    }


    public void showOverflow(boolean bool) {
        this.showOverflow = bool;
    }


    public boolean shouldShowOverflow() {
        return showOverflow;
    }


    protected boolean withinContentArea(double pX, double pY) {
        return this.contentArea.innerContains((float) pX, (float) pY);
    }


    protected boolean withinContentAreaTopBottom(float top, float bottom) {
        return switch (this.scrollDirection) {
            case VERTICAL, BOTH -> bottom - this.yScrollBar.getScrollAmount() >= this.contentArea.getInnerY() && top - this.yScrollBar.getScrollAmount() <= this.contentArea.getInnerYMax();
            case HORIZONTAL -> bottom >= this.contentArea.getInnerY() && top <= this.contentArea.getInnerYMax();
        };
    }


    protected float contentWidth() {
        return this.contentArea.getInnerWidth();
    }


    public void setScrollDirection(ScrollDirection scrollDirection) {
        this.scrollDirection = scrollDirection;
    }


    public void setScrollBarVisibility(ScrollBar.Visibility visibility) {
        switch (this.scrollDirection) {
            case BOTH -> {
                this.yScrollBar.setVisibility(visibility);
                this.xScrollBar.setVisibility(visibility);
            }
            case HORIZONTAL -> this.xScrollBar.setVisibility(visibility);
            case VERTICAL -> this.yScrollBar.setVisibility(visibility);
        }
    }


    public float getMaxScrollAmount(ScrollDirection target) {
        return switch (target) {
            case BOTH -> Math.max(this.yScrollBar.getMaxScrollAmount(), this.xScrollBar.getMaxScrollAmount());
            case HORIZONTAL -> this.xScrollBar.getMaxScrollAmount();
            case VERTICAL -> this.yScrollBar.getMaxScrollAmount();
        };
    }


    public enum ScrollDirection {

        VERTICAL,

        HORIZONTAL,

        BOTH
    }


    public class HorizontalScrollBar extends ScrollBar {

        private final float height;


        protected HorizontalScrollBar(Visibility visibility, float height, double scrollRate) {
            super(visibility, scrollRate);
            this.height = height;
        }


        @Override
        public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
            if (!this.mouseClicked) {
                return false;
            }

            if (mouseX < (double) ScrollableScreenComponent.this.contentArea.getX()) {
                this.setScrollAmount(0.0D);
            }
            else if (mouseX > (double) ScrollableScreenComponent.this.contentArea.getXMax()) {
                this.setScrollAmount(this.getMaxScrollAmount());
            }
            else {
                float i = this.getWidth();
                double d0 = Math.max(1, this.getMaxScrollAmount() / (ScrollableScreenComponent.this.contentArea.getInnerWidth() - i));

                this.setScrollAmount(this.scrollAmount + dragX * d0);
            }

            return true;
        }


        @Override
        public float getMaxScrollAmount() {
            return Math.max(0, ScrollableScreenComponent.this.contentWidth() - ScrollableScreenComponent.this.contentArea.getInnerWidth());
        }


        @Override
        public boolean isMouseOver(double mouseX, double mouseY) {
            if (!this.isVisible()) {
                return false;
            }

            float scrollBarWidth = this.getWidth();
            float xMin = Math.max(ScrollableScreenComponent.this.contentArea.getX(), (int) this.getScrollAmount() * (ScrollableScreenComponent.this.contentArea.getWidth() - scrollBarWidth) / Math.max(this.getMaxScrollAmount(), 1) + ScrollableScreenComponent.this.contentArea.getX());
            float xMax = xMin + scrollBarWidth;
            float yMin = ScrollableScreenComponent.this.contentArea.getYMax() - this.height;
            float yMax = yMin + this.height + 2;

            return mouseX >= (double) xMin && mouseX <= (double) xMax && mouseY >= yMin && mouseY < (double) yMax;
        }


        @Override
        protected boolean calcVisibility() {
            return ScrollableScreenComponent.this.contentWidth() > ScrollableScreenComponent.this.contentArea.getInnerWidth();
        }


        private float getWidth() {
            return Mth.clamp(((ScrollableScreenComponent.this.contentArea.getWidth() * ScrollableScreenComponent.this.contentArea.getWidth()) / ScrollableScreenComponent.this.contentWidth()), 15, ScrollableScreenComponent.this.contentArea.getWidth());
        }


        @Override
        public void render(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            float scrollBarWidth = this.getWidth();
            float xMin = Math.max(ScrollableScreenComponent.this.contentArea.getX(), (int) this.getScrollAmount() * (ScrollableScreenComponent.this.contentArea.getWidth() - scrollBarWidth) / Math.max(this.getMaxScrollAmount(), 1) + ScrollableScreenComponent.this.contentArea.getX());
            float xMax = xMin + scrollBarWidth;
            float yMin = ScrollableScreenComponent.this.contentArea.getYMax() - this.height;
            float yMax = yMin + this.height;

            this.render(xMin, xMax, yMin, yMax, ScrollableScreenComponent.this.contentArea.getZ() + 2, mouseX, mouseY);
        }
    }


    public class VerticalScrollBar extends ScrollBar {

        private final float width;


        protected VerticalScrollBar(Visibility visibility, float width, double scrollRate) {
            super(visibility, scrollRate);
            this.width = width;
        }


        @Override
        public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
            if (!this.mouseClicked) {
                return false;
            }

            if (mouseY < (double) ScrollableScreenComponent.this.contentArea.getY()) {
                this.setScrollAmount(0.0D);
            }
            else if (mouseY > (double) ScrollableScreenComponent.this.contentArea.getYMax()) {
                this.setScrollAmount(this.getMaxScrollAmount());
            }
            else {
                float i = this.getHeight();
                double d0 = Math.max(1, this.getMaxScrollAmount() / (ScrollableScreenComponent.this.contentArea.getInnerHeight() - i));

                this.setScrollAmount(this.scrollAmount + dragY * d0);
            }

            return true;
        }


        @Override
        public float getMaxScrollAmount() {
            return Math.max(0, ScrollableScreenComponent.this.contentHeight() - (ScrollableScreenComponent.this.contentArea.getInnerHeight()));
        }


        @Override
        public boolean isMouseOver(double mouseX, double mouseY) {
            if (!this.isVisible()) {
                return false;
            }

            float scrollBarHeight = this.getHeight();
            float xMin = ScrollableScreenComponent.this.contentArea.getXMax() - this.width;
            float xMax = xMin + this.width + 2;
            float yMin = Math.max(ScrollableScreenComponent.this.contentArea.getY(), (int) this.getScrollAmount() * (ScrollableScreenComponent.this.contentArea.getHeight() - scrollBarHeight) / Math.max(this.getMaxScrollAmount(), 1) + ScrollableScreenComponent.this.contentArea.getY());
            float yMax = yMin + scrollBarHeight;

            return mouseX >= (double) xMin && mouseX <= (double) xMax && mouseY >= yMin && mouseY < (double) yMax;
        }


        @Override
        protected boolean calcVisibility() {
            return ScrollableScreenComponent.this.contentHeight() > ScrollableScreenComponent.this.contentArea.getInnerHeight();
        }


        private float getHeight() {
            return Mth.clamp(((ScrollableScreenComponent.this.contentArea.getHeight() * ScrollableScreenComponent.this.contentArea.getHeight()) / ScrollableScreenComponent.this.contentHeight()), 15, ScrollableScreenComponent.this.contentArea.getHeight());
        }


        @Override
        public void render(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            float scrollBarHeight = this.getHeight();
            float xMin = ScrollableScreenComponent.this.contentArea.getXMax() - this.width;
            float xMax = xMin + this.width;
            float yMin = Math.max(ScrollableScreenComponent.this.contentArea.getY(), (int) this.getScrollAmount() * (ScrollableScreenComponent.this.contentArea.getHeight() - scrollBarHeight) / Math.max(this.getMaxScrollAmount(), 1) + ScrollableScreenComponent.this.contentArea.getY());
            float yMax = yMin + scrollBarHeight;

            this.render(xMin, xMax, yMin, yMax, ScrollableScreenComponent.this.contentArea.getZ() + 2, mouseX, mouseY);
        }
    }
}
