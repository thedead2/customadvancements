package de.thedead2.customadvancements.client.components;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import de.thedead2.customadvancements.client.RenderUtil;
import de.thedead2.customadvancements.client.animation.*;
import de.thedead2.customadvancements.util.MathHelper;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;

import java.util.concurrent.atomic.AtomicReference;


public abstract class ScrollBar implements Renderable, GuiEventListener {

    private final IAnimation fadeAnimation = new SimpleAnimation(MathHelper.secondsToTicks(0.25f), MathHelper.secondsToTicks(0.75f), LoopTypes.NO_LOOP, AnimationTypes.EASE_OUT, InterpolationTypes.QUINTIC);

    protected boolean mouseClicked = false;

    protected Visibility visibility;

    protected double scrollAmount;

    protected double scrollRate;

    protected boolean scrolling;

    private boolean bool = false;


    protected ScrollBar(Visibility visibility, double scrollRate) {
        this.visibility = visibility;
        this.scrollRate = scrollRate;
    }


    public double getScrollRate() {
        return scrollRate;
    }


    public void setScrollRate(float scrollRate) {
        this.scrollRate = scrollRate;
    }


    public double getScrollAmount() {
        return scrollAmount;
    }


    public void setVisibility(Visibility visibility) {
        this.visibility = visibility;
    }


    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.isMouseOver(mouseX, mouseY)) {
            this.mouseClicked = true;

            return true;
        }

        return false;
    }


    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (this.mouseClicked && button == 0) {
            this.mouseClicked = false;

            return true;
        }

        return false;
    }


    @Override
    public abstract boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY);


    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        return this.setScrollAmount(this.scrollAmount - delta * this.scrollRate);
    }


    public boolean setScrollAmount(double scrollAmount) {
        double newScrollAmount = Mth.clamp(scrollAmount, 0, this.getMaxScrollAmount());

        if (this.scrollAmount != newScrollAmount) {
            this.scrolling = true;
            this.scrollAmount = newScrollAmount;

            return true;
        }

        return false;
    }


    public abstract float getMaxScrollAmount();


    public abstract boolean isMouseOver(double mouseX, double mouseY);


    protected void render(float xMin, float xMax, float yMin, float yMax, float zPos, double mouseX, double mouseY) {
        if (!this.isVisible()) {
            return;
        }

        AtomicReference<Float> alpha = new AtomicReference<>();
        this.animateHoverEffect(alpha, mouseX, mouseY);

        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.getBuilder();

        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        bufferbuilder.vertex(xMin, yMax, zPos).color((float) 220 / 255, (float) 220 / 255, (float) 220 / 255, alpha.get()).endVertex();
        bufferbuilder.vertex(xMax, yMax, zPos).color((float) 220 / 255, (float) 220 / 255, (float) 220 / 255, alpha.get()).endVertex();
        bufferbuilder.vertex(xMax, yMin, zPos).color((float) 220 / 255, (float) 220 / 255, (float) 220 / 255, alpha.get()).endVertex();
        bufferbuilder.vertex(xMin, yMin, zPos).color((float) 220 / 255, (float) 220 / 255, (float) 220 / 255, alpha.get()).endVertex();
        bufferbuilder.vertex(xMin, yMax - 0.5f, zPos).color((float) 217 / 255, (float) 217 / 255, (float) 217 / 255, alpha.get()).endVertex();
        bufferbuilder.vertex(xMax - 0.5f, yMax - 1, zPos).color((float) 217 / 255, (float) 217 / 255, (float) 217 / 255, alpha.get()).endVertex();
        bufferbuilder.vertex(xMax - 0.5f, yMin, zPos).color((float) 217 / 255, (float) 217 / 255, (float) 217 / 255, alpha.get()).endVertex();
        bufferbuilder.vertex(xMin, yMin, zPos).color((float) 217 / 255, (float) 217 / 255, (float) 217 / 255, alpha.get()).endVertex();
        tesselator.end();

        RenderSystem.disableBlend();
    }


    public boolean isVisible() {
        return switch (this.visibility) {
            case ALWAYS -> true;
            case IF_NECESSARY -> this.calcVisibility();
            case NEVER -> false;
        };
    }


    private void animateHoverEffect(AtomicReference<Float> alpha, double mouseX, double mouseY) {
        if (!bool && (this.isMouseOver(mouseX, mouseY) || this.scrolling)) {
            this.fadeAnimation.start();
            this.fadeAnimation.sleep(MathHelper.secondsToTicks(0.05f));
            this.fadeAnimation.invert(false);

            bool = true;
        }
        else if (bool) {
            this.fadeAnimation.sleep(MathHelper.secondsToTicks(0.5f));
            this.fadeAnimation.invert(true);

            bool = false;
        }

        this.fadeAnimation.animate(0.1f, 0.6f, alpha::set);

        if (!this.isMouseOver(mouseX, mouseY)) {
            this.scrolling = false;
        }
    }


    protected abstract boolean calcVisibility();


    @Override
    public void setFocused(boolean pFocused) {}


    @Override
    public boolean isFocused() {
        var mousePos = RenderUtil.getMousePos();

        return this.isMouseOver(mousePos.x, mousePos.y);
    }


    public enum Visibility {

        NEVER,

        ALWAYS,

        IF_NECESSARY
    }
}
