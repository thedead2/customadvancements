package de.thedead2.customadvancements.generator;

import com.google.gson.JsonElement;
import de.thedead2.customadvancements.generator.gui.AdvancementCreationScreen;
import de.thedead2.customadvancements.util.handler.FileHandler;
import net.minecraft.advancements.*;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static de.thedead2.customadvancements.util.ModHelper.DIR_PATH;
import static de.thedead2.customadvancements.util.ModHelper.LOGGER;

public class AdvancementGenerator {

    private final Advancement.Builder builder;
    protected ResourceLocation parentId;
    protected DisplayInfo display;
    protected AdvancementRewards rewards;
    protected Map<String, Criterion> criteria;
    protected String[][] requirements;

    protected ResourceLocation advancementId;
    private final DedicatedServer server;
    private final Collection<Advancement> advancements;


    public AdvancementGenerator(MinecraftServer server){
        this.builder = Advancement.Builder.advancement(); //Empty Advancement.Builder

        if(FMLEnvironment.dist.isClient()){
            Minecraft minecraft = Minecraft.getInstance();
            this.server = null;
            assert minecraft.player != null;
            this.advancements = minecraft.player.connection.getAdvancements().getAdvancements().getAllAdvancements();
            minecraft.setScreen(new AdvancementCreationScreen(null, minecraft, this));
        }
        else {
            this.server = (DedicatedServer) server;
            this.advancements = server.getAdvancements().getAllAdvancements();

            createNewAdvancement();
        }
    }

    public void createNewAdvancement(){
        this.resolveParent();
        this.createDisplayInfo();
        this.createRewards();
        this.createCriteria();
        this.createRequirements();
        this.getAdvancementId();

        build();
    }

    private void createDisplayInfo(){
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

    private void createRewards(){
        AdvancementRewards.Builder rewardsBuilder = new AdvancementRewards.Builder();

        /*rewardsBuilder.addExperience();
        rewardsBuilder.addLootTable();
        rewardsBuilder.addRecipe();
        rewardsBuilder.runs(); //Function*/

        this.rewards = rewardsBuilder.build();
    }

    private void resolveParent(){
        LOGGER.info("Please enter a resource location of the parent advancement or 'null' for a root advancement!");
    }

    private void createCriteria() {

    }

    private void createRequirements(){
    }

    private void getAdvancementId(){
    }

    protected Set<Advancement> getNoneParentAdvancements(){
        Set<Advancement> noneParentAdvancements = new HashSet<>();

        this.advancements.forEach(advancement -> {
            if(!advancement.getChildren().iterator().hasNext()){
                noneParentAdvancements.add(advancement);
            }
        });

        return noneParentAdvancements;
    }


    public void build(){
        try {
            this.builder.parent(this.parentId);
            this.builder.display(this.display);
            this.builder.rewards(this.rewards);
            this.criteria.forEach(builder::addCriterion);
            this.builder.requirements(this.requirements);

            this.builder.build(this.advancementId);

            Path path = Path.of(DIR_PATH + this.advancementId.toString().replace(":", "/") + ".json");

            JsonElement jsonElement = this.builder.serializeToJson();
            FileHandler.writeFile(new ByteArrayInputStream(jsonElement.toString().getBytes()), path);

            LOGGER.info("Created Advancement {} successfully at: {}", this.advancementId, path);
        }
        catch (IllegalStateException | NullPointerException e){
            LOGGER.error("Couldn't create Advancement {} with Advancement.Builder: {}",this.advancementId, this.builder);
            e.printStackTrace();
        }
    }
}
