package org.mcadminToolkit.playermanagement;

import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public class kick {

    public static void kick (JavaPlugin plugin, UUID playerUUID, String reason) {
        Server server = plugin.getServer();

        Player[] players = server.getOnlinePlayers().toArray(new Player[0]);

        for (Player player : players) {
            if (player.getUniqueId().equals(playerUUID)) {
                player.kickPlayer(reason);
                break;
            }
        }
    }
}
