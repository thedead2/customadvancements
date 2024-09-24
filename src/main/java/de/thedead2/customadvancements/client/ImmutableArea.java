package de.thedead2.customadvancements.client;


import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.concurrent.Immutable;


/**
 * An immutable representation of an {@link Area}. Any attempt in modifying the position, size or padding of this area will result in an {@link UnsupportedOperationException} to be thrown!
 */
@Immutable
public class ImmutableArea extends Area {

    public static final ImmutableArea SCREEN = new ImmutableArea(null, Alignment.TOP_LEFT, () -> 0, () -> 0, () -> 0, RenderUtil::getScreenWidth, RenderUtil::getScreenHeight, Padding.NONE);


    public ImmutableArea(float xPos, float yPos, float zPos, float width, float height) {
        super(xPos, yPos, zPos, width, height);
    }


    public ImmutableArea(@Nullable Alignment alignment, float xPos, float yPos, float zPos, float width, float height) {
        super(alignment, xPos, yPos, zPos, width, height);
    }


    public ImmutableArea(float xPos, float yPos, float zPos, float width, float height, Padding padding) {
        super(xPos, yPos, zPos, width, height, padding);
    }


    public ImmutableArea(@Nullable Alignment alignment, float xPos, float yPos, float zPos, float width, float height, Padding padding) {
        super(alignment, xPos, yPos, zPos, width, height, padding);
    }


    public ImmutableArea(@NotNull Area parent, float xPos, float yPos, float zPos, float width, float height) {
        super(parent, xPos, yPos, zPos, width, height);
    }


    public ImmutableArea(@NotNull Area parent, @Nullable Alignment alignment, float xPos, float yPos, float zPos, float width, float height) {
        super(parent, alignment, xPos, yPos, zPos, width, height);
    }


    public ImmutableArea(@NotNull Area parent, @Nullable Alignment alignment, float xPos, float yPos, float zPos, float width, float height, Padding padding) {
        super(parent, alignment, xPos, yPos, zPos, width, height, padding);
    }


    public ImmutableArea(int xPos, int yPos, int zPos, int width, int height) {
        super(xPos, yPos, zPos, width, height);
    }


    public ImmutableArea(@Nullable Alignment alignment, int xPos, int yPos, int zPos, int width, int height) {
        super(alignment, xPos, yPos, zPos, width, height);
    }


    public ImmutableArea(int xPos, int yPos, int zPos, int width, int height, Padding padding) {
        super(xPos, yPos, zPos, width, height, padding);
    }


    public ImmutableArea(@Nullable Alignment alignment, int xPos, int yPos, int zPos, int width, int height, Padding padding) {
        super(alignment, xPos, yPos, zPos, width, height, padding);
    }


    public ImmutableArea(@NotNull Area parent, int xPos, int yPos, int zPos, int width, int height) {
        super(parent, xPos, yPos, zPos, width, height);
    }


    public ImmutableArea(@NotNull Area parent, @Nullable Alignment alignment, int xPos, int yPos, int zPos, int width, int height) {
        super(parent, alignment, xPos, yPos, zPos, width, height);
    }


    public ImmutableArea(@NotNull Area parent, int xPos, int yPos, int zPos, int width, int height, Padding padding) {
        super(parent, xPos, yPos, zPos, width, height, padding);
    }


    public ImmutableArea(@NotNull Area parent, @Nullable Alignment alignment, int xPos, int yPos, int zPos, int width, int height, Padding padding) {
        super(parent, alignment, xPos, yPos, zPos, width, height, padding);
    }


    protected ImmutableArea(Area parent, Alignment alignment, FloatSupplier xPos, FloatSupplier yPos, FloatSupplier zPos, FloatSupplier width, FloatSupplier height, Padding padding) {
        super(parent, alignment, xPos, yPos, zPos, width, height, padding);
    }


    /**
     * @deprecated Any attempt in modifying the position, size or padding of this area will result in an {@link UnsupportedOperationException} to be thrown!
     */
    @Override
    @Deprecated
    public Area setY(float yPos) {
        throw new UnsupportedOperationException();
    }


    /**
     * @deprecated Any attempt in modifying the position, size or padding of this area will result in an {@link UnsupportedOperationException} to be thrown!
     */
    @Override
    @Deprecated
    public Area setX(float xPos) {
        throw new UnsupportedOperationException();
    }


    /**
     * @deprecated Any attempt in modifying the position, size or padding of this area will result in an {@link UnsupportedOperationException} to be thrown!
     */
    @Override
    @Deprecated
    public Area setPosition(float xPos, float yPos, float zPos) {
        throw new UnsupportedOperationException();
    }


    /**
     * @deprecated Any attempt in modifying the position, size or padding of this area will result in an {@link UnsupportedOperationException} to be thrown!
     */
    @Override
    @Deprecated
    public Area setHeight(float height) {
        throw new UnsupportedOperationException();
    }


