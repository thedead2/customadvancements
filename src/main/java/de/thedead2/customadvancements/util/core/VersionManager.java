package de.thedead2.customadvancements.util.core;

import de.thedead2.customadvancements.util.localisation.TranslationKeyProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.VersionChecker;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;

import static de.thedead2.customadvancements.util.core.ModHelper.*;


//TODO: Check if still working
public class VersionManager {

    private static final VersionChecker.CheckResult RESULT = VersionChecker.getResult(getModContainerFor(MOD_ID).getModInfo());


    public static void register(IEventBus modEventBus, IEventBus forgeEventBus) {
        modEventBus.addListener(VersionManager::onLoadComplete);
        forgeEventBus.addListener(VersionManager::onPlayerLogin);
    }


    public static void onLoadComplete(final FMLLoadCompleteEvent event) {
        sendLoggerMessage();
    }


    public static void onPlayerLogin(final PlayerEvent.PlayerLoggedInEvent event) {
        if (ConfigManager.OUT_DATED_MESSAGE.get() && !isDevEnv()) {
            sendChatMessage(event.getEntity());
        }
    }


    public static void sendLoggerMessage() {
        VersionChecker.Status status = RESULT.status();

        switch (status) {
            case OUTDATED -> {
                ModHelper.LOGGER.warn("Mod is outdated! Current Version: {} Latest Version: {}", ModHelper.MOD_VERSION, RESULT.target());
                ModHelper.LOGGER.warn("Please update {} using this link: {}", ModHelper.MOD_NAME, ModHelper.MOD_UPDATE_LINK);
            }
            case BETA, BETA_OUTDATED -> {
                ModHelper.LOGGER.warn("You're currently using a Beta of {}! Please note that using this beta is at your own risk!", ModHelper.MOD_NAME);

                if (status == VersionChecker.Status.BETA_OUTDATED) {
                    ModHelper.LOGGER.warn("This Beta is outdated! Please update {} using this link: {}", ModHelper.MOD_NAME, ModHelper.MOD_UPDATE_LINK);
                }
            }
            case FAILED -> ModHelper.LOGGER.error("Failed to check for updates! Please check your internet connection!");
        }
    }


    public static void sendChatMessage(Player player) {
        VersionChecker.Status status = RESULT.status();

        switch (status) {
            case OUTDATED -> {
                player.sendSystemMessage(TranslationKeyProvider.chatMessage("mod_outdated_message", ChatFormatting.RED));
                player.sendSystemMessage(TranslationKeyProvider.chatLink(ModHelper.MOD_UPDATE_LINK, ChatFormatting.RED));
            }
            case BETA, BETA_OUTDATED -> {
                player.sendSystemMessage(TranslationKeyProvider.chatMessage("beta_warn_message", ChatFormatting.YELLOW));

                if (status == VersionChecker.Status.BETA_OUTDATED) {
                    player.sendSystemMessage(TranslationKeyProvider.chatMessage("beta_outdated_message", ChatFormatting.RED));
                    player.sendSystemMessage(TranslationKeyProvider.chatLink(ModHelper.MOD_UPDATE_LINK, ChatFormatting.RED));
                }
            }
        }
    }
}
