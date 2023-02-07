package de.thedead2.customadvancements.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import de.thedead2.customadvancements.client.gui.components.FakeAdvancementWidget;
import de.thedead2.customadvancements.client.gui.components.ItemButton;
import de.thedead2.customadvancements.client.gui.generator.ClientAdvancementGenerator;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.advancements.FrameType;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.advancements.AdvancementWidgetType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class FrameAndItemSelectionScreen extends BasicInputScreen {

    private final ItemStack originalIcon;
    private final FrameType originalFrame;

    public FrameAndItemSelectionScreen(Screen parent, DisplayInfo display, FakeAdvancementWidget widget, Supplier<Integer> screenWidthSupplier, Supplier<Integer> screenHeightSupplier, Supplier<int[]> screenStartPositionSupplier) {
        super(parent, display, widget, screenWidthSupplier, screenHeightSupplier, screenStartPositionSupplier);
        this.originalIcon = this.display != null ? this.display.getIcon() : ClientAdvancementGenerator.DEFAULT_ITEM;
        this.originalFrame = this.display != null ? this.display.getFrame() : ClientAdvancementGenerator.DEFAULT_FRAME;
    }


    @Override
    public void save() {
        if(this.parent instanceof ClientAdvancementGenerator){
            ((ClientAdvancementGenerator) this.parent).updateDisplayInfo(this.display.getIcon());
            ((ClientAdvancementGenerator) this.parent).updateDisplayInfo(this.display.getFrame());
        }
    }

    @Override
    public void reset() {
        this.updateDisplayInfo(this.originalIcon);
        this.updateDisplayInfo(this.originalFrame);
    }

    @Override
    public void init() {
        super.init();

        int frames = 1;
        for(FrameType frameType : FrameType.values()){
            this.addRenderableWidget(new ImageButton(this.screenTopLeftCorner[0] + (FRAME_LENGTH + PADDING) * frames, this.screenTopLeftCorner[1] + TOP_OFFSET + 5, FRAME_LENGTH, FRAME_LENGTH, frameType.getTexture(), 128 + AdvancementWidgetType.OBTAINED.getIndex() * FRAME_LENGTH , FakeAdvancementWidget.WIDGETS_LOCATION, pButton -> {
                if(this.parent instanceof ClientAdvancementGenerator){
                    this.updateDisplayInfo(frameType);
                }
            }));
            frames++;
        }

        int column = 1;
        int row = 1;
        int maxColumns = (this.screenWidth - ((FRAME_LENGTH + PADDING)))/(16 + 10);

        for (Item item : ForgeRegistries.ITEMS){
            if(column <= maxColumns){
                this.addRenderableWidget(new ItemButton(this.screenTopLeftCorner[0] + (FRAME_LENGTH + PADDING) * column, this.screenTopLeftCorner[1] + TOP_OFFSET + 5 + (FRAME_LENGTH + 10) * row, item.getDefaultInstance(), pButton -> {
                    this.updateDisplayInfo(item.getDefaultInstance());
                }));
                column++;
            }
            else {
                column = 1;
                this.addRenderableWidget(new ItemButton(this.screenTopLeftCorner[0] + (FRAME_LENGTH + PADDING) * column, this.screenTopLeftCorner[1] + TOP_OFFSET + 5 + (FRAME_LENGTH + 10) * row, item.getDefaultInstance(), pButton -> {
                    this.updateDisplayInfo(item);
                }));
                row++;
            }
        }
    }

    @Override
    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        super.render(poseStack, mouseX, mouseY, partialTick);
    }
}
