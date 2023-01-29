package de.thedead2.customadvancements.generator;

import com.mojang.blaze3d.vertex.PoseStack;
import de.thedead2.customadvancements.client.gui.BasicInputScreen;
import de.thedead2.customadvancements.util.handler.AdvancementHandler;
import net.minecraft.advancements.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

import static de.thedead2.customadvancements.util.ModHelper.LOGGER;

public class ClientAdvancementGenerator extends BasicInputScreen implements IAdvancementGenerator {

    private final Advancement.Builder builder;
    protected ResourceLocation parentId;
    protected DisplayInfo display;
    protected AdvancementRewards rewards;
    protected Map<String, Criterion> criteria;
    protected String[][] requirements;

    protected ResourceLocation advancementId;
    private final Collection<Advancement> advancements;

    public ClientAdvancementGenerator(Screen parent, Minecraft minecraft, Advancement editAdvancement) {
        this(parent, minecraft, editAdvancement.getParent() != null ? editAdvancement.getParent().getId() : null);
        this.display = editAdvancement.getDisplay();
        this.requirements = editAdvancement.getRequirements();
        this.criteria = editAdvancement.getCriteria();
        this.rewards = editAdvancement.getRewards();
        this.advancementId = editAdvancement.getId();
    }

    public ClientAdvancementGenerator(Screen parent, Minecraft minecraft, ResourceLocation parentId) {
        super(parent, minecraft, null);
        super.setGenerator(this);
        this.builder = Advancement.Builder.advancement();
        this.parentId = parentId;
        this.advancements = this.minecraft.getSingleplayerServer().getAdvancements().getAllAdvancements();
    }

    @Override
    public void createDisplayInfo() {
        ItemStack stack;
        Component title;
        Component description;
        ResourceLocation background;
        FrameType frame;
        boolean showToast;
        boolean announceToChat;
        boolean hidden;

        //Input

        //this.display = new DisplayInfo(stack, title, description, background, frame, showToast, announceToChat, hidden);
    }

    @Override
    public void createRewards() {
        AdvancementRewards.Builder rewardsBuilder = new AdvancementRewards.Builder();

        /*rewardsBuilder.addExperience();
        rewardsBuilder.addLootTable();
        rewardsBuilder.addRecipe();
        rewardsBuilder.runs(); //Function*/

        this.rewards = rewardsBuilder.build();
    }

    @Override
    public void resolveParent() {

    }

    @Override
    public void createCriteria() {

    }

    @Override
    public void createRequirements() {

    }

    @Override
    public void resolveId() {

    }

    @Override
    public void build() {
        try {
            this.builder.parent(this.parentId);
            this.builder.display(this.display);
            this.builder.rewards(this.rewards);
            this.criteria.forEach(builder::addCriterion);
            this.builder.requirements(this.requirements);

            Advancement finishedAdvancement = this.builder.build(this.advancementId);

            AdvancementHandler.writeAdvancementToFile(finishedAdvancement);

            LOGGER.info("Created Advancement {} successfully!", finishedAdvancement.getId());
        }
        catch (IllegalStateException | NullPointerException | IOException e){
            LOGGER.error("Couldn't create Advancement {} with Advancement.Builder: {}", this.advancementId, this.builder);
            e.printStackTrace();
        }
    }

    @Override
    public void init() {
        super.init();
    }

    @Override
    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        super.render(poseStack, mouseX, mouseY, partialTick);
    }


}
