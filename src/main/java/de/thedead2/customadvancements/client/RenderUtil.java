package de.thedead2.customadvancements.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import de.thedead2.customadvancements.advancements.CustomAdvancement;
import de.thedead2.customadvancements.util.core.ModHelper;
import net.minecraft.advancements.Advancement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector2d;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;


@OnlyIn(Dist.CLIENT)
public class RenderUtil {

    /**
     * Rotates the given {@link Matrix4f} around the given point
     *
     * @param matrix4f the {@link Matrix4f} to rotate
     * @param rotation the rotation in form of a {@link Quaternionf} to apply to the {@link PoseStack}
     * @param anchor   the rotation point in form of a {@link Vector3f}
     **/
    public static void rotateAround(Matrix4f matrix4f, Quaternionf rotation, Vector3f anchor) {
        rotateAround(matrix4f, rotation, anchor.x, anchor.y, anchor.z);
    }


    /**
     * Rotates the given {@link Matrix4f} around the given point
     *
     * @param matrix4f the {@link Matrix4f} to rotate
     * @param rotation the rotation in form of a {@link Quaternionf} to apply to the {@link PoseStack}
     * @param pX       the x coordinate of the rotation point
     * @param pY       the y coordinate of the rotation point
     * @param pZ       the z coordinate of the rotation point
     **/
    public static void rotateAround(Matrix4f matrix4f, Quaternionf rotation, float pX, float pY, float pZ) {
        matrix4f.rotateAround(rotation, pX, pY, pZ);
    }


    /**
     * Scales the given {@link Matrix4f} around the given point
     *
     * @param matrix4f the {@link Matrix4f} to scale
     * @param scale    the scaling factor in form of a {@link Vector3f}
     * @param anchor   the scaling point in form of a {@link Vector3f}
     **/
    public static void scaleAround(Matrix4f matrix4f, Vector3f scale, Vector3f anchor) {
        scaleAround(matrix4f, scale.x, scale.y, scale.z, anchor.x, anchor.y, anchor.z);
    }


    /**
     * Scales the given {@link Matrix4f} around the given point
     *
     * @param matrix4f the {@link Matrix4f} to scale
     * @param scaleX   the scale factor of the x-axis
     * @param scaleY   the scale factor of the y-axis
     * @param scaleZ   the scale factor of the z-axis
     * @param pX       the x coordinate of the scaling point
     * @param pY       the y coordinate of the scaling point
     * @param pZ       the z coordinate of the scaling point
     **/
    public static void scaleAround(Matrix4f matrix4f, float scaleX, float scaleY, float scaleZ, float pX, float pY, float pZ) {
        matrix4f.scaleAround(scaleX, scaleY, scaleZ, pX, pY, pZ);
    }


    public static Vector3f getScreenCenter() {
        return new Vector3f(getScreenWidth() / 2, getScreenHeight() / 2, 0);
    }


    public static float getScreenWidth() {
        return Minecraft.getInstance().getWindow().getGuiScaledWidth();
    }


    public static float getScreenHeight() {
        return Minecraft.getInstance().getWindow().getGuiScaledHeight();
    }


    public static int convertColor(int[] colorHolder) {
        return ((colorHolder[3] & 255) << 24) |
                ((colorHolder[0] & 255) << 16) |
                ((colorHolder[1] & 255) << 8) |
                ((colorHolder[2] & 255));
    }


    public static void drawAdvancementTabBg(Advancement advancement, GuiGraphics guiGraphics, CallbackInfo ci, int xMin, int xMax, int yMin, int yMax, double scrollX, double scrollY, RootRenderer rootRenderer) {
        ResourceLocation advancementId = advancement.getId();
        ResourceLocation root = new ResourceLocation(advancementId.getNamespace(), advancementId.getPath() + ".json");

        if (advancement.getDisplay() == null) {
            return;
        }

        if (ModHelper.CUSTOM_ADVANCEMENTS.containsKey(root)) {
            CustomAdvancement advancement1 = ModHelper.CUSTOM_ADVANCEMENTS.get(root);
            IBackgroundRenderer backgroundRenderer = advancement1.getBackgroundRenderer();

            if (backgroundRenderer != null) {
                ci.cancel();

                int roundedScrollX = Mth.floor(scrollX);
                int roundedScrollY = Mth.floor(scrollY);

                guiGraphics.enableScissor(xMin, yMin, xMax, yMax);
                RenderSystem.enableBlend();

                backgroundRenderer.drawBg(guiGraphics, xMin, xMax, yMin, yMax, roundedScrollX, roundedScrollY);

                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate((float) xMin, (float) yMin, 0.0F);

                rootRenderer.draw(guiGraphics, roundedScrollX, roundedScrollY);

                guiGraphics.pose().popPose();
                RenderSystem.disableBlend();
                guiGraphics.disableScissor();
            }
        }
    }


