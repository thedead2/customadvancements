package de.thedead2.customadvancements.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import de.thedead2.customadvancements.advancements.advancementtypes.IAdvancement;
import de.thedead2.customadvancements.client.RenderUtil;
import de.thedead2.customadvancements.util.core.ModHelper;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.client.gui.screens.advancements.AdvancementTab;
import net.minecraft.client.gui.screens.advancements.AdvancementWidget;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.minecraft.client.gui.GuiComponent.fill;


@Mixin(AdvancementTab.class)
public class MixinAdvancementTab {

    @Shadow @Final private Advancement advancement;


    @Shadow @Final private DisplayInfo display;


    @Shadow private double scrollX;


    @Shadow private double scrollY;


    @Shadow @Final private AdvancementWidget root;


    @Inject(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;pushPose()V", shift = At.Shift.BEFORE), method = "drawContents(Lcom/mojang/blaze3d/vertex/PoseStack;)V", cancellable = true)
    public void onDrawContents(PoseStack poseStack, CallbackInfo ci){
        ResourceLocation advancementId = this.advancement.getId();
        ResourceLocation root = new ResourceLocation(advancementId.getNamespace(), advancementId.getPath() + ".json");

        if(ModHelper.CUSTOM_ADVANCEMENTS.containsKey(root) || ModHelper.GAME_ADVANCEMENTS.containsKey(root)){
            IAdvancement advancement1 = ModHelper.CUSTOM_ADVANCEMENTS.get(root);
            if(advancement1 == null){
                advancement1 = ModHelper.GAME_ADVANCEMENTS.get(root);
            }

            if(advancement1.hasLargeBackground() && this.display.getBackground() != null){
                ci.cancel();

                poseStack.pushPose();
                poseStack.translate(0.0, 0.0, 950.0);
                RenderSystem.enableDepthTest();
                RenderSystem.colorMask(false, false, false, false);
                fill(poseStack, 4680, 2260, -4680, -2260, -16777216);
                RenderSystem.colorMask(true, true, true, true);
                poseStack.translate(0.0, 0.0, -950.0);
                RenderSystem.depthFunc(518);
                fill(poseStack, AdvancementsScreen.WINDOW_INSIDE_WIDTH, AdvancementsScreen.WINDOW_INSIDE_HEIGHT, 0, 0, -16777216);
                RenderSystem.depthFunc(515);
                ResourceLocation resourcelocation = this.display.getBackground();
                RenderSystem.setShader(GameRenderer::getPositionTexShader);
                RenderSystem.setShaderTexture(0, resourcelocation);
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                int roundedScrollX = Mth.floor(this.scrollX);
                int roundedScrollY = Mth.floor(this.scrollY);

                if(advancement1.shouldBackgroundClip()){
                    RenderUtil.blitCenteredWithClipping(poseStack, AdvancementsScreen.WINDOW_INSIDE_WIDTH, AdvancementsScreen.WINDOW_INSIDE_HEIGHT, advancement1.getBackgroundAspectRatio());
                }
                else {
                    RenderUtil.blitCenteredNoClipping(poseStack, AdvancementsScreen.WINDOW_INSIDE_WIDTH, AdvancementsScreen.WINDOW_INSIDE_HEIGHT);
                }

                this.root.drawConnectivity(poseStack, roundedScrollX, roundedScrollY, true);
                this.root.drawConnectivity(poseStack, roundedScrollX, roundedScrollY, false);
                this.root.draw(poseStack, roundedScrollX, roundedScrollY);

                RenderSystem.depthFunc(518);
                poseStack.translate(0.0, 0.0, -950.0);
                RenderSystem.colorMask(false, false, false, false);
                fill(poseStack, 4680, 2260, -4680, -2260, -16777216);
                RenderSystem.colorMask(true, true, true, true);
                RenderSystem.depthFunc(515);
                poseStack.popPose();
            }
        }
    }
}
