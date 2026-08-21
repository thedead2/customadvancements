package de.thedead2.customadvancements.client;

import com.mojang.blaze3d.systems.RenderSystem;
import de.thedead2.customadvancements.client.gui.*;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementNode;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


public class ClientRenderer {

    public static void drawAdvancementTabBg(AdvancementNode rootNode, GuiGraphics guiGraphics, CallbackInfo ci, int xMin, int xMax, int yMin, int yMax, double scrollX, double scrollY, RootRenderer rootRenderer) {
        AdvancementHolder advancementHolder = rootNode.holder();
        Advancement advancement = rootNode.advancement();

        ResourceLocation advancementId = advancementHolder.id();
        ResourceLocation root = ResourceLocation.tryBuild(advancementId.getNamespace(), advancementId.getPath() + ".json");

        if (advancement.display().isEmpty()) {
            return;
        }

        if (ClientSyncHandler.BACKGROUND_RENDERERS.containsKey(root)) {
            IBackgroundRenderer backgroundRenderer = ClientSyncHandler.BACKGROUND_RENDERERS.get(root);

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
}
