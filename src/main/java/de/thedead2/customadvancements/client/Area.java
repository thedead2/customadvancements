package de.thedead2.customadvancements.client;

import it.unimi.dsi.fastutil.floats.Float2FloatFunction;
import org.joml.Vector3f;


public class Area {

    public static final Area EMPTY = new Area(0, 0, 0, 0, 0, 0);

    public static Area SCREEN() {
        return new Area(() -> 0, () -> 0, () -> 0, RenderUtil::getScreenWidth, RenderUtil::getScreenHeight, () -> 0);
    }

    protected Padding padding;

    protected FloatSupplier xPos;

    protected FloatSupplier yPos;

    protected FloatSupplier zPos;

    protected FloatSupplier width;

    protected FloatSupplier height;

    protected FloatSupplier depth;


    public Area(float xPos, float yPos, float zPos, float width, float height, float depth) {
        this(xPos, yPos, zPos, width, height, depth, Padding.NONE);
    }


    public Area(Float2FloatFunction xPos, Float2FloatFunction yPos, float zPos, float width, float height, float depth, Padding padding) {
        this.xPos = () -> xPos.get(width);
        this.yPos = () -> yPos.get(height);
        this.zPos = () -> zPos;
        this.width = () -> width;
        this.height = () -> height;
        this.depth = () -> depth;
        this.padding = padding;
    }


    public Area(float xPos, float yPos, float zPos, float width, float height, float depth, Padding padding) {
        this(() -> xPos, () -> yPos, () -> zPos, () -> width, () -> height, () -> depth,  padding);
    }


    public Area(FloatSupplier xPos, FloatSupplier yPos, FloatSupplier zPos, FloatSupplier width, FloatSupplier height, FloatSupplier depth, Padding padding) {
        this.xPos = xPos;
        this.yPos = yPos;
        this.zPos = zPos;
        this.width = width;
        this.height = height;
        this.depth = depth;
        this.padding = padding;
    }


    public Area(FloatSupplier xPos, FloatSupplier yPos, FloatSupplier zPos, FloatSupplier width, FloatSupplier height, FloatSupplier depth) {
        this(xPos, yPos, zPos, width, height, depth, Padding.NONE);
    }


    public static Area withCorners(int xMin, int xMax, int yMin, int yMax, int zPos) {
        return new Area(xMin, yMin, zPos, xMax - xMin, yMax - yMin, 0);
    }


    public boolean doAreasIntersect2D(Area other) {
        return  this.getX() < other.getXMax() && this.getXMax() > other.getX() &&
                this.getY() < other.getYMax() && this.getYMax() > other.getY();
    }

    public boolean doAreasIntersect3D(Area other) {
        return  this.getX() < other.getXMax() && this.getXMax() > other.getX() &&
                this.getY() < other.getYMax() && this.getYMax() > other.getY() &&
                this.getZ() < other.getZMax() && this.getZMax() > other.getZ();
    }


    public float getX() {
        return this.xPos.getAsFloat();
    }


    public float getY() {
        return this.yPos.getAsFloat();
    }


    public Vector3f getCenter() {
        return new Vector3f(this.getCenterX(), this.getCenterY(), this.getCenterZ());
    }


    public float getXMax() {
        return this.xPos.getAsFloat() + this.width.getAsFloat();
    }


    public float getYMax() {
        return this.yPos.getAsFloat() + this.height.getAsFloat();
    }

    public float getZMax() {
        return this.zPos.getAsFloat() + this.depth.getAsFloat();
    }


    public Area setY(float yPos) {
        this.yPos = () -> yPos;
        return this;
    }


    public Area setY(FloatSupplier yPos) {
        this.yPos = yPos;
        return this;
    }


    public Area setX(float xPos) {
        this.xPos = () -> xPos;
        return this;
    }


    public Area setX(FloatSupplier xPos) {
        this.xPos = xPos;
        return this;
    }


    public Area setPosition(float xPos, float yPos, float zPos) {
        this.xPos = () -> xPos;
        this.yPos = () -> yPos;
        this.zPos = () -> zPos;
        return this;
    }


    public boolean innerContains(float pX, float pY) {
        return pX >= this.getInnerX() && pX <= this.getInnerXMax() && pY >= this.getInnerY() && pY <= this.getInnerYMax();
    }


    public Area setPostion(FloatSupplier xPos, FloatSupplier yPos, FloatSupplier zPos) {
        this.xPos = xPos;
        this.yPos = yPos;
        this.zPos = zPos;

        return this;
    }


