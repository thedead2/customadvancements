package de.thedead2.customadvancements.mixin;

import de.thedead2.customadvancements.client.RenderUtil;
import net.minecraft.advancements.Advancement;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.advancements.AdvancementTab;
import net.minecraft.client.gui.screens.advancements.AdvancementWidget;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(AdvancementTab.class)
public class MixinAdvancementTab {

    @Shadow
    @Final
    private Advancement advancement;

    @Shadow
    private double scrollX;

    @Shadow
    private double scrollY;

    @Shadow
    @Final
    private AdvancementWidget root;


    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;enableScissor(IIII)V", shift = At.Shift.BEFORE), method = "drawContents(Lnet/minecraft/client/gui/GuiGraphics;II)V", cancellable = true)
    public void onDrawContents(GuiGraphics guiGraphics, int pX, int pY, CallbackInfo ci) {
        RenderUtil.drawAdvancementTabBg(this.advancement, guiGraphics, ci, pX, pX + AdvancementsScreen.WINDOW_INSIDE_WIDTH, pY, pY + AdvancementsScreen.WINDOW_INSIDE_HEIGHT, this.scrollX, this.scrollY, (guiGraphics1, scrollX1, scrollY1) -> {
            this.root.drawConnectivity(guiGraphics, scrollX1, scrollY1, true);
            this.root.drawConnectivity(guiGraphics, scrollX1, scrollY1, false);
            this.root.draw(guiGraphics, scrollX1, scrollY1);
        });
    }
}
