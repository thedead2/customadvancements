package de.thedead2.customadvancements.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;


@OnlyIn(Dist.CLIENT)
public abstract class RenderUtil {

    /**
     * Draws the given texture without transformation using a relative height to the given screenWidth and centering the texture
     **/
    public static void blitCenteredWithClipping(GuiGraphics guiGraphics, int screenWidth, int screenHeight, float ratio, ResourceLocation texture) {
        var relativeHeight = Math.round(screenWidth / ratio);
        int yStart = Math.negateExact((screenHeight - relativeHeight) / 2);
        guiGraphics.blit(texture, 0, 0, 0, yStart, screenWidth, screenHeight, screenWidth, relativeHeight);
    }

    public static void blitCenteredNoClipping(GuiGraphics guiGraphics, int width, int height, ResourceLocation texture) {
        guiGraphics.blit(texture, 0, 0, 0, 0,width, height, width, height);
    }
}
