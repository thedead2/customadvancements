package de.thedead2.customadvancements.advancements;

import com.google.common.collect.ImmutableMap;
import de.thedead2.customadvancements.util.ConfigManager;
import de.thedead2.customadvancements.util.ModHelper;
import de.thedead2.customadvancements.util.localisation.ModTranslationKeys;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

import static de.thedead2.customadvancements.util.ModHelper.MOD_ID;


public enum AdvancementProgressionMode {

    ALL {
        @Override
        protected Optional<Boolean> handleAdvancementAchieving(AdvancementHolder advancement, AdvancementHolder parentAdvancement, PlayerAdvancements playerAdvancements) {
            return this.defaultAction(parentAdvancement, playerAdvancements);
        }
    },

    MODS {
        @Override
        protected Optional<Boolean> handleAdvancementAchieving(AdvancementHolder advancement, AdvancementHolder parentAdvancement, PlayerAdvancements playerAdvancements) {
            String id = advancement.id().getNamespace();

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
        protected Optional<Boolean> handleAdvancementAchieving(AdvancementHolder advancement, AdvancementHolder parentAdvancement, PlayerAdvancements playerAdvancements) {
            if (advancement.id().getNamespace().equals("minecraft")) {
                return this.defaultAction(parentAdvancement, playerAdvancements);
            }

            return Optional.empty();
        }
    },

    CUSTOM_ADVANCEMENTS {
        @Override
        protected Optional<Boolean> handleAdvancementAchieving(AdvancementHolder advancement, AdvancementHolder parentAdvancement, PlayerAdvancements playerAdvancements) {
            if (advancement.id().getNamespace().equals(MOD_ID)) {
                return this.defaultAction(parentAdvancement, playerAdvancements);
            }

            return Optional.empty();
        }
    };


    public static void resetAdvancementProgress(ServerPlayer player) {
        player.sendSystemMessage(ModTranslationKeys.chatMessage(ModTranslationKeys.ADVANCEMENTS_RESET_MESSAGE, ChatFormatting.RED, player.getDisplayName()));

        for (AdvancementHolder advancement : player.getServer().getAdvancements().getAllAdvancements()) {
            PlayerAdvancements playerAdvancements = player.getAdvancements();
            AdvancementProgress advancementProgress = playerAdvancements.getOrStartProgress(advancement);

            for (String s : advancementProgress.getCompletedCriteria()) {
                playerAdvancements.revoke(advancement, s);
            }
        }
    }


    public Optional<Boolean> handleAdvancementAchieving(AdvancementHolder advancement, PlayerAdvancements playerAdvancements) {
        ResourceLocation parentId = advancement.value().parent().orElse(null);
        AdvancementHolder parentAdvancement = parentId != null ? getAdvancement(parentId) : handleConnectedAdvancements(advancement);

        if (parentAdvancement != null && !advancement.id().getPath().contains("recipes/")) {
            return this.handleAdvancementAchieving(advancement, parentAdvancement, playerAdvancements);
        }

        return Optional.empty();
    }


    public AdvancementHolder handleConnectedAdvancements(AdvancementHolder advancement) {
        ResourceLocation advancementId = advancement.id();
        ImmutableMap<ResourceLocation, ResourceLocation> connectedAdvancements = ConfigManager.getConnectedAdvancements();
        ResourceLocation parent = connectedAdvancements.get(advancementId);

        return parent == null ? null : getAdvancement(parent);
    }

    private AdvancementHolder getAdvancement(ResourceLocation id) {
        return ModHelper.getServer().orElseThrow(NullPointerException::new).getAdvancements().get(id);
    }


    protected abstract Optional<Boolean> handleAdvancementAchieving(AdvancementHolder advancement, AdvancementHolder parentAdvancement, PlayerAdvancements playerAdvancements);


    protected Optional<Boolean> defaultAction(AdvancementHolder parentAdvancement, PlayerAdvancements playerAdvancements) {
        AdvancementProgress progress = playerAdvancements.getOrStartProgress(parentAdvancement);

        if (!progress.isDone()) {
            return Optional.of(false);
        }

        return Optional.empty();
    }
}