    public static void renderImage(GuiGraphics guiGraphics, TextureInfo textureInfo, Area area, float[] colorShift) {
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
        RenderSystem.setShaderTexture(0, textureInfo.getTextureLocation());

        final ObjectFit objectFit = textureInfo.getObjectFit();
        float uMin = objectFit.getUMin(textureInfo, area); //start-percent of the width of the original image
        float vMin = objectFit.getVMin(textureInfo, area); //start-percent of the height of the original image
        float uMax = objectFit.getUMax(textureInfo, area); //end-percent of the width of the original image --> percent of how much of the specified width of the area to render in should be filled with the image
        float vMax = objectFit.getVMax(textureInfo, area); //end-percent of the height of the original image

        float xMin = area.getInnerX();
        float yMin = area.getInnerY();
        float xMax = area.getInnerXMax();
        float yMax = area.getInnerYMax();
        float zPos = area.getZ();

        if (objectFit == ObjectFit.CONTAIN) {
            xMin = xMin - area.getInnerWidth() * uMin; //Mth.clamp(, 0, area.getInnerWidth() / 2);
            yMin = yMin - area.getInnerHeight() * vMin; //Mth.clamp(, 0, area.getInnerHeight() / 2);
            xMax = xMax - Mth.clamp(area.getInnerWidth() * (uMax - 1), 0, area.getInnerWidth() / 2);
            yMax = yMax - Mth.clamp(area.getInnerHeight() * (vMax - 1), 0, area.getInnerHeight() / 2);

            uMin = textureInfo.getUMin();
            uMax = 1;
            vMin = textureInfo.getVMin();
            vMax = 1;
        }

        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuilder();
        Matrix4f matrix = guiGraphics.pose().last().pose();

        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);
        bufferbuilder.vertex(matrix, xMin, yMax, zPos).color(colorShift[0], colorShift[1], colorShift[2], colorShift[3]).uv(uMin, vMax).endVertex();
        bufferbuilder.vertex(matrix, xMax, yMax, zPos).color(colorShift[0], colorShift[1], colorShift[2], colorShift[3]).uv(uMax, vMax).endVertex();
        bufferbuilder.vertex(matrix, xMax, yMin, zPos).color(colorShift[0], colorShift[1], colorShift[2], colorShift[3]).uv(uMax, vMin).endVertex();
        bufferbuilder.vertex(matrix, xMin, yMin, zPos).color(colorShift[0], colorShift[1], colorShift[2], colorShift[3]).uv(uMin, vMin).endVertex();
        tessellator.end();

