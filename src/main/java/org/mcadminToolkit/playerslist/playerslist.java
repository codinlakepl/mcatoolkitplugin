package org.mcadminToolkit.playerslist;

import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class playerslist {

    public static String[] getPlayers (JavaPlugin plugin) {

        Server server = plugin.getServer();

        List<String> players = new ArrayList<>();

        Player[] onlinePlayers = server.getOnlinePlayers().toArray(new Player[0]);

        for (Player onlinePlayer : onlinePlayers) {
            players.add(onlinePlayer.getName());
        }

        return players.toArray(new String[0]);
    }
}
