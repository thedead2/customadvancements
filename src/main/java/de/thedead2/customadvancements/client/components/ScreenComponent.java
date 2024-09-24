package de.thedead2.customadvancements.client.components;

import de.thedead2.customadvancements.client.Area;
import de.thedead2.customadvancements.client.RenderUtil;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2d;

import javax.annotation.Nonnull;


public abstract class ScreenComponent implements Renderable, GuiEventListener, NarratableEntry {

    protected final Area area;

    protected float alpha = 1;

    protected boolean focused = false;


    public ScreenComponent(Area area) {
        this.area = area;
    }


    @Override
    public void setFocused(boolean focus) {
        this.focused = focus;
    }


    public Area getArea() {
        return area;
    }


    public float getAlpha() {
        return this.alpha;
    }


    public ScreenComponent setAlpha(float alpha) {
        this.alpha = alpha;

        return this;
    }


    @Override
    public @NotNull NarrationPriority narrationPriority() {
        if (this.focused) {
            return NarrationPriority.FOCUSED;
        }
        else {
            return this.isMouseOver() ? NarrationPriority.HOVERED : NarrationPriority.NONE;
        }
    }


    public boolean isMouseOver() {
        Vector2d mousePos = RenderUtil.getMousePos();

        return this.isMouseOver(mousePos.x, mousePos.y);
    }


    @Override
    public boolean isFocused() {
        return focused;
    }


    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return this.area.contains((float) mouseX, (float) mouseY);
    }


    public float getY() {
        return this.area.getY();
    }


    public float getZ() {
        return this.area.getZ();
    }


    public float getWidth() {
        return this.area.getWidth();
    }


    public float getHeight() {
        return this.area.getHeight();
    }


    public float getInnerWidth() {
        return this.area.getInnerWidth();
    }


    public void setInnerWidth(float width) {
        this.area.setInnerWidth(width);
    }


    public float getInnerHeight() {
        return this.area.getInnerHeight();
    }


    public float getInnerX() {
        return this.area.getInnerX();
    }


    @Override
    public abstract void updateNarration(@Nonnull NarrationElementOutput narrationElementOutput);


    public float getInnerY() {
        return this.area.getInnerY();
    }


    public float getX() {
        return this.area.getX();
    }


    public float getXMax() {
        return this.area.getXMax();
    }


    public float getInnerXMax() {
        return this.area.getInnerXMax();
    }


    public float getYMax() {
        return this.area.getYMax();
    }






}
