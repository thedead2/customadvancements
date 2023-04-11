package de.thedead2.customadvancements.datagen.dataprovider;

import de.thedead2.customadvancements.util.language.TranslationKeyProvider;
import de.thedead2.customadvancements.util.language.TranslationKeyType;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.FrameType;
import net.minecraft.advancements.critereon.*;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.advancements.AdvancementProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.ForgeAdvancementProvider;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static de.thedead2.customadvancements.util.ModHelper.MOD_ID;

public class ModAdvancementProvider extends ForgeAdvancementProvider {

    public ModAdvancementProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries, ExistingFileHelper existingFileHelper) {
        super(output, registries, existingFileHelper, List.of(new ModAdvancementSubProvider()));
    }

    public static class ModAdvancementSubProvider implements ForgeAdvancementProvider.AdvancementGenerator {

        @Override
        public void generate(HolderLookup.@NotNull Provider registries, @NotNull Consumer<Advancement> saver, @NotNull ExistingFileHelper existingFileHelper) {
            Advancement advancement = withDisplay(Items.ROTTEN_FLESH, "back_to_the_roots", Advancement.Builder.advancement()).parent(new ResourceLocation("story/root"))
                    .rewards(new AdvancementRewards.Builder().addExperience(50))
                    .addCriterion("back_to_the_roots", KilledTrigger.TriggerInstance.playerKilledEntity(EntityPredicate.Builder.entity().of(EntityType.ZOMBIE).flags(EntityFlagsPredicate.Builder.flags().setIsBaby(false).build()).build(), DamageSourcePredicate.Builder.damageType().direct(EntityPredicate.Builder.entity().equipment(EntityEquipmentPredicate.Builder.equipment().mainhand(ItemPredicate.Builder.item().of(Items.ROTTEN_FLESH).build()).build()))))
                    .save(saver, new ResourceLocation(MOD_ID, "back_to_the_roots"), existingFileHelper);

        }

        private Advancement newAdvancement(String name, ResourceLocation parent, ItemLike item, AdvancementRewards rewards, Map<String, CriterionTriggerInstance> criteria, Consumer<Advancement> saver, ExistingFileHelper fileHelper){
            Advancement.Builder builder = Advancement.Builder.advancement()
                    .parent(parent)
                    .display(new ItemStack(item), Component.translatable(TranslationKeyProvider.advancementTranslationKeyFor(name, TranslationKeyType.AdvancementKeySubType.TITLE)), Component.translatable(TranslationKeyProvider.advancementTranslationKeyFor(name, TranslationKeyType.AdvancementKeySubType.DESCRIPTION)), null, FrameType.TASK, true, true, false)
                    .rewards(rewards);
            criteria.forEach(builder::addCriterion);
            return builder.save(saver, new ResourceLocation(MOD_ID, name), fileHelper);
        }

        private Advancement.Builder withDisplay(ItemLike item, String name, Advancement.Builder builder) {
            return builder.display(new ItemStack(item), Component.translatable(TranslationKeyProvider.advancementTranslationKeyFor(name, TranslationKeyType.AdvancementKeySubType.TITLE)), Component.translatable(TranslationKeyProvider.advancementTranslationKeyFor(name, TranslationKeyType.AdvancementKeySubType.DESCRIPTION)), null, FrameType.TASK, true, true, false);
        }
    }


}
