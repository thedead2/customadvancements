package de.thedead2.customadvancements.client.gui;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;
import de.thedead2.customadvancements.client.gui.components.FakeAdvancementWidget;
import de.thedead2.customadvancements.client.gui.generator.AdvancementGeneratorGUI;
import de.thedead2.customadvancements.client.gui.generator.ClientAdvancementGenerator;
import net.minecraft.ResourceLocationException;
import net.minecraft.advancements.*;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static de.thedead2.customadvancements.util.ModHelper.LOGGER;

public class CriteriaInputScreen extends BasicInputScreen {

    Map<ResourceLocation, CriterionTrigger<?>> allCriteriaTriggers = ImmutableMap.copyOf(CriteriaTriggers.CRITERIA);
    Map<String, CriterionTrigger<?>> criteria = new HashMap<>();


    public CriteriaInputScreen(ClientAdvancementGenerator parent, AdvancementGeneratorGUI gui, DisplayInfo display, @NotNull FakeAdvancementWidget widget, Supplier<Integer> screenWidthSupplier, Supplier<Integer> screenHeightSupplier, Supplier<int[]> screenStartPositionSupplier) {
        super(parent, gui, display, widget, screenWidthSupplier, screenHeightSupplier, screenStartPositionSupplier);
    }

    @Override
    public void init() {
        super.init();

        /*this.addDropDownList(this.allCriteriaTriggers.keySet(), "criteriaTriggers", 45, 45, 80, () -> "", Color.green::getRGB, (editBox1 -> {
            try {
                ResourceLocation temp = new ResourceLocation(editBox1.getValue());
                var trigger = this.allCriteriaTriggers.get(temp);
                String key = temp.toString().substring(temp.toString().indexOf(":") + 1);

                if(trigger != null){
                    this.criteria.put(key, trigger);
                }
                else {
                    throw new IllegalArgumentException();
                }
            }
            catch (ResourceLocationException | IllegalArgumentException e){
                LOGGER.error("Can't find Criterion for input: {}", editBox1.getValue());
            }
        }));*/
        this.addRenderableWidget(new MultiLineEditBox(this.font, this.screenTopLeftCorner[0] + 5, this.screenTopLeftCorner[1] + 5, 100, 100, Component.literal("LOL"), Component.literal("Test")));
    }

    @Override
    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        super.render(poseStack, mouseX, mouseY, partialTick);
        this.minecraft.font.drawShadow(poseStack, this.display != null ? this.display.getTitle() : DEFAULT_TITLE, screenTopLeftCorner[0] + FRAME_LENGTH + PADDING, screenTopLeftCorner[1] + 9, -1);

        //RequirementsStrategy.AND;
        //RequirementsStrategy.OR;
    }
}
