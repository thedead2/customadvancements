package de.thedead2.customadvancements.generator;

import de.thedead2.customadvancements.util.handler.AdvancementHandler;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;

import java.io.IOException;
import java.util.Map;

import static de.thedead2.customadvancements.util.ModHelper.LOGGER;

public class ServerAdvancementGenerator implements IAdvancementGenerator {

    private final Advancement.Builder builder;
    protected ResourceLocation parentId;
    protected DisplayInfo display;
    protected AdvancementRewards rewards;
    protected Map<String, Criterion> criteria;
    protected String[][] requirements;

    protected ResourceLocation advancementId;
    private final MinecraftServer server;


    public ServerAdvancementGenerator(DedicatedServer server){
        this.builder = Advancement.Builder.advancement(); //Empty Advancement.Builder
        this.server = server;
    }

    public void createNewAdvancement() {

    }

    @Override
    public void createDisplayInfo() {

    }

    @Override
    public void createRewards() {

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
}
