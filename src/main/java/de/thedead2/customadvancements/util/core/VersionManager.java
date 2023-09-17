package de.thedead2.customadvancements.util.core;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Util;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.VersionChecker;

/**
 * Inner Class VersionManager
 * handles every Update related action
 **/
public abstract class VersionManager {

    private static final VersionChecker.CheckResult RESULT = VersionChecker.getResult(ModHelper.THIS_MOD_CONTAINER.getModInfo());


    public static void sendChatMessage(PlayerEntity player) {
        if (RESULT.status.equals(VersionChecker.Status.OUTDATED)) {
            player.sendMessage(TranslationKeyProvider.chatMessage("mod_outdated_message", TextFormatting.RED), Util.DUMMY_UUID);
            player.sendMessage(TranslationKeyProvider.chatLink(ModHelper.MOD_UPDATE_LINK, TextFormatting.RED), Util.DUMMY_UUID);
        } else if (RESULT.status.equals(VersionChecker.Status.BETA)) {
            player.sendMessage(TranslationKeyProvider.chatMessage("beta_warn_message", TextFormatting.YELLOW), Util.DUMMY_UUID);
        } else if (RESULT.status.equals(VersionChecker.Status.BETA_OUTDATED)) {
            player.sendMessage(TranslationKeyProvider.chatMessage("beta_warn_message", TextFormatting.YELLOW), Util.DUMMY_UUID);
            player.sendMessage(TranslationKeyProvider.chatMessage("beta_outdated_message", TextFormatting.RED), Util.DUMMY_UUID);
            player.sendMessage(TranslationKeyProvider.chatLink(ModHelper.MOD_UPDATE_LINK, TextFormatting.RED), Util.DUMMY_UUID);}
    }

    public static void sendLoggerMessage() {
        if (RESULT.status.equals(VersionChecker.Status.OUTDATED)) {
            ModHelper.LOGGER.warn("Mod is outdated! Current Version: " + ModHelper.MOD_VERSION + " Latest Version: " + RESULT.target);
            ModHelper.LOGGER.warn("Please update " + ModHelper.MOD_NAME + " using this link: " + ModHelper.MOD_UPDATE_LINK);
        } else if (RESULT.status.equals(VersionChecker.Status.FAILED)) {
            ModHelper.LOGGER.error("Failed to check for updates! Please check your internet connection!");
        } else if (RESULT.status.equals(VersionChecker.Status.BETA)) {
            ModHelper.LOGGER.warn("You're currently using a Beta of " + ModHelper.MOD_NAME + "! Please note that using this beta is at your own risk!");
            ModHelper.LOGGER.info("Beta Status: " + RESULT.status);
        } else if (RESULT.status.equals(VersionChecker.Status.BETA_OUTDATED)) {
            ModHelper.LOGGER.warn("You're currently using a Beta of " + ModHelper.MOD_NAME + "! Please note that using this beta is at your own risk!");
            ModHelper.LOGGER.warn("This Beta is outdated! Please update " + ModHelper.MOD_NAME + " using this link: " + ModHelper.MOD_UPDATE_LINK);
            ModHelper.LOGGER.warn("Beta Status: " + RESULT.status);
        }
    }
}
