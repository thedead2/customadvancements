package de.thedead2.customadvancements.client.components;

import de.thedead2.customadvancements.client.RenderUtil;
import de.thedead2.customadvancements.client.animation.AnimationTypes;
import de.thedead2.customadvancements.client.animation.InterpolationTypes;
import de.thedead2.customadvancements.client.animation.LoopTypes;
import de.thedead2.customadvancements.client.animation.SimpleAnimation;
import de.thedead2.customadvancements.util.MathHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;

import javax.annotation.Nonnull;
import java.awt.*;


public class Cursor implements Renderable {

    private final SimpleAnimation blinkAnimation = new SimpleAnimation(MathHelper.secondsToTicks(0.65f / 2), MathHelper.secondsToTicks(0.65f), LoopTypes.LOOP, AnimationTypes.STEPS(1), InterpolationTypes.LINEAR);

    private final int lineWidth = 2;

    private float xPos;

    private float yPos;

    private float zPos;

    private int charPos;

    private int selectPos;

    private float alpha = 1;


    public Cursor(float xPos, float yPos, float zPos, int charPos) {
        this.xPos = xPos;
        this.yPos = yPos;
        this.zPos = zPos;
        this.charPos = charPos;
    }


    @Override
    public void render(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.blinkAnimation.animate(0, this.alpha, t -> RenderUtil.verticalLine(guiGraphics.pose(), this.xPos, this.yPos - 2, this.yPos + this.getHeight() - 2, this.zPos, lineWidth, RenderUtil.changeAlpha(Color.WHITE.getRGB(), t)));
    }


    public float getHeight() {
        return 9 + 4;
    }


    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }


    public int getCharPos() {
        return charPos;
    }


    public void setCharPos(int i) {
        this.charPos = i;
    }


    public int getSelectPos() {
        return selectPos;
    }


    public void setSelectPos(int selectPos) {
        this.selectPos = selectPos;
    }


    public void moveCharPos(int amount) {
        this.charPos += amount;
    }


    public void setSelectToCurrentPos() {
        this.selectPos = this.charPos;
    }


    public boolean hasSelection() {
        return this.selectPos != this.charPos;
    }


    public void setDisplayPos(float x, float y, float z) {
        this.xPos = x;
        this.yPos = y;
        this.zPos = z;
    }


    public float getLineWidth() {
        return lineWidth;
    }


    public float getXPos() {
        return xPos;
    }


    public float getYPos() {
        return yPos;
    }


    public float getZPos() {
        return zPos;
    }
}
