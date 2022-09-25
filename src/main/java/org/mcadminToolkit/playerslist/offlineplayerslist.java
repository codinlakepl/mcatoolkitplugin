package org.mcadminToolkit.playerslist;

import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class offlineplayerslist {

    public static String[] getOfflinePlayers (JavaPlugin plugin) {

        List<String> offlinePlayers = new ArrayList<>();

        Server server = plugin.getServer();

        OfflinePlayer[] allPlayers = server.getOfflinePlayers();

        String[] onlinePlayers = playerslist.getPlayers(plugin);

        for (OfflinePlayer player : allPlayers) {
            boolean exists = false;
            for (String onlinePlayer : onlinePlayers) {
                if (onlinePlayer == player.getName()) {
                    exists = true;
                    break;
                }
            }

            if (!exists) offlinePlayers.add (player.getName());
        }

        return offlinePlayers.toArray(new String[0]);
    }
}
