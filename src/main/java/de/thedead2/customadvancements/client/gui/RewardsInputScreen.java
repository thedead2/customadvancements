package de.thedead2.customadvancements.client.gui;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import de.thedead2.customadvancements.client.gui.components.AddButton;
import de.thedead2.customadvancements.client.gui.components.FakeAdvancementWidget;
import de.thedead2.customadvancements.client.gui.generator.AdvancementGeneratorGUI;
import de.thedead2.customadvancements.client.gui.generator.ClientAdvancementGenerator;
import net.minecraft.ResourceLocationException;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static de.thedead2.customadvancements.util.ModHelper.LOGGER;

public class RewardsInputScreen extends BasicInputScreen {

    private final ImmutableList<ResourceLocation> allRecipes = ImmutableList.copyOf(this.minecraft.getSingleplayerServer().getRecipeManager().getRecipeIds().toList());
    private final ImmutableList<ResourceLocation> allLootTables = ImmutableList.copyOf(this.minecraft.getSingleplayerServer().getLootTables().getIds());
    private final ImmutableList<ResourceLocation> allFunctions = ImmutableList.copyOf(this.minecraft.getSingleplayerServer().getFunctions().getFunctionNames());
    private final AdvancementRewards originalRewards;

    private int experience;
    private final List<ResourceLocation> loot = Lists.newArrayList();
    private final List<ResourceLocation> recipes = Lists.newArrayList();
    @Nullable
    private ResourceLocation function;


    public RewardsInputScreen(ClientAdvancementGenerator parent, AdvancementGeneratorGUI gui, DisplayInfo display, @NotNull FakeAdvancementWidget widget, Supplier<Integer> screenWidthSupplier, Supplier<Integer> screenHeightSupplier, Supplier<int[]> screenStartPositionSupplier, AdvancementRewards rewardsIn) {
        super(parent, gui, display, widget, screenWidthSupplier, screenHeightSupplier, screenStartPositionSupplier);
        this.originalRewards = rewardsIn;
        this.experience = rewardsIn.experience;
        this.loot.addAll(Arrays.stream(rewardsIn.loot).toList());
        this.recipes.addAll(Arrays.stream(rewardsIn.getRecipes()).toList());
        this.function = rewardsIn.function.getId();
    }

    public RewardsInputScreen(ClientAdvancementGenerator parent, AdvancementGeneratorGUI gui, DisplayInfo display, @NotNull FakeAdvancementWidget widget, Supplier<Integer> screenWidthSupplier, Supplier<Integer> screenHeightSupplier, Supplier<int[]> screenStartPositionSupplier) {
        super(parent, gui, display, widget, screenWidthSupplier, screenHeightSupplier, screenStartPositionSupplier);
        this.originalRewards = null;
    }

    @Override
    public void init() {
        super.init();
        //LOGGER.debug(this.minecraft.getSingleplayerServer().getFunctions().getFunctionNames());

        this.addEditBox(screenTopLeftCorner[0] + PADDING + FRAME_LENGTH /2 + getFontWidth("Experience: "), screenTopLeftCorner[1] + 26 + PADDING, "experience", () -> String.valueOf(this.experience), Color.white::getRGB, editBox1 -> {
            try {
                this.experience = Integer.parseInt(editBox1.getValue());
            }
            catch (NumberFormatException e){
                LOGGER.error("Can't parse {} to Integer!", editBox1.getValue());
            }
        });

        int var = screenTopLeftCorner[1] + 26 + PADDING + this.minecraft.font.lineHeight + 10;
        AtomicInteger ref = new AtomicInteger(0);
        this.addDynamicDropDownLists(this.recipes, this.allRecipes, "recipes", screenTopLeftCorner[0] + PADDING + FRAME_LENGTH / 2, var + 7, var, ref);

        int temp = var + 13 * ref.get();

        int var2 = temp + 25;
        this.addDynamicDropDownLists(this.loot, this.allLootTables, "loot", screenTopLeftCorner[0] + PADDING + FRAME_LENGTH / 2, temp + 7, var2, new AtomicInteger(0));

        int i = Math.min(this.recipes.size(), 5);
        int startYPos = screenTopLeftCorner[1] + 26 + PADDING + 10 + this.minecraft.font.lineHeight + (this.minecraft.font.lineHeight + 3) * i + 5;

        int j = Math.min(this.loot.size(), 5);
        int nextYStartPos = startYPos + 15 + (this.minecraft.font.lineHeight + 3) * j + 5;

        this.addDropDownList(this.allFunctions, "function", screenTopLeftCorner[0] + PADDING + FRAME_LENGTH /2 + getFontWidth("Function: "), nextYStartPos + 15, 80, () -> this.function != null ? this.function.toString() : "None", Color.white::getRGB, (editBox1 -> {
            try {
                this.function = new ResourceLocation(editBox1.getValue());
            }
            catch (ResourceLocationException e){
                LOGGER.error("Unable to create ResourceLocation with input: " + editBox1.getValue());
            }
        }));
    }

