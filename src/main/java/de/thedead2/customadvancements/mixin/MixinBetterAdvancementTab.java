package de.thedead2.customadvancements.mixin;

import betteradvancements.gui.BetterAdvancementWidget;
import com.mojang.blaze3d.systems.RenderSystem;
import de.thedead2.customadvancements.advancements.advancementtypes.IAdvancement;
import de.thedead2.customadvancements.client.RenderUtil;
import de.thedead2.customadvancements.util.core.ModHelper;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "betteradvancements.gui.BetterAdvancementTab")
public class MixinBetterAdvancementTab {

    @Shadow @Final private Advancement advancement;


    @Shadow @Final private DisplayInfo display;


    @Shadow protected int scrollX;


    @Shadow protected int scrollY;


    @Shadow @Final private BetterAdvancementWidget root;


    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;enableScissor(IIII)V", shift = At.Shift.BEFORE), method = "drawContents(Lnet/minecraft/client/gui/GuiGraphics;IIII)V", cancellable = true)
    public void onDrawContents(GuiGraphics guiGraphics, int left, int top, int width, int height, CallbackInfo ci){
        ResourceLocation advancementId = this.advancement.getId();
        ResourceLocation root = new ResourceLocation(advancementId.getNamespace(), advancementId.getPath() + ".json");

        if(ModHelper.CUSTOM_ADVANCEMENTS.containsKey(root) || ModHelper.GAME_ADVANCEMENTS.containsKey(root)){
            IAdvancement advancement1 = ModHelper.CUSTOM_ADVANCEMENTS.get(root);
            if(advancement1 == null){
                advancement1 = ModHelper.GAME_ADVANCEMENTS.get(root);
            }

            if(advancement1.hasLargeBackground() && this.display.getBackground() != null){
                ci.cancel();

                guiGraphics.enableScissor(left, top, left + width, top + height);
                RenderSystem.enableBlend();
                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate((float)left, (float)top, 0.0F);
                ResourceLocation background = this.display.getBackground();
                int roundedScrollX = Mth.floor(this.scrollX);
                int roundedScrollY = Mth.floor(this.scrollY);

                if(advancement1.shouldBackgroundClip()){
                    RenderUtil.blitCenteredWithClipping(guiGraphics, width, height, advancement1.getBackgroundAspectRatio(), background);
                }
                else {
                    RenderUtil.blitCenteredNoClipping(guiGraphics, width, height, background);
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
