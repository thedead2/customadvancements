package de.thedead2.customadvancements.client;

import org.jetbrains.annotations.Nullable;


public enum Alignment {

    TOP_LEFT(XAlign.LEFT, YAlign.TOP),

    TOP_CENTERED(XAlign.CENTER, YAlign.TOP),

    TOP_RIGHT(XAlign.RIGHT, YAlign.TOP),

    LEFT_CENTERED(XAlign.LEFT, YAlign.CENTER),

    CENTERED(XAlign.CENTER, YAlign.CENTER),

    RIGHT_CENTERED(XAlign.RIGHT, YAlign.CENTER),

    BOTTOM_LEFT(XAlign.LEFT, YAlign.BOTTOM),

    BOTTOM_CENTERED(XAlign.CENTER, YAlign.BOTTOM),

    BOTTOM_RIGHT(XAlign.RIGHT, YAlign.BOTTOM);


    private final XAlign xAlign;

    private final YAlign yAlign;


    Alignment(XAlign xAlign, YAlign yAlign) {
        this.xAlign = xAlign;
        this.yAlign = yAlign;
    }


    public float getXPos(@Nullable Area area, float objectWidth, float offset) {
        float x = 0;
        float width = RenderUtil.getScreenWidth();

        if (area != null) {
            x = area.getInnerX();
            width = area.getInnerWidth();
        }

        return this.getXPos(x, width, objectWidth, offset);
    }


    public float getXPos(float origin, float areaWidth, float objectWidth, float offset) {
        return this.xAlign.getXPos(origin, areaWidth, objectWidth) + offset;
    }


    public float getYPos(@Nullable Area area, float objectHeight, float offset) {
        float y = 0;
        float height = RenderUtil.getScreenHeight();

        if (area != null) {
            y = area.getInnerY();
            height = area.getInnerHeight();
        }

        return this.getYPos(y, height, objectHeight, offset);
    }


    public float getYPos(float origin, float areaHeight, float objectHeight, float offset) {
        return this.yAlign.getYPos(origin, areaHeight, objectHeight) + offset;
    }


    public XAlign getXAlign() {
        return this.xAlign;
    }


    public YAlign getYAlign() {
        return yAlign;
    }


    public enum XAlign {

        LEFT {
            @Override
            public float getXPos(float origin, float areaWidth, float objectWidth) {
                return origin;
            }
        },

        RIGHT {
            @Override
            public float getXPos(float origin, float areaWidth, float objectWidth) {
                return origin + (areaWidth - objectWidth);
            }
        },

        CENTER {
            @Override
            public float getXPos(float origin, float areaWidth, float objectWidth) {
                return origin + ((areaWidth - objectWidth) / 2);
            }
        };


        public abstract float getXPos(float origin, float areaWidth, float objectWidth);
    }


    public enum YAlign {

        TOP {
            @Override
            public float getYPos(float origin, float areaHeight, float objectHeight) {
                return origin;
            }
        },

        BOTTOM {
            @Override
            public float getYPos(float origin, float areaHeight, float objectHeight) {
                return origin + (areaHeight - objectHeight);
            }
        },

        CENTER {
            @Override
            public float getYPos(float origin, float areaHeight, float objectHeight) {
                return origin + ((areaHeight - objectHeight) / 2);
            }
        };


        public abstract float getYPos(float origin, float areaHeight, float objectHeight);
    }
}
