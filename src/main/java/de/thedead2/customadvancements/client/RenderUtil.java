package de.thedead2.customadvancements.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import static net.minecraft.client.gui.GuiComponent.blit;


@OnlyIn(Dist.CLIENT)
public abstract class RenderUtil {

    /**
     * Draws the given texture without transformation using a relative height to the given screenWidth and centering the texture
     **/
    public static void blitCenteredWithClipping(PoseStack poseStack, int screenWidth, int screenHeight, float ratio) {
        var relativeHeight = Math.round(screenWidth / ratio);
        int yStart = Math.negateExact((screenHeight - relativeHeight) / 2);
        blit(poseStack, 0, 0, 0, yStart, screenWidth, screenHeight, screenWidth, relativeHeight);
    }

    public static void blitCenteredNoClipping(PoseStack poseStack, int width, int height) {
        blit(poseStack, 0, 0, 0, 0,width, height, width, height);
    }
}
