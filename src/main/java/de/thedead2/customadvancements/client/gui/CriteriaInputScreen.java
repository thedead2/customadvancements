package de.thedead2.customadvancements.client.gui;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;
import de.thedead2.customadvancements.client.gui.components.FakeAdvancementWidget;
import de.thedead2.customadvancements.client.gui.generator.AdvancementGeneratorGUI;
import de.thedead2.customadvancements.client.gui.generator.ClientAdvancementGenerator;
import de.thedead2.customadvancements.util.handler.CriteriaConditionsIdentifier;
import net.minecraft.ResourceLocationException;
import net.minecraft.advancements.*;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static de.thedead2.customadvancements.util.ModHelper.LOGGER;

public class CriteriaInputScreen extends BasicInputScreen {

    Map<ResourceLocation, CriterionTrigger<?>> allCriteriaTriggers = ImmutableMap.copyOf(CriteriaTriggers.CRITERIA);
    Map<String, CriterionTrigger<?>> criteria = new HashMap<>();
    List<? extends Widget> inputFields = new ArrayList<>();


    public CriteriaInputScreen(ClientAdvancementGenerator parent, AdvancementGeneratorGUI gui, DisplayInfo display, @NotNull FakeAdvancementWidget widget, Supplier<Integer> screenWidthSupplier, Supplier<Integer> screenHeightSupplier, Supplier<int[]> screenStartPositionSupplier) {
        super(parent, gui, display, widget, screenWidthSupplier, screenHeightSupplier, screenStartPositionSupplier);
    }

    @Override
    public void init() {
        super.init();

        this.addDropDownList(this.allCriteriaTriggers.keySet(), "criteriaTriggers", this.screenTopLeftCorner[0] + 10, this.screenTopLeftCorner[1] + 45, 80, () -> "", Color.green::getRGB, (editBox1 -> {
            try {
                ResourceLocation temp = new ResourceLocation(editBox1.getValue());
                var trigger = this.allCriteriaTriggers.get(temp);
                String key = temp.toString().substring(temp.toString().indexOf(":") + 1);

                if(trigger != null){
                    this.criteria.put(key, trigger);
                    this.identifyConditionInputs(temp);
                    this.init();
                }
                else {
                    throw new IllegalArgumentException();
                }
            }
            catch (ResourceLocationException | IllegalArgumentException e){
                LOGGER.error("Can't find Criterion for input: {}", editBox1.getValue());
            }
        }));
        //this.addRenderableWidget(new MultiLineEditBox(this.font, this.screenTopLeftCorner[0] + 5, this.screenTopLeftCorner[1] + 5, 100, 100, Component.literal("LOL"), Component.literal("Test")));
    }

    @Override
    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        super.render(poseStack, mouseX, mouseY, partialTick);
        this.minecraft.font.drawShadow(poseStack, this.display != null ? this.display.getTitle() : DEFAULT_TITLE, screenTopLeftCorner[0] + FRAME_LENGTH + PADDING, screenTopLeftCorner[1] + 9, -1);

        //RequirementsStrategy.AND;
        //RequirementsStrategy.OR;
    }

    private void identifyConditionInputs(ResourceLocation trigger){
        CriteriaConditionsIdentifier.CriteriaConditions criteriaConditions = CriteriaConditionsIdentifier.getConditionsFor(trigger);
        var conditions = criteriaConditions.getConditions();
        List<Class<?>> mainConditions = conditions.get(criteriaConditions.getTriggerInstanceClass());
        this.addInputFields(mainConditions, conditions);
    }

    private void addInputFields(List<Class<?>> conditionsList, Map<Class<?>, List<Class<?>>> conditions){
        if(conditionsList == null){
            return;
        }
        conditionsList.forEach(aClass -> {
            String className = aClass.getName();
            if(className.equals("java.lang.String")){
                //TODO: ADD String input field
                LOGGER.debug("Found String");
            }
            else if (aClass.getGenericSuperclass() != null && aClass.getGenericSuperclass().getTypeName().equals("java.lang.Number")) {
                //TODO: Add Number input field
                LOGGER.debug("Found number");
            }
            else if (className.equals("java.lang.Boolean")) {
                //TODO: ADD Boolean input field
                LOGGER.debug("Found boolean");
            }
            else {
                LOGGER.debug("Class: " + aClass.getName());
                addInputFields(conditions.get(aClass), conditions);
            }
        });
    }
}
