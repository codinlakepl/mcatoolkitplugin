package org.mcadminToolkit.playerslist;

import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class offlineplayerslist {

    public static playerInfo[] getOfflinePlayers (JavaPlugin plugin) {

        List<playerInfo> offlinePlayers = new ArrayList<>();

        Server server = plugin.getServer();

        OfflinePlayer[] allPlayers = server.getOfflinePlayers();

        playerInfo[] onlinePlayers = playerslist.getPlayers(plugin);

        for (OfflinePlayer player : allPlayers) {
            boolean exists = false;
            for (playerInfo onlinePlayer : onlinePlayers) {
                if (onlinePlayer.name == player.getName()) {
                    exists = true;
                    break;
                }
            }

            if (!exists) offlinePlayers.add (new playerInfo(player.getName(), player.getUniqueId()));
        }

        return offlinePlayers.toArray(new playerInfo[0]);
    }
}
