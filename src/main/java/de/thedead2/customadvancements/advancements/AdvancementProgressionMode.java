package de.thedead2.customadvancements.advancements;

import com.google.common.collect.ImmutableMap;
import de.thedead2.customadvancements.util.core.ConfigManager;
import de.thedead2.customadvancements.util.core.ModHelper;
import de.thedead2.customadvancements.util.localisation.TranslationKeyProvider;
import net.minecraft.ChatFormatting;
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

            if (isWhitelisted(id) || !isBlacklisted(id)) {
                return this.defaultAction(parentAdvancement, playerAdvancements);
            }

            return Optional.empty();
        }


        private boolean isWhitelisted(String id) {
            return ConfigManager.ADVANCEMENT_PROGRESSION_MODE_MOD_BLACKLIST.get().contains(id) && ConfigManager.ADVANCEMENT_PROGRESSION_MODE_MOD_BLACKLIST_IS_WHITELIST.get();
        }


        private boolean isBlacklisted(String id) {
            return ConfigManager.ADVANCEMENT_PROGRESSION_MODE_MOD_BLACKLIST.get().contains(id) && !ConfigManager.ADVANCEMENT_PROGRESSION_MODE_MOD_BLACKLIST_IS_WHITELIST.get();
        }
    },

    MINECRAFT {
        @Override
        protected Optional<Boolean> handleAdvancementAchieving(Advancement advancement, Advancement parentAdvancement, PlayerAdvancements playerAdvancements) {
            if (advancement.getId().getNamespace().equals("minecraft")) {
                return this.defaultAction(parentAdvancement, playerAdvancements);
            }

            return Optional.empty();
        }
    },

    CUSTOM_ADVANCEMENTS {
        @Override
        protected Optional<Boolean> handleAdvancementAchieving(Advancement advancement, Advancement parentAdvancement, PlayerAdvancements playerAdvancements) {
            if (advancement.getId().getNamespace().equals(MOD_ID)) {
                return this.defaultAction(parentAdvancement, playerAdvancements);
            }

            return Optional.empty();
        }
    };


    public static void resetAdvancementProgress(ServerPlayer player) {
        player.sendSystemMessage(TranslationKeyProvider.chatMessage("advancements_reset", ChatFormatting.RED, player.getDisplayName()));

        for (Advancement advancement : player.getServer().getAdvancements().getAllAdvancements()) {
            PlayerAdvancements playerAdvancements = player.getAdvancements();
            AdvancementProgress advancementProgress = playerAdvancements.getOrStartProgress(advancement);

            for (String s : advancementProgress.getCompletedCriteria()) {
                playerAdvancements.revoke(advancement, s);
            }
        }
    }


    public Optional<Boolean> handleAdvancementAchieving(Advancement advancement, PlayerAdvancements playerAdvancements) {
        Advancement parentAdvancement = advancement.getParent() != null ? advancement.getParent() : handleConnectedAdvancements(advancement);

        if (parentAdvancement != null && !advancement.getId().getPath().contains("recipes/")) {
            return this.handleAdvancementAchieving(advancement, parentAdvancement, playerAdvancements);
        }

        return Optional.empty();
    }


    public Advancement handleConnectedAdvancements(Advancement advancement) {
        ResourceLocation advancementId = advancement.getId();
        ImmutableMap<ResourceLocation, ResourceLocation> connectedAdvancements = ConfigManager.getConnectedAdvancements();
        ResourceLocation parent = connectedAdvancements.get(advancementId);

        return parent == null ? null : ModHelper.getServer().orElseThrow(NullPointerException::new).getAdvancements().getAdvancement(parent);
    }


    protected abstract Optional<Boolean> handleAdvancementAchieving(Advancement advancement, Advancement parentAdvancement, PlayerAdvancements playerAdvancements);


    protected Optional<Boolean> defaultAction(Advancement parentAdvancement, PlayerAdvancements playerAdvancements) {
        AdvancementProgress progress = playerAdvancements.getOrStartProgress(parentAdvancement);

        if (!progress.isDone()) {
            return Optional.of(false);
        }

        return Optional.empty();
    }
}
