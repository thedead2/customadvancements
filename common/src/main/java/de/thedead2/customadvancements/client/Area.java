package de.thedead2.customadvancements.client;

import net.minecraft.util.Mth;
import org.joml.Vector3f;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static de.thedead2.customadvancements.client.ImmutableArea.SCREEN;


public class Area {

    public static final Area EMPTY = new Area(0, 0, 0, 0, 0);

    protected Area parent;

    protected Alignment alignment;

    protected Padding padding;

    protected FloatSupplier xPos;

    protected FloatSupplier yPos;

    protected FloatSupplier zPos;

    protected FloatSupplier width;

    protected FloatSupplier height;


    public Area(float xPos, float yPos, float zPos, float width, float height) {
        this(SCREEN, null, xPos, yPos, zPos, width, height, Padding.NONE);
    }


    public Area(@Nonnull Area parent, @Nullable Alignment alignment, float xPos, float yPos, float zPos, float width, float height, Padding padding) {
        this.parent = parent;
        this.alignment = alignment != null ? alignment : Alignment.TOP_LEFT;
        this.width = () -> Mth.clamp(width, 0, 1) * this.parent.getInnerWidth();
        this.height = () -> Mth.clamp(height, 0, 1) * this.parent.getInnerHeight();
        this.xPos = () -> this.alignment.getXPos(this.parent, this.width.getAsFloat(), xPos); //this.parent.getInnerX() + xPos
        this.yPos = () -> this.alignment.getYPos(this.parent, this.height.getAsFloat(), yPos); //this.parent.getInnerY() + yPos
        this.zPos = () -> zPos;
        this.padding = padding;
    }


    public float getInnerWidth() {
        return this.width.getAsFloat() - this.padding.getLeft() - this.padding.getRight();
    }


    public void setInnerWidth(float width) {
        this.setWidth((int) (width + this.padding.getLeft() + this.padding.getRight()));
    }


    public float getInnerHeight() {
        return this.height.getAsFloat() - this.padding.getTop() - this.padding.getBottom();
    }


    public Area setInnerHeight(float height) {
        return this.setHeight((int) (height + this.padding.getTop() + this.padding.getBottom()));
    }


    public Area(@Nullable Alignment alignment, float xPos, float yPos, float zPos, float width, float height) {
        this(SCREEN, alignment, xPos, yPos, zPos, width, height, Padding.NONE);
    }


    public Area(float xPos, float yPos, float zPos, float width, float height, Padding padding) {
        this(SCREEN, null, xPos, yPos, zPos, width, height, padding);
    }


    public Area(@Nullable Alignment alignment, float xPos, float yPos, float zPos, float width, float height, Padding padding) {
        this(SCREEN, alignment, xPos, yPos, zPos, width, height, padding);
    }


    public Area(@Nonnull Area parent, float xPos, float yPos, float zPos, float width, float height) {
        this(parent, null, xPos, yPos, zPos, width, height, Padding.NONE);
    }


    public Area(@Nonnull Area parent, @Nullable Alignment alignment, float xPos, float yPos, float zPos, float width, float height) {
        this(parent, alignment, xPos, yPos, zPos, width, height, Padding.NONE);
    }


    public Area(int xPos, int yPos, int zPos, int width, int height) {
        this(SCREEN, null, xPos, yPos, zPos, width, height, Padding.NONE);
    }


    public Area(@Nonnull Area parent, @Nullable Alignment alignment, int xPos, int yPos, int zPos, int width, int height, Padding padding) {
        this.parent = parent;
        this.alignment = alignment != null ? alignment : Alignment.TOP_LEFT;
        this.width = () -> width;
        this.height = () -> height;
        this.xPos = () -> this.alignment.getXPos(this.parent, this.width.getAsFloat(), xPos);
        this.yPos = () -> this.alignment.getYPos(this.parent, this.height.getAsFloat(), yPos);
        this.zPos = () -> zPos;
        this.padding = padding;
    }


    public Area(@Nullable Alignment alignment, int xPos, int yPos, int zPos, int width, int height) {
        this(SCREEN, alignment, xPos, yPos, zPos, width, height, Padding.NONE);
    }


    public Area(int xPos, int yPos, int zPos, int width, int height, Padding padding) {
        this(SCREEN, null, xPos, yPos, zPos, width, height, padding);
    }


    public Area(@Nullable Alignment alignment, int xPos, int yPos, int zPos, int width, int height, Padding padding) {
        this(SCREEN, alignment, xPos, yPos, zPos, width, height, padding);
    }


    public Area(@Nonnull Area parent, int xPos, int yPos, int zPos, int width, int height) {
        this(parent, null, xPos, yPos, zPos, width, height, Padding.NONE);
    }


    public Area(@Nonnull Area parent, @Nullable Alignment alignment, int xPos, int yPos, int zPos, int width, int height) {
        this(parent, alignment, xPos, yPos, zPos, width, height, Padding.NONE);
    }


    public Area(@Nonnull Area parent, int xPos, int yPos, int zPos, int width, int height, Padding padding) {
        this(parent, null, xPos, yPos, zPos, width, height, padding);
    }


