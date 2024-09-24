package de.thedead2.customadvancements.client;

public class Padding {

    public static final Padding NONE = new Padding(0);

    private final FloatSupplier paddingLeft;

    private final FloatSupplier paddingRight;

    private final FloatSupplier paddingTop;

    private final FloatSupplier paddingBottom;


    public Padding(float padding) {
        this(padding, padding);
    }


    public Padding(float paddingLeftRight, float paddingTopBottom) {
        this(paddingLeftRight, paddingLeftRight, paddingTopBottom, paddingTopBottom);
    }


    public Padding(float paddingLeft, float paddingRight, float paddingTop, float paddingBottom) {
        this(() -> paddingLeft, () -> paddingRight, () -> paddingTop, () -> paddingBottom);
    }


    public Padding(FloatSupplier paddingLeft, FloatSupplier paddingRight, FloatSupplier paddingTop, FloatSupplier paddingBottom) {
        this.paddingLeft = paddingLeft;
        this.paddingRight = paddingRight;
        this.paddingTop = paddingTop;
        this.paddingBottom = paddingBottom;
    }


    public Padding(FloatSupplier padding) {
        this(padding, padding);
    }


    public Padding(FloatSupplier paddingLeftRight, FloatSupplier paddingTopBottom) {
        this(paddingLeftRight, paddingLeftRight, paddingTopBottom, paddingTopBottom);
    }


    public float getLeft() {
        return paddingLeft.getAsFloat();
    }


    public float getTop() {
        return paddingTop.getAsFloat();
    }


    public float getRight() {
        return paddingRight.getAsFloat();
    }


    public float getBottom() {
        return paddingBottom.getAsFloat();
    }
}