    private void addDynamicDropDownLists(List<ResourceLocation> listIn, ImmutableList<ResourceLocation> listContent, String name, int x, int addButtonY, int yStartPos, AtomicInteger i){
        for (ResourceLocation resourceLocation : listIn) {
            int index = i.get();
            String s = name + index;
            this.addDropDownList(listContent, s, x + getFontWidth(name + ": "), yStartPos + 13 * i.getAndIncrement(), 80, resourceLocation::toString, Color.white::getRGB, editBox1 -> {
                try {
                    ResourceLocation temp = new ResourceLocation(editBox1.getValue());
                    if ((index < 0) || (index >= listIn.size())) {
                        listIn.add(temp);
                    } else {
                        listIn.remove(index);
                        listIn.add(index, temp);
                    }
                }
                catch (ResourceLocationException e) {
                    LOGGER.error("Unable to create ResourceLocation with input: " + editBox1.getValue());
                }
            });
            this.addRenderableWidget(new Button(screenTopRightCorner[0] - (EDIT_BUTTON_LENGTH - PADDING_RIGHT) * 2 , yStartPos + 13 * index, 20, 20, Component.empty(), pButton1 -> {
                listIn.remove(index);
            }));
        }
        this.addRenderableWidget(new AddButton(x + 5, addButtonY, 24, 24, Component.empty(), pButton -> {
            int index = i.get();
            String s = name + index;
            this.addDropDownList(listContent, s, x + getFontWidth(name + ": "), yStartPos + 13 * i.getAndIncrement(), 80, () -> "", Color.white::getRGB, editBox1 -> {
                try {
                    ResourceLocation temp = new ResourceLocation(editBox1.getValue());
                    if((index < 0) || (index >= listIn.size())){
                        listIn.add(temp);
                    }
                    else {
                        listIn.remove(index);
                        listIn.add(index, temp);
                    }

                    pButton.active = true;
                }
                catch (ResourceLocationException e){
                    LOGGER.error("Unable to create ResourceLocation with input: " + editBox1.getValue());
                }
            });
            this.addRenderableWidget(new Button(screenTopRightCorner[0] - (EDIT_BUTTON_LENGTH - PADDING_RIGHT) * 2 , yStartPos + 13 * index, 20, 20, Component.empty(), pButton1 -> {
                listIn.remove(index);
            }));
            pButton.active = false;
        }, this.widget));
    }

    @Override
    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        super.render(poseStack, mouseX, mouseY, partialTick);
        this.minecraft.font.drawShadow(poseStack, this.display != null ? this.display.getTitle() : DEFAULT_TITLE, screenTopLeftCorner[0] + FRAME_LENGTH + PADDING, screenTopLeftCorner[1] + 9, -1);

        this.minecraft.font.drawShadow(poseStack, Component.literal("Experience: "), screenTopLeftCorner[0] + PADDING + FRAME_LENGTH /2, screenTopLeftCorner[1] + 26 + PADDING, Color.WHITE.getRGB());
        /*if(!this.editBoxes.get("experience").editBox().isVisible()){
            this.minecraft.font.drawShadow(poseStack, Component.literal(String.valueOf(this.experience)), screenTopLeftCorner[0] + PADDING + FRAME_LENGTH /2 + getFontWidth("Experience: "), screenTopLeftCorner[1] + 26 + PADDING, Color.WHITE.getRGB());
        }*/

        this.hLine(poseStack, this.screenTopLeftCorner[0] + PADDING_RIGHT,this.screenTopRightCorner[0] - PADDING_RIGHT, this.screenTopLeftCorner[1] + 26 + PADDING + this.minecraft.font.lineHeight + 5, Color.DARK_GRAY.getRGB());

        this.minecraft.font.drawShadow(poseStack, Component.literal("Recipes: "), screenTopLeftCorner[0] + PADDING + FRAME_LENGTH /2, screenTopLeftCorner[1] + 26 + PADDING + this.minecraft.font.lineHeight + 10, Color.WHITE.getRGB());

        /*int i = 0;
        for(ResourceLocation resourceLocation : this.recipes){
            String s = "recipes" + i;
            if(!this.editBoxes.get(s).editBox().isVisible()) {
                this.minecraft.font.drawShadow(poseStack, Component.literal(resourceLocation.toString()), screenTopLeftCorner[0] + PADDING + FRAME_LENGTH / 2 + getFontWidth("Recipes: "), screenTopLeftCorner[1] + 26 + PADDING + 10 + this.minecraft.font.lineHeight + (this.minecraft.font.lineHeight + 3) * i, Color.WHITE.getRGB());
            }
            if(i > 3){
                if(this.recipes.size() - (i+1) > 0) {
                    this.minecraft.font.drawShadow(poseStack, Component.literal("And " + (this.recipes.size() - (i + 1)) + " more"), screenTopLeftCorner[0] + PADDING + FRAME_LENGTH / 2 + getFontWidth("Recipes: "), screenTopLeftCorner[1] + 26 + PADDING + 10 + this.minecraft.font.lineHeight + (this.minecraft.font.lineHeight + 3) * (i + 1), Color.WHITE.getRGB());
                }
                break;
            }
            i++;
        }*/

