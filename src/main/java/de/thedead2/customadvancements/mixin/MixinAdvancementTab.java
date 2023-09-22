package de.thedead2.customadvancements.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import de.thedead2.customadvancements.advancements.advancementtypes.IAdvancement;
import de.thedead2.customadvancements.client.RenderUtil;
import de.thedead2.customadvancements.util.core.ModHelper;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.advancements.AdvancementTab;
import net.minecraft.client.gui.screens.advancements.AdvancementWidget;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(AdvancementTab.class)
public class MixinAdvancementTab {

    @Shadow @Final private Advancement advancement;


    @Shadow @Final private DisplayInfo display;


    @Shadow private double scrollX;


    @Shadow private double scrollY;


    @Shadow @Final private AdvancementWidget root;


    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;enableScissor(IIII)V", shift = At.Shift.BEFORE), method = "drawContents(Lnet/minecraft/client/gui/GuiGraphics;II)V", cancellable = true)
    public void onDrawContents(GuiGraphics guiGraphics, int pX, int pY, CallbackInfo ci){
        ResourceLocation advancementId = this.advancement.getId();
        ResourceLocation root = new ResourceLocation(advancementId.getNamespace(), advancementId.getPath() + ".json");

        if(ModHelper.CUSTOM_ADVANCEMENTS.containsKey(root) || ModHelper.GAME_ADVANCEMENTS.containsKey(root)){
            IAdvancement advancement1 = ModHelper.CUSTOM_ADVANCEMENTS.get(root);
            if(advancement1 == null){
                advancement1 = ModHelper.GAME_ADVANCEMENTS.get(root);
            }

            if(advancement1.hasLargeBackground() && this.display.getBackground() != null){
                ci.cancel();

                guiGraphics.enableScissor(pX, pY, pX + AdvancementsScreen.WINDOW_INSIDE_WIDTH, pY + AdvancementsScreen.WINDOW_INSIDE_HEIGHT);
                RenderSystem.enableBlend();
                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate((float)pX, (float)pY, 0.0F);
                ResourceLocation background = this.display.getBackground();
                int roundedScrollX = Mth.floor(this.scrollX);
                int roundedScrollY = Mth.floor(this.scrollY);

                if(advancement1.shouldBackgroundClip()){
                    RenderUtil.blitCenteredWithClipping(guiGraphics, AdvancementsScreen.WINDOW_INSIDE_WIDTH, AdvancementsScreen.WINDOW_INSIDE_HEIGHT, advancement1.getBackgroundAspectRatio(), background);
                }
                else {
                    RenderUtil.blitCenteredNoClipping(guiGraphics, AdvancementsScreen.WINDOW_INSIDE_WIDTH, AdvancementsScreen.WINDOW_INSIDE_HEIGHT, background);
                }

                this.root.drawConnectivity(guiGraphics, roundedScrollX, roundedScrollY, true);
                this.root.drawConnectivity(guiGraphics, roundedScrollX, roundedScrollY, false);
                this.root.draw(guiGraphics, roundedScrollX, roundedScrollY);
                guiGraphics.pose().popPose();
                RenderSystem.disableBlend();
                guiGraphics.disableScissor();
            }
        }
    }
}
