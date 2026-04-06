package de.thedead2.customadvancements.client;

//TODO: Check if u and v are not 0, if other textures get drawn --> maybe fixable with also fixing texture repetition
public enum ObjectFit {
    /**
     * The object is resized to fill the given dimension. If necessary, the object will be stretched or squished to fit
     */
    FILL {
        @Override
        public float getUMin(TextureInfo textureInfo, Area area) {
            return textureInfo.getUMin() / area.getInnerWidth(); //start-percent of the width of the original image
        }


        @Override
        public float getUMax(TextureInfo textureInfo, Area area) {
            return (textureInfo.getUMin() + area.getInnerWidth()) / area.getInnerWidth();
        }


        @Override
        public float getVMin(TextureInfo textureInfo, Area area) {
            return textureInfo.getVMin() / area.getInnerHeight();
        }


        @Override
        public float getVMax(TextureInfo textureInfo, Area area) {
            return (textureInfo.getVMin() + area.getInnerHeight()) / area.getInnerHeight();
        }
    },
    /**
     * The object keeps its aspect ratio, but is resized to fit within the given dimension
     */
    CONTAIN {
        @Override
        public float getUMin(TextureInfo textureInfo, Area area) {
            float relativeWidth = textureInfo.getRelativeWidth(area.getInnerHeight());

            if (area.getInnerWidth() < relativeWidth) { //width fixed --> use relative height
                return textureInfo.getUMin() / area.getInnerWidth();
            }
            else { //height fixed --> use relative width
                float uStart = (area.getInnerWidth() - relativeWidth) / 2;

                return (textureInfo.getUMin() - uStart) / relativeWidth;
            }
        }


        @Override
        public float getUMax(TextureInfo textureInfo, Area area) {
            float relativeWidth = textureInfo.getRelativeWidth(area.getInnerHeight());

            if (area.getInnerWidth() < relativeWidth) { //width fixed --> use relative height
                return (textureInfo.getUMin() + area.getInnerWidth()) / area.getInnerWidth();
            }
            else { //height fixed --> use relative width
                float uStart = (area.getInnerWidth() - relativeWidth) / 2;

                return (textureInfo.getUMin() + (area.getInnerWidth() - uStart)) / relativeWidth;
            }
        }


        @Override
        public float getVMin(TextureInfo textureInfo, Area area) {
            float relativeHeight = textureInfo.getRelativeHeight(area.getInnerWidth());

            if (area.getInnerHeight() < relativeHeight) { //height fixed --> use relative width
                return textureInfo.getVMin() / area.getInnerHeight();
            }
            else { //width fixed --> use relative height
                float vStart = (area.getInnerHeight() - relativeHeight) / 2;

                return (textureInfo.getVMin() - vStart) / relativeHeight;
            }
        }


        @Override
        public float getVMax(TextureInfo textureInfo, Area area) {
            float relativeHeight = textureInfo.getRelativeHeight(area.getInnerWidth());

            if (area.getInnerHeight() < relativeHeight) { //height fixed --> use relative width
                return (textureInfo.getVMin() + area.getInnerHeight()) / area.getInnerHeight();
            }
            else { //width fixed --> use relative height
                float vStart = (area.getInnerHeight() - relativeHeight) / 2;

                return (textureInfo.getVMin() + (area.getInnerHeight() - vStart)) / relativeHeight;
            }
        }
    },
    /**
     * The object keeps its aspect ratio and fills the given dimension. The object will be clipped to fit
     */
    COVER {
        @Override
        public float getUMin(TextureInfo textureInfo, Area area) {
            float relativeWidth = textureInfo.getRelativeWidth(area.getInnerHeight());

            if (area.getInnerWidth() > relativeWidth) { //width fixed --> use relative height
                return textureInfo.getUMin() / area.getInnerWidth();
            }
            else { //height fixed --> use relative width
                float uStart = (area.getInnerWidth() - relativeWidth) / 2;

                return (textureInfo.getUMin() - uStart) / relativeWidth;
            }
        }


        @Override
        public float getUMax(TextureInfo textureInfo, Area area) {
            float relativeWidth = textureInfo.getRelativeWidth(area.getInnerHeight());

            if (area.getInnerWidth() > relativeWidth) { //width fixed --> use relative height
                return (textureInfo.getUMin() + area.getInnerWidth()) / area.getInnerWidth();
            }
            else { //height fixed --> use relative width
                float uStart = (area.getInnerWidth() - relativeWidth) / 2;

                return (textureInfo.getUMin() + (area.getInnerWidth() - uStart)) / relativeWidth;
            }
        }


        @Override
        public float getVMin(TextureInfo textureInfo, Area area) {
            float relativeHeight = textureInfo.getRelativeHeight(area.getInnerWidth());

            if (area.getInnerHeight() > relativeHeight) { //height fixed --> use relative width
                return textureInfo.getVMin() / area.getInnerHeight();
            }
            else { //width fixed --> use relative height
                float vStart = (area.getInnerHeight() - relativeHeight) / 2;

                return (textureInfo.getVMin() - vStart) / relativeHeight;
            }
        }


        @Override
        public float getVMax(TextureInfo textureInfo, Area area) {
            float relativeHeight = textureInfo.getRelativeHeight(area.getInnerWidth());

            if (area.getInnerHeight() > relativeHeight) { //height fixed --> use relative width
                return (textureInfo.getVMin() + area.getInnerHeight()) / area.getInnerHeight();
            }
            else { //width fixed --> use relative height
                float vStart = (area.getInnerHeight() - relativeHeight) / 2;

                return (textureInfo.getVMin() + (area.getInnerHeight() - vStart)) / relativeHeight;
            }
        }
    },
    /**
     * The object is not resized and keeps its original width and height
     */
    NONE {
        @Override
        public float getUMin(TextureInfo textureInfo, Area area) {
            float uStart = (area.getInnerWidth() - textureInfo.getTextureWidth()) / 2;

            return (textureInfo.getUMin() - uStart) / textureInfo.getTextureWidth(); //start-percent of the width of the original image
        }


        @Override
        public float getUMax(TextureInfo textureInfo, Area area) {
            float uStart = (area.getInnerWidth() - textureInfo.getTextureWidth()) / 2;

            return (textureInfo.getUMin() + (area.getInnerWidth() - uStart)) / textureInfo.getTextureWidth();
        }


        @Override
        public float getVMin(TextureInfo textureInfo, Area area) {
            float vStart = (area.getInnerHeight() - textureInfo.getTextureHeight()) / 2;

            return (textureInfo.getVMin() - vStart) / textureInfo.getTextureHeight();
        }


        @Override
        public float getVMax(TextureInfo textureInfo, Area area) {
            float vStart = (area.getInnerHeight() - textureInfo.getTextureHeight()) / 2;

            return (textureInfo.getVMin() + (area.getInnerHeight() - vStart)) / textureInfo.getTextureHeight();
        }
    },
    /**
     * The object is scaled down to the smallest version of {@link #NONE} or {@link #CONTAIN}
     */
    SCALE_DOWN {
        @Override
        public float getUMin(TextureInfo textureInfo, Area area) {
            float noneUMin = NONE.getUMin(textureInfo, area);
            float containUMin = CONTAIN.getUMin(textureInfo, area);
            float noneUMax = NONE.getUMax(textureInfo, area);
            float containUMax = CONTAIN.getUMax(textureInfo, area);

            if ((noneUMax - noneUMin) > (containUMax - containUMin)) {
                return noneUMin;
            }
            else {
                return containUMin;
            }
        }


        @Override
        public float getUMax(TextureInfo textureInfo, Area area) {
            float noneUMin = NONE.getUMin(textureInfo, area);
            float containUMin = CONTAIN.getUMin(textureInfo, area);
            float noneUMax = NONE.getUMax(textureInfo, area);
            float containUMax = CONTAIN.getUMax(textureInfo, area);

            if ((noneUMax - noneUMin) > (containUMax - containUMin)) {
                return noneUMax;
            }
            else {
                return containUMax;
            }
        }


        @Override
        public float getVMin(TextureInfo textureInfo, Area area) {
            float noneVMin = NONE.getVMin(textureInfo, area);
            float containVMin = CONTAIN.getVMin(textureInfo, area);
            float noneVMax = NONE.getVMax(textureInfo, area);
            float containVMax = CONTAIN.getVMax(textureInfo, area);

            if ((noneVMax - noneVMin) > (containVMax - containVMin)) {
                return noneVMin;
            }
            else {
                return containVMin;
            }
        }


        @Override
        public float getVMax(TextureInfo textureInfo, Area area) {
            float noneVMin = NONE.getVMin(textureInfo, area);
            float containVMin = CONTAIN.getVMin(textureInfo, area);
            float noneVMax = NONE.getVMax(textureInfo, area);
            float containVMax = CONTAIN.getVMax(textureInfo, area);

            if ((noneVMax - noneVMin) > (containVMax - containVMin)) {
                return noneVMax;
            }
            else {
                return containVMax;
            }
        }
    };


    public abstract float getUMin(TextureInfo textureInfo, Area area);


    public abstract float getUMax(TextureInfo textureInfo, Area area);


    public abstract float getVMin(TextureInfo textureInfo, Area area);


    public abstract float getVMax(TextureInfo textureInfo, Area area);
}
