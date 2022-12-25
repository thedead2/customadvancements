package de.thedead2.customadvancements.util;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Util;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.VersionChecker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static de.thedead2.customadvancements.util.ModHelper.*;

public class VersionControl {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final VersionChecker.CheckResult RESULT = VersionChecker.getResult(THIS_MOD_CONTAINER.getModInfo());

    public static void chatMessage(MinecraftServer server){
        if (RESULT.status == VersionChecker.Status.OUTDATED){
            server.sendMessage(new StringTextComponent("[" + MOD_NAME + "]: Mod is outdated! Please update " + MOD_NAME + " using this link:"), Util.DUMMY_UUID);
            server.sendMessage(new StringTextComponent(MOD_UPDATE_LINK), Util.DUMMY_UUID);
        }
        else if (RESULT.status == VersionChecker.Status.BETA) {
            server.sendMessage(new StringTextComponent("[" + MOD_NAME + "]: You're currently using a Beta Version of the mod! Please note that using this beta is at your own risk!"),Util.DUMMY_UUID);
        }
        else if (RESULT.status == VersionChecker.Status.BETA_OUTDATED) {
            server.sendMessage(new StringTextComponent("[" + MOD_NAME + "]: You're currently using a Beta Version of the mod! Please note that using this beta is at your own risk!"), Util.DUMMY_UUID);
            server.sendMessage(new StringTextComponent("[" + MOD_NAME + "]: This Beta Version is outdated! Please update " + MOD_NAME + " using this link:"), Util.DUMMY_UUID);
            server.sendMessage(new StringTextComponent(MOD_UPDATE_LINK), Util.DUMMY_UUID);
        }
    }

    public void loggerMessage(){
        if(RESULT.status == VersionChecker.Status.UP_TO_DATE){
            LOGGER.debug("Retrieved Info that mod is up to date! Current Version: " + MOD_VERSION + " Latest Version: " + RESULT.target);
        }
        else if (RESULT.status == VersionChecker.Status.OUTDATED) {
            LOGGER.warn("Mod is outdated! Current Version: " + MOD_VERSION + " Latest Version: " + RESULT.target);
            LOGGER.warn("Please update " + MOD_NAME + " using this link: " + MOD_UPDATE_LINK);
        }
        else if (RESULT.status == VersionChecker.Status.FAILED) {
            LOGGER.error("Failed to check for updates! Please check your internet connection!");
        }
        else if (RESULT.status == VersionChecker.Status.BETA) {
            LOGGER.warn("You're currently using a Beta of " + MOD_NAME + "! Please note that using this beta is at your own risk!");
            LOGGER.info("Beta Status: " + RESULT.status);
        }
        else if (RESULT.status == VersionChecker.Status.BETA_OUTDATED) {
            LOGGER.warn("You're currently using a Beta of " + MOD_NAME + "! Please note that using this beta is at your own risk!");
            LOGGER.warn("This Beta is outdated! Please update " + MOD_NAME + " using this link: " + MOD_UPDATE_LINK);
            LOGGER.warn("Beta Status: " + RESULT.status);
        }
    }
}
