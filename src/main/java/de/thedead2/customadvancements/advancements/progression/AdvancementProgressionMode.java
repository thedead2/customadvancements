package de.thedead2.customadvancements.advancements.progression;

import de.thedead2.customadvancements.util.core.ConfigManager;
import de.thedead2.customadvancements.util.core.ModHelper;
import de.thedead2.customadvancements.util.core.TranslationKeyProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;

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
        AdvancementProgress progress = playerAdvancements.getOrStartProgress(parentAdvancement);
        if(!progress.isDone()){
            return Optional.of(false);
        }
        return Optional.empty();
    }

    protected abstract Optional<Boolean> handleAdvancementAchieving(Advancement advancement, Advancement parentAdvancement, PlayerAdvancements playerAdvancements);

    public Advancement handleConnectedAdvancements(Advancement advancement){
        ResourceLocation resourceLocation = advancement.getId();

        var connectedAdvancements = ConfigManager.getConnectedAdvancements();

        ResourceLocation parent = connectedAdvancements.get(resourceLocation);

        return parent == null ? null : ModHelper.getServer().orElseThrow(NullPointerException::new).getAdvancements().getAdvancement(parent);
    }

    public static void resetAdvancementProgress(ServerPlayer player){
        player.sendMessage(TranslationKeyProvider.chatMessage("advancements_reset", ChatFormatting.RED, player.getDisplayName()), Util.NIL_UUID);
        for(Advancement advancement : player.getServer().getAdvancements().getAllAdvancements()){
            AdvancementProgress advancementProgress = player.getAdvancements().getOrStartProgress(advancement);
            for(String s : advancementProgress.getCompletedCriteria()) {
                player.getAdvancements().revoke(advancement, s);
            }
        }
    }
}