        RenderSystem.disableBlend();
    }


    public static void linearGradient(GuiGraphics guiGraphics, float xMin, float xMax, float yMin, float yMax, float zPos, float degrees, GradientColor... colors) {
        guiGraphics.pose().pushPose();
        //guiGraphics.enableScissor(Math.round(xMin), Math.round(yMin), Math.round(xMax), Math.round(yMax));
        Arrays.sort(colors);

        float height = yMax - yMin;
        float width = xMax - xMin;
        float scale = scaleToFit(width, height, degrees);

        Vector3f center = getCenter(xMin, yMin, zPos, width, height);
        rotateAround(guiGraphics.pose(), Axis.ZP.rotationDegrees(degrees), center);
        scaleAround(guiGraphics.pose(), new Vector3f(scale, scale, 1), center);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.getBuilder();

        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        fillLinearGradient(guiGraphics.pose().last().pose(), bufferbuilder, xMin, xMin + width, yMin, yMin + height, zPos, height, colors);
        tesselator.end();

        RenderSystem.disableBlend();
        //RenderSystem.disableScissor();
        guiGraphics.pose().popPose();
    }


    /**
     * @return a scale factor to scale an object with a given rotation to fit to the bounding box defined by the given width and height, so that there is no empty space within the bounding box
     */
    public static float scaleToFit(float width, float height, float degrees) {
        double radians = Math.toRadians(degrees);
        double sin = Math.abs(Math.sin(radians));
        double cos = Math.abs(Math.cos(radians));

        float W = (float) (width * cos + height * sin);
        float H = (float) (width * sin + height * cos);

        return Math.max(W / width, H / height);
    }


    public static Vector3f getCenter(float xPos, float yPos, float zPos, float objectWidth, float objectHeight) {
        float a = objectWidth / 2;
        float b = objectHeight / 2;

        return new Vector3f(xPos + a, yPos + b, zPos);
    }


    /**
     * Rotates the given {@link PoseStack} around the given point
     *
     * @param poseStack the {@link PoseStack} to rotate
     * @param rotation  the rotation in form of a {@link Quaternionf} to apply to the {@link PoseStack}
     * @param anchor    the rotation point in form of a {@link Vector3f}
     **/
    public static void rotateAround(PoseStack poseStack, Quaternionf rotation, Vector3f anchor) {
        rotateAround(poseStack, rotation, anchor.x, anchor.y, anchor.z);
    }


    /**
     * Scales the given {@link PoseStack} around the given point
     *
     * @param poseStack the {@link PoseStack} to scale
     * @param scale     the scaling factor in form of a {@link Vector3f}
     * @param anchor    the scaling point in form of a {@link Vector3f}
     **/
    public static void scaleAround(PoseStack poseStack, Vector3f scale, Vector3f anchor) {
        scaleAround(poseStack, scale.x, scale.y, scale.z, anchor.x, anchor.y, anchor.z);
    }


    private static void fillLinearGradient(Matrix4f matrix, VertexConsumer builder, float xMin, float xMax, float yMin, float yMax, float zPos, float height, GradientColor... colors) {
        GradientColor first = colors[0];

        _fillColor(matrix, builder, xMin, xMax, yMin, yMin + first.stopPercent() * height, zPos, first.red(), first.green(), first.blue(), first.alpha());

        for (int i = 0; i < colors.length; i++) {
            GradientColor colorA = colors[i];
            GradientColor colorB = (i + 1) < colors.length ? colors[i + 1] : null;
            float aYPos = yMin + colorA.stopPercent() * height;
            float bYPos = colorB == null ? yMax : yMin + colorB.stopPercent() * height;

            if (colorB != null) {
                _fillGradient(matrix, builder, xMin, xMax, aYPos, bYPos, zPos, colorA.red(), colorA.green(), colorA.blue(), colorA.alpha(), colorB.red(), colorB.green(), colorB.blue(), colorB.alpha());
            }
        }

        GradientColor last = colors[colors.length - 1];

        _fillColor(matrix, builder, xMin, xMax, yMin + last.stopPercent() * height, yMax, zPos, last.red(), last.green(), last.blue(), last.alpha());
    }


    /**
     * Rotates the given {@link PoseStack} around the given point
     *
     * @param poseStack the {@link PoseStack} to rotate
     * @param rotation  the rotation in form of a {@link Quaternionf} to apply to the {@link PoseStack}
     * @param pX        the x coordinate of the rotation point
     * @param pY        the y coordinate of the rotation point
     * @param pZ        the z coordinate of the rotation point
     **/
    public static void rotateAround(PoseStack poseStack, Quaternionf rotation, float pX, float pY, float pZ) {
        Matrix4f matrix4f = new Matrix4f();

        matrix4f.rotateAround(rotation, pX, pY, pZ);
        poseStack.mulPoseMatrix(matrix4f);
    }


    /**
     * Scales the given {@link PoseStack} around the given point
     *
     * @param poseStack the {@link PoseStack} to scale
     * @param scaleX    the scale factor of the x-axis
     * @param scaleY    the scale factor of the y-axis
     * @param scaleZ    the scale factor of the z-axis
     * @param pX        the x coordinate of the scaling point
     * @param pY        the y coordinate of the scaling point
     * @param pZ        the z coordinate of the scaling point
     **/
    public static void scaleAround(PoseStack poseStack, float scaleX, float scaleY, float scaleZ, float pX, float pY, float pZ) {
        Matrix4f matrix4f = new Matrix4f();

        matrix4f.scaleAround(scaleX, scaleY, scaleZ, pX, pY, pZ);
        poseStack.mulPoseMatrix(matrix4f);
    }


    private static void _fillColor(Matrix4f matrix, VertexConsumer builder, float xMin, float xMax, float yMin, float yMax, float zPos, int red, int green, int blue, int alpha) {
        _fillGradient(matrix, builder, xMin, xMax, yMin, yMax, zPos, red, green, blue, alpha, red, green, blue, alpha);
    }


    private static void _fillGradient(Matrix4f matrix, VertexConsumer builder, float xMin, float xMax, float yMin, float yMax, float zPos, int redA, int greenA, int blueA, int alphaA, int redB, int greenB, int blueB, int alphaB) {
        builder.vertex(matrix, xMax, yMin, zPos).color(redA, greenA, blueA, alphaA).endVertex();
        builder.vertex(matrix, xMin, yMin, zPos).color(redA, greenA, blueA, alphaA).endVertex();
        builder.vertex(matrix, xMin, yMax, zPos).color(redB, greenB, blueB, alphaB).endVertex();
        builder.vertex(matrix, xMax, yMax, zPos).color(redB, greenB, blueB, alphaB).endVertex();
    }


    public static void radialGradient(GuiGraphics guiGraphics, float xMin, float xMax, float yMin, float yMax, float zPos, GradientColor... colors) {
        guiGraphics.pose().pushPose();

        Arrays.sort(colors);
        //guiGraphics.enableScissor(Math.round(xMin), Math.round(yMin), Math.round(xMax), Math.round(yMax));

        float height = yMax - yMin;
        float width = xMax - xMin;
        float diagonal = (float) Math.sqrt((height * height) + (width * width));

        Vector3f center = getCenter(xMin, yMin, zPos, width, height);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.getBuilder();

        bufferbuilder.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        fillRadialGradient(guiGraphics.pose().last().pose(), bufferbuilder, center.x, center.y, zPos, diagonal / 2, colors);
        tesselator.end();

        RenderSystem.disableBlend();
        //RenderSystem.disableScissor();

        guiGraphics.pose().popPose();
    }


    private static void fillRadialGradient(Matrix4f matrix, BufferBuilder bufferBuilder, float xPos, float yPos, float zPos, float radius, GradientColor... colors) {
        GradientColor first = colors[0];

        _fillCircleOutline(matrix, bufferBuilder, xPos, yPos, zPos, 0, 0, radius * first.stopPercent(), radius * first.stopPercent(), 0, 1f, first.red(), first.green(), first.blue(), first.alpha());

        for (int i = 0; i < colors.length; i++) {
            GradientColor colorA = colors[i];
            GradientColor colorB = (i + 1) < colors.length ? colors[i + 1] : null;

            float innerRadius = colorA.stopPercent() * radius;
            float outerRadius = colorB != null ? colorB.stopPercent() * radius : radius;

            if (colorB != null) {
                _fillCircleOutlineGradient(matrix, bufferBuilder, xPos, yPos, zPos, innerRadius, innerRadius, outerRadius, outerRadius, 0, 1f, colorA.red(), colorA.green(), colorA.blue(), colorA.alpha(), colorB.red(), colorB.green(), colorB.blue(), colorB.alpha());
            }
        }

        GradientColor last = colors[colors.length - 1];

        _fillCircleOutline(matrix, bufferBuilder, xPos, yPos, zPos, last.stopPercent() * radius, last.stopPercent() * radius, radius, radius, 0, 1f, last.red(), last.green(), last.blue(), last.alpha());
    }


    private static void _fillCircleOutline(Matrix4f matrix, VertexConsumer bufferBuilder, float xPos, float yPos, float zPos, float innerXRadius, float innerYRadius, float outerXRadius, float outerYRadius, float startPercent, float percentFilled, int red, int green, int blue, int alpha) {
        _fillCircleOutlineGradient(matrix, bufferBuilder, xPos, yPos, zPos, innerXRadius, innerYRadius, outerXRadius, outerYRadius, startPercent, percentFilled, red, green, blue, alpha, red, green, blue, alpha);
    }


    private static void _fillCircleOutlineGradient(Matrix4f matrix, VertexConsumer bufferBuilder, float xPos, float yPos, float zPos, float innerXRadius, float innerYRadius, float outerXRadius, float outerYRadius, float startPercent, float percentFilled, int redA, int greenA, int blueA, int alphaA, int redB, int greenB, int blueB, int alphaB) {
        final int vertices = Mth.floor(percentFilled * 100);

        for (int i = vertices; i >= 0; --i) {
            float f = (float) (((startPercent * 100) + (percentFilled * 100) * (double) i / vertices) * (2 * Math.PI) / 100);
            float sin = (float) Math.sin(f);
            float cos = (float) Math.cos(f);
            float outerXOffset = sin * outerXRadius;
            float outerYOffset = cos * outerYRadius;
            float innerXOffset = sin * innerXRadius;
            float innerYOffset = cos * innerYRadius;

            bufferBuilder.vertex(matrix, xPos + innerXOffset, yPos - innerYOffset, zPos).color(redA, greenA, blueA, alphaA).endVertex();
            bufferBuilder.vertex(matrix, xPos + outerXOffset, yPos - outerYOffset, zPos).color(redB, greenB, blueB, alphaB).endVertex();
        }
    }


    public static void radialGradient(GuiGraphics guiGraphics, float xPos, float yPos, float zPos, float radius, GradientColor... colors) {
        guiGraphics.pose().pushPose();

        Arrays.sort(colors);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.getBuilder();

        bufferbuilder.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        fillRadialGradient(guiGraphics.pose().last().pose(), bufferbuilder, xPos, yPos, zPos, radius, colors);
        tesselator.end();

        RenderSystem.disableBlend();

        guiGraphics.pose().popPose();
    }


    public static Vector2d getMousePos() {
        Minecraft minecraft = Minecraft.getInstance();
        double mouseX = (minecraft.mouseHandler.xpos() * minecraft.getWindow().getGuiScaledWidth() / minecraft.getWindow().getScreenWidth());
        double mouseY = (minecraft.mouseHandler.ypos() * minecraft.getWindow().getGuiScaledHeight() / minecraft.getWindow().getScreenHeight());

        return new Vector2d(mouseX, mouseY);
    }


    public static int changeAlpha(int color, float alphaPercent) {
        return Math.max(Math.round(alphaPercent * 255), 4) << 24 | color & 0xFFFFFF; //TODO: Find out why the alpha of the color stays by one when under 4???
    }


    public static void horizontalLine(PoseStack poseStack, float xMin, float xMax, float yPos, float zPos, float lineWidth, int color) {
        if (xMax < xMin) {
            float i = xMin;
            xMin = xMax;
            xMax = i;
        }

        fill(poseStack, xMin, xMax, yPos - lineWidth / 2, yPos + lineWidth / 2, zPos, color);
    }


    public static void verticalLine(PoseStack poseStack, float xPos, float yMin, float yMax, float zPos, float lineWidth, int color) {
        if (yMax < yMin) {
            float i = yMin;
            yMin = yMax;
            yMax = i;
        }

        fill(poseStack, xPos - lineWidth / 2, xPos + lineWidth / 2, yMin, yMax, zPos, color);
    }


    public static void fill(PoseStack poseStack, float xMin, float xMax, float yMin, float yMax, float z, int color) {
        if (xMin < xMax) {
            float i = xMin;
            xMin = xMax;
            xMax = i;
        }

        if (yMin < yMax) {
            float j = yMin;
            yMin = yMax;
            yMax = j;
        }

        int alpha = (color >> 24 & 255);
        int red = (color >> 16 & 255);
        int green = (color >> 8 & 255);
        int blue = (color & 255);
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        _fillColor(poseStack.last().pose(), bufferbuilder, xMin, xMax, yMin, yMax, z, red, green, blue, alpha);
        BufferUploader.drawWithShader(bufferbuilder.end());

        RenderSystem.disableBlend();
    }


    public static void renderArea(PoseStack poseStack, Area area, int outerColor, int innerColor) {
        renderSquareOutline(poseStack, area.getX(), area.getXMax(), area.getY(), area.getYMax(), area.getZ(), outerColor);
        renderSquareOutline(poseStack, area.getInnerX(), area.getInnerXMax(), area.getInnerY(), area.getInnerYMax(), area.getZ(), innerColor);
    }


    public static void renderSquareOutline(PoseStack poseStack, float xMin, float xMax, float yMin, float yMax, float zPos, int color) {
        horizontalLine(poseStack, xMin, xMax, yMin, zPos, 1, color);
        horizontalLine(poseStack, xMin, xMax, yMax, zPos, 1, color);
        verticalLine(poseStack, xMin, yMin, yMax, zPos, 1, color);
        verticalLine(poseStack, xMax, yMin, yMax, zPos, 1, color);
    }


    @FunctionalInterface
    public interface RootRenderer {

        void draw(GuiGraphics guiGraphics, int scrollX, int scrollY);
    }
}
