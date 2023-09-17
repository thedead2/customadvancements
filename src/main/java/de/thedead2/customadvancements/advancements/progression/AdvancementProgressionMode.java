package de.thedead2.customadvancements.advancements.progression;

import de.thedead2.customadvancements.util.core.ConfigManager;
import de.thedead2.customadvancements.util.core.ModHelper;
import de.thedead2.customadvancements.util.core.TranslationKeyProvider;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.text.TextFormatting;

import java.util.Optional;

import static de.thedead2.customadvancements.util.core.ModHelper.MOD_ID;

public enum AdvancementProgressionMode {

    ALL {
        @Override
        protected Optional<Boolean> handleAdvancementAchieving(Advancement advancement, Advancement parentAdvancement, PlayerAdvancements playerAdvancements) {
            return this.defaultAction(parentAdvancement, playerAdvancements);
        }
    },
    MODS {
        @Override
        protected Optional<Boolean> handleAdvancementAchieving(Advancement advancement, Advancement parentAdvancement, PlayerAdvancements playerAdvancements) {
            String id = advancement.getId().getNamespace();
            if((ConfigManager.ADVANCEMENT_PROGRESSION_MODE_MOD_BLACKLIST.get().contains(id) && ConfigManager.ADVANCEMENT_PROGRESSION_MODE_MOD_BLACKLIST_IS_WHITELIST.get()) || (!ConfigManager.ADVANCEMENT_PROGRESSION_MODE_MOD_BLACKLIST.get().contains(id) && !ConfigManager.ADVANCEMENT_PROGRESSION_MODE_MOD_BLACKLIST_IS_WHITELIST.get())){
                return this.defaultAction(parentAdvancement, playerAdvancements);
            }
            return Optional.empty();
        }
    },
    MINECRAFT {
        @Override
        protected Optional<Boolean> handleAdvancementAchieving(Advancement advancement, Advancement parentAdvancement, PlayerAdvancements playerAdvancements) {
            if(advancement.getId().getNamespace().equals("minecraft")) return this.defaultAction(parentAdvancement, playerAdvancements);
            return Optional.empty();
        }
    },
    CUSTOM_ADVANCEMENTS {
        @Override
        protected Optional<Boolean> handleAdvancementAchieving(Advancement advancement, Advancement parentAdvancement, PlayerAdvancements playerAdvancements) {
            if(advancement.getId().getNamespace().equals(MOD_ID)) return this.defaultAction(parentAdvancement, playerAdvancements);
            return Optional.empty();
        }
    };

    public Optional<Boolean> handleAdvancementAchieving(Advancement advancement, PlayerAdvancements playerAdvancements){
        Advancement parentAdvancement = advancement.getParent() != null ? advancement.getParent() : handleConnectedAdvancements(advancement);
        if(parentAdvancement != null && !advancement.getId().getPath().contains("recipes/")) return this.handleAdvancementAchieving(advancement, parentAdvancement, playerAdvancements);
        return Optional.empty();
    }

    protected Optional<Boolean> defaultAction(Advancement parentAdvancement, PlayerAdvancements playerAdvancements){
        AdvancementProgress progress = playerAdvancements.getProgress(parentAdvancement);
        if(!progress.isDone()){
            return Optional.of(false);
        }
        return Optional.empty();
    }

    protected abstract Optional<Boolean> handleAdvancementAchieving(Advancement advancement, Advancement parentAdvancement, PlayerAdvancements playerAdvancements);

    public Advancement handleConnectedAdvancements(Advancement advancement){
        ResourceLocation resourceLocation = advancement.getId();

        com.google.common.collect.ImmutableMap<ResourceLocation, ResourceLocation> connectedAdvancements = ConfigManager.getConnectedAdvancements();

        ResourceLocation parent = connectedAdvancements.get(resourceLocation);

        return parent == null ? null : ModHelper.getServer().orElseThrow(NullPointerException::new).getAdvancementManager().getAdvancement(parent);
    }

    public static void resetAdvancementProgress(ServerPlayerEntity player){
        player.sendMessage(TranslationKeyProvider.chatMessage("advancements_reset", TextFormatting.RED, player.getDisplayName()), Util.DUMMY_UUID);
        for(Advancement advancement : player.getServer().getAdvancementManager().getAllAdvancements()){
            AdvancementProgress advancementProgress = player.getAdvancements().getProgress(advancement);
            for(String s : advancementProgress.getCompletedCriteria()) {
                player.getAdvancements().revokeCriterion(advancement, s);
            }
        }
    }
}