    protected Area(Area parent, Alignment alignment, FloatSupplier xPos, FloatSupplier yPos, FloatSupplier zPos, FloatSupplier width, FloatSupplier height, Padding padding) {
        this.parent = parent;
        this.alignment = alignment;
        this.xPos = xPos;
        this.yPos = yPos;
        this.zPos = zPos;
        this.width = width;
        this.height = height;
        this.padding = padding;
    }


    public static Area withCorners(float xMin, float xMax, float yMin, float yMax, float zPos, Padding padding) {
        return new Area(xMin, yMin, zPos, xMax - xMin, yMax - yMin, padding);
    }


    public static Area withCorners(float xMin, float xMax, float yMin, float yMax, float zPos) {
        return new Area(xMin, yMin, zPos, xMax - xMin, yMax - yMin);
    }


    public Vector3f getCenter() {
        return new Vector3f(this.getCenterX(), this.getCenterY(), this.getZ());
    }


    public float getCenterX() {
        return this.xPos.getAsFloat() + this.width.getAsFloat() / 2;
    }


    public float getCenterY() {
        return this.yPos.getAsFloat() + this.height.getAsFloat() / 2;
    }


    public float getZ() {
        return zPos.getAsFloat();
    }


    public Area setZ(float zPos) {
        this.zPos = () -> zPos;

        return this;
    }


    public float getXMax() {
        return this.xPos.getAsFloat() + this.width.getAsFloat();
    }


    public float getYMax() {
        return this.yPos.getAsFloat() + this.height.getAsFloat();
    }


    public Area setPosition(float xPos, float yPos, float zPos) {
        this.setX(xPos);
        this.setY(yPos);
        this.setZ(zPos);

        return this;
    }


    public boolean innerContains(float pX, float pY) {
        return pX >= this.getInnerX() && pX <= this.getInnerXMax() && pY >= this.getInnerY() && pY <= this.getInnerYMax();
    }


    public float getInnerX() {
        return this.xPos.getAsFloat() + this.padding.getLeft();
    }


    public float getInnerXMax() {
        return this.getInnerX() + this.getInnerWidth();
    }


    public float getInnerY() {
        return this.yPos.getAsFloat() + this.padding.getTop();
    }


    public float getInnerYMax() {
        return this.getInnerY() + this.getInnerHeight();
    }


    public float getWidth() {
        return this.width.getAsFloat();
    }


    public Area setWidth(float width) {
        this.width = () -> Mth.clamp(width, 0, 1) * this.parent.getInnerWidth();

        return this;
    }


    public Area setWidth(int width) {
        this.width = () -> width;

        return this;
    }


    public float getHeight() {
        return this.height.getAsFloat();
    }


    public Area setHeight(float height) {
        this.height = () -> Mth.clamp(height, 0, 1) * this.parent.getInnerHeight();

        return this;
    }


    public Area setHeight(int height) {
        this.height = () -> height;

        return this;
    }


    public Area moveX(float amount) {
        return this.setX(this.getX() - this.parent.getInnerX() + amount);
    }


    public float getX() {
        return this.xPos.getAsFloat();
    }


    public Area setX(float xPos) {
        this.xPos = () -> this.alignment.getXPos(this.parent, this.width.getAsFloat(), xPos);

        return this;
    }


    public Area moveY(float amount) {
        return this.setY(this.getY() - this.parent.getInnerY() + amount);
    }


    public float getY() {
        return this.yPos.getAsFloat();
    }


    public Area setY(float yPos) {
        this.yPos = () -> this.alignment.getYPos(this.parent, this.height.getAsFloat(), yPos);

        return this;
    }


    public boolean contains(float pX, float pY) {
        return pX >= this.xPos.getAsFloat() && pX <= this.xPos.getAsFloat() + this.width.getAsFloat() && pY >= this.yPos.getAsFloat() && pY <= this.yPos.getAsFloat() + this.height.getAsFloat();
    }


    public Area moveZ(float amount) {
        return this.setZ(this.zPos.getAsFloat() + amount);
    }


    public Area growX(int amount) {
        return this.setWidth((int) this.width.getAsFloat() + amount);
    }


    public Area growY(int amount) {
        return this.setHeight((int) this.height.getAsFloat() + amount);
    }


    public Area scaleX(float amount) {
        return this.setWidth((int) this.width.getAsFloat() * (int) amount);
    }


    public Area scaleY(float amount) {
        return this.setHeight((int) this.height.getAsFloat() * (int) amount);
    }


    public Area copy() {
        return new Area(this.parent, this.alignment, this.xPos, this.yPos, this.zPos, this.width, this.height, this.padding);
    }


    public Area setPadding(float padding) {
        this.padding = new Padding(padding);

        return this;
    }


    public Area setPadding(float leftRight, float topBottom) {
        this.padding = new Padding(leftRight, topBottom);

        return this;
    }


    public Area setPadding(float left, float right, float top, float bottom) {
        this.padding = new Padding(left, right, top, bottom);

        return this;
    }


    public ImmutableArea toImmutable() {
        return new ImmutableArea(this.parent, this.alignment, this.xPos, this.yPos, this.zPos, this.width, this.height, this.padding);
    }


    public Area toMutable() {
        return this;
    }


    public void set(Area other) {
        this.xPos = other.xPos;
        this.yPos = other.yPos;
        this.zPos = other.zPos;
        this.width = other.width;
        this.height = other.height;
        this.padding = other.padding;
    }
}
