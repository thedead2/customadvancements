package de.thedead2.customadvancements.mixin;

import betteradvancements.common.gui.BetterAdvancementTab;
import betteradvancements.common.gui.BetterAdvancementWidget;
import de.thedead2.customadvancements.client.RenderUtil;
import net.minecraft.advancements.Advancement;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Pseudo
@Mixin(BetterAdvancementTab.class)
public class MixinBetterAdvancementTab {

    @Shadow
    protected int scrollX;

    @Shadow
    protected int scrollY;

    @Shadow
    @Final
    private Advancement advancement;

    @Shadow
    @Final
    private BetterAdvancementWidget root;


    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;enableScissor(IIII)V", shift = At.Shift.BEFORE), method = "drawContents(Lnet/minecraft/client/gui/GuiGraphics;IIII)V", cancellable = true)
    public void onDrawContents(GuiGraphics guiGraphics, int left, int top, int width, int height, CallbackInfo ci) {
        RenderUtil.drawAdvancementTabBg(this.advancement, guiGraphics, ci, left, left + width, top, top + height, this.scrollX, this.scrollY, (guiGraphics1, scrollX1, scrollY1) -> {
            this.root.drawConnectivity(guiGraphics1, scrollX1, scrollY1, true);
            this.root.drawConnectivity(guiGraphics1, scrollX1, scrollY1, false);
            this.root.draw(guiGraphics1, scrollX1, scrollY1);
        });
    }
}