    /**
     * @deprecated Any attempt in modifying the position, size or padding of this area will result in an {@link UnsupportedOperationException} to be thrown!
     */
    @Override
    @Deprecated
    public Area setHeight(int height) {
        throw new UnsupportedOperationException();
    }


    /**
     * @deprecated Any attempt in modifying the position, size or padding of this area will result in an {@link UnsupportedOperationException} to be thrown!
     */
    @Override
    @Deprecated
    public Area setWidth(float width) {
        throw new UnsupportedOperationException();
    }


    /**
     * @deprecated Any attempt in modifying the position, size or padding of this area will result in an {@link UnsupportedOperationException} to be thrown!
     */
    @Override
    @Deprecated
    public Area setWidth(int width) {
        throw new UnsupportedOperationException();
    }


    /**
     * @deprecated Any attempt in modifying the position, size or padding of this area will result in an {@link UnsupportedOperationException} to be thrown!
     */
    @Override
    @Deprecated
    public Area setZ(float zPos) {
        throw new UnsupportedOperationException();
    }


    /**
     * @deprecated Any attempt in modifying the position, size or padding of this area will result in an {@link UnsupportedOperationException} to be thrown!
     */
    @Override
    @Deprecated
    public Area moveX(float amount) {
        throw new UnsupportedOperationException();
    }


    /**
     * @deprecated Any attempt in modifying the position, size or padding of this area will result in an {@link UnsupportedOperationException} to be thrown!
     */
    @Override
    @Deprecated
    public Area moveY(float amount) {
        throw new UnsupportedOperationException();
    }


    /**
     * @deprecated Any attempt in modifying the position, size or padding of this area will result in an {@link UnsupportedOperationException} to be thrown!
     */
    @Override
    @Deprecated
    public Area moveZ(float amount) {
        throw new UnsupportedOperationException();
    }


    /**
     * @deprecated Any attempt in modifying the position, size or padding of this area will result in an {@link UnsupportedOperationException} to be thrown!
     */
    @Override
    @Deprecated
    public Area growX(int amount) {
        throw new UnsupportedOperationException();
    }


    /**
     * @deprecated Any attempt in modifying the position, size or padding of this area will result in an {@link UnsupportedOperationException} to be thrown!
     */
    @Override
    @Deprecated
    public Area growY(int amount) {
        throw new UnsupportedOperationException();
    }


    /**
     * @deprecated Any attempt in modifying the position, size or padding of this area will result in an {@link UnsupportedOperationException} to be thrown!
     */
    @Override
    @Deprecated
    public Area scaleX(float amount) {
        throw new UnsupportedOperationException();
    }


    /**
     * @deprecated Any attempt in modifying the position, size or padding of this area will result in an {@link UnsupportedOperationException} to be thrown!
     */
    @Override
    @Deprecated
    public Area scaleY(float amount) {
        throw new UnsupportedOperationException();
    }


    /**
     * @deprecated Any attempt in modifying the position, size or padding of this area will result in an {@link UnsupportedOperationException} to be thrown!
     */
    @Override
    @Deprecated
    public void setInnerWidth(float width) {
        throw new UnsupportedOperationException();
    }


    /**
     * @deprecated Any attempt in modifying the position, size or padding of this area will result in an {@link UnsupportedOperationException} to be thrown!
     */
    @Override
    @Deprecated
    public Area setInnerHeight(float height) {
        throw new UnsupportedOperationException();
    }


    @Override
    public ImmutableArea copy() {
        return new ImmutableArea(this.parent, this.alignment, this.xPos, this.yPos, this.zPos, this.width, this.height, this.padding);
    }


    /**
     * @deprecated Any attempt in modifying the position, size or padding of this area will result in an {@link UnsupportedOperationException} to be thrown!
     */
    @Override
    @Deprecated
    public Area setPadding(float padding) {
        throw new UnsupportedOperationException();
    }


    /**
     * @deprecated Any attempt in modifying the position, size or padding of this area will result in an {@link UnsupportedOperationException} to be thrown!
     */
    @Override
    @Deprecated
    public Area setPadding(float leftRight, float topBottom) {
        throw new UnsupportedOperationException();
    }


    /**
     * @deprecated Any attempt in modifying the position, size or padding of this area will result in an {@link UnsupportedOperationException} to be thrown!
     */
    @Override
    @Deprecated
    public Area setPadding(float left, float right, float top, float bottom) {
        throw new UnsupportedOperationException();
    }


    /**
     * @deprecated returns this
     */
    @Override
    @Deprecated
    public ImmutableArea toImmutable() {
        return this;
    }


    /**
     * @return a mutable representation of this area
     */
    public Area toMutable() {
        return new Area(this.parent, this.alignment, this.xPos, this.yPos, this.zPos, this.width, this.height, this.padding);
    }


    /**
     * @deprecated Any attempt in modifying the position, size or padding of this area will result in an {@link UnsupportedOperationException} to be thrown!
     */
    @Override
    @Deprecated
    public void set(Area other) {
        throw new UnsupportedOperationException();
    }
}
