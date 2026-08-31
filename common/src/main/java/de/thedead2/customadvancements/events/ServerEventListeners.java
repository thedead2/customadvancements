package de.thedead2.customadvancements.events;

import de.thedead2.customadvancements.CAMain;
import de.thedead2.customadvancements.advancements.AdvancementProgressionMode;
import de.thedead2.customadvancements.util.ConfigManager;
import net.minecraft.network.protocol.game.ClientboundUpdateAdvancementsPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.Map;
import java.util.Set;


public class ServerEventListeners {

    public static void onServerStart() {
        CAMain.getInstance().loadData();
    }


    public static void onPlayerDeath(ServerPlayer player) {
        if (ConfigManager.RESET_ADVANCEMENTS_ON_DEATH.get()) {
            AdvancementProgressionMode.resetAdvancementProgress(player);
        }
    }


    public static void onServerStop() {
        CAMain.getInstance().clearLoadingStates();
    }

    // ClientAdvancements don't get reset completely on a reload, so we have to do it manually when the advancement list is empty
    public static void onDatapackReload(MinecraftServer server) {
        var serverAdvancementManager = server.getAdvancements();

        if (serverAdvancementManager.getAllAdvancements().isEmpty()) {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                player.connection.send(new ClientboundUpdateAdvancementsPacket(true, List.of(), Set.of(), Map.of()));
            }
        }
    }
}