    public Area align(Alignment alignment, Area other) {
        return this.alignWithOffset(alignment, other, 0, 0);
    }


    public Area alignWithOffset(Alignment alignment, Area other, float xOffset, float yOffset) {
        this.setX(() -> alignment.getXPos(other, this.getWidth(), xOffset));
        this.setY(() -> alignment.getYPos(other, this.getHeight(), yOffset));

        return this;
    }


    public float getWidth() {
        return this.width.getAsFloat();
    }


    public float getHeight() {
        return this.height.getAsFloat();
    }

    public float getDepth() {
        return this.depth.getAsFloat();
    }


    public Area setHeight(float height) {
        this.height = () -> height;
        return this;
    }


    public Area setHeight(FloatSupplier height) {
        this.height = height;
        return this;
    }


    public Area setWidth(float width) {
        this.width = () -> width;
        return this;
    }


    public Area setWidth(FloatSupplier width) {
        this.width = width;
        return this;
    }


    public float getZ() {
        return zPos.getAsFloat();
    }


    public Area setZ(float zPos) {
        this.zPos = () -> zPos;
        return this;
    }


    public Area setZ(FloatSupplier zPos) {
        this.zPos = zPos;
        return this;
    }


    public float getCenterX() {
        return this.xPos.getAsFloat() + this.width.getAsFloat() / 2;
    }


    public float getCenterY() {
        return this.yPos.getAsFloat() + this.height.getAsFloat() / 2;
    }

    public float getCenterZ() {
        return this.zPos.getAsFloat() + this.depth.getAsFloat() / 2;
    }


    public Area moveX(float amount) {
        return this.setX(this.xPos.getAsFloat() + amount);
    }


    public Area moveY(float amount) {
        return this.setY(this.yPos.getAsFloat() + amount);
    }


    public boolean contains3D(float pX, float pY, float pZ) {
        return pX >= this.xPos.getAsFloat() && pX <= this.xPos.getAsFloat() + this.width.getAsFloat()
                && pY >= this.yPos.getAsFloat() && pY <= this.yPos.getAsFloat() + this.height.getAsFloat()
                && pZ >= this.zPos.getAsFloat() && pZ <= this.zPos.getAsFloat() + this.depth.getAsFloat();
    }

    public boolean contains2D(float pX, float pY) {
        return pX >= this.xPos.getAsFloat() && pX <= this.xPos.getAsFloat() + this.width.getAsFloat()
                && pY >= this.yPos.getAsFloat() && pY <= this.yPos.getAsFloat() + this.height.getAsFloat();
    }



    public Area moveZ(float amount) {
        return this.setZ(this.zPos.getAsFloat() + amount);
    }


    public Area growX(float amount) {
        return this.setWidth(this.width.getAsFloat() + amount);
    }


    public Area growY(float amount) {
        return this.setHeight(this.height.getAsFloat() + amount);
    }

    public Area growZ(float amount) {
        return this.setDepth(this.depth.getAsFloat() + amount);
    }


    private Area setDepth(float depth) {
        this.depth = () -> depth;

        return this;
    }


    public Area scaleX(float amount) {
        return this.setWidth(this.width.getAsFloat() * amount);
    }


    public Area scaleY(float amount) {
        return this.setHeight(this.height.getAsFloat() * amount);
    }


    public float getInnerXMax() {
        return this.getInnerX() + this.getInnerWidth();
    }


    public float getInnerX() {
        return this.xPos.getAsFloat() + this.padding.getLeft();
    }


    public float getInnerWidth() {
        return this.width.getAsFloat() - this.padding.getLeft() - this.padding.getRight();
    }


    public Area setInnerWidth(float width) {
        return this.setWidth(width + this.padding.getLeft() + this.padding.getRight());
    }


    public float getInnerYMax() {
        return this.getInnerY() + this.getInnerHeight();
    }


    public float getInnerY() {
        return this.yPos.getAsFloat() + this.padding.getTop();
    }


    public float getInnerHeight() {
        return this.height.getAsFloat() - this.padding.getTop() - this.padding.getBottom();
    }


    public Area setInnerHeight(float height) {
        return this.setHeight(height + this.padding.getTop() + this.padding.getBottom());
    }


    public Area copy() {
        return new Area(this.xPos, this.yPos, this.zPos, this.width, this.height, this.depth, this.padding);
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


    public void set(Area other) {
        this.xPos = other.xPos;
        this.yPos = other.yPos;
        this.zPos = other.zPos;
        this.width = other.width;
        this.height = other.height;
        this.padding = other.padding;
    }
}