        if(this.recipes.isEmpty() && (this.editBoxes.get("recipes" + 0) != null && !this.editBoxes.get("recipes" + 0).editBox().isVisible())){
            this.minecraft.font.drawShadow(poseStack, Component.literal("None"), screenTopLeftCorner[0] + PADDING + FRAME_LENGTH /2 + getFontWidth("Recipes: "), screenTopLeftCorner[1] + 26 + PADDING + 10 + this.minecraft.font.lineHeight + (this.minecraft.font.lineHeight + 3) * i, Color.WHITE.getRGB());
        }

        int startYPos = screenTopLeftCorner[1] + 26 + PADDING + 10 + this.minecraft.font.lineHeight + (this.minecraft.font.lineHeight + 3) * i + 5;

        this.hLine(poseStack, this.screenTopLeftCorner[0] + PADDING_RIGHT,this.screenTopRightCorner[0] - PADDING_RIGHT, startYPos + 10, Color.DARK_GRAY.getRGB());

        this.minecraft.font.drawShadow(poseStack, Component.literal("Loot: "), screenTopLeftCorner[0] + PADDING + FRAME_LENGTH /2, startYPos + 15, Color.WHITE.getRGB());

        /*int j = 0;
        for(ResourceLocation resourceLocation : this.loot){
            this.minecraft.font.drawShadow(poseStack, Component.literal(resourceLocation.toString()), screenTopLeftCorner[0] + PADDING + FRAME_LENGTH /2 + getFontWidth("Loot: "), startYPos + 15 + (this.minecraft.font.lineHeight + 3) * j, Color.WHITE.getRGB());
            if(j >= 5){
                this.minecraft.font.drawShadow(poseStack, Component.literal("And " + (this.loot.size() - i) + " more"), screenTopLeftCorner[0] + PADDING + FRAME_LENGTH /2 + getFontWidth("Loot: "), startYPos + 15 + (this.minecraft.font.lineHeight + 3) * j, Color.WHITE.getRGB());
                break;
            }
            j++;
        }*/
        /*if(this.loot.isEmpty()){
            this.minecraft.font.drawShadow(poseStack, Component.literal("None"), screenTopLeftCorner[0] + PADDING + FRAME_LENGTH /2 + getFontWidth("Loot: "), startYPos + 15 + (this.minecraft.font.lineHeight + 3) * j, Color.WHITE.getRGB());
        }

        int nextYStartPos = startYPos + 15 + (this.minecraft.font.lineHeight + 3) * j + 5;*/

        /*this.hLine(poseStack, this.screenTopLeftCorner[0] + PADDING_RIGHT,this.screenTopRightCorner[0] - PADDING_RIGHT, nextYStartPos + 10, Color.DARK_GRAY.getRGB());

        this.minecraft.font.drawShadow(poseStack, Component.literal("Function: "), screenTopLeftCorner[0] + PADDING + FRAME_LENGTH /2, nextYStartPos + 15, Color.WHITE.getRGB());

        if(!this.editBoxes.get("function").editBox().isVisible()){
            this.minecraft.font.drawShadow(poseStack, Component.literal(this.function != null ? this.function.toString() : "None"), screenTopLeftCorner[0] + PADDING + FRAME_LENGTH /2 + getFontWidth("Function: "), nextYStartPos + 15, Color.WHITE.getRGB());
        }*/
    }

    private AdvancementRewards build(){
        AdvancementRewards.Builder rewardsBuilder = new AdvancementRewards.Builder();
        rewardsBuilder.addExperience(this.experience);
        this.recipes.forEach(rewardsBuilder::addRecipe);
        this.loot.forEach(rewardsBuilder::addLootTable);

        if(this.function != null){
            rewardsBuilder.runs(this.function);
        }
        return rewardsBuilder.build();
    }

    @Override
    public void save() {
        this.generator.addRewards(this.build());
        super.save();
    }

    @Override
    public void reset() {
        this.loot.clear();
        this.recipes.clear();
        if(this.originalRewards != null){
            this.experience = this.originalRewards.experience;
            this.loot.addAll(Arrays.stream(this.originalRewards.loot).toList());
            this.recipes.addAll(Arrays.stream(this.originalRewards.getRecipes()).toList());
            this.function = this.originalRewards.function.getId();
        }
        else {
            this.experience = 0;
            this.function = null;
        }
    }
}
