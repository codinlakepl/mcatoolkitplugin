package org.mcadminToolkit.playerslist;

import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class playerslist {

    public static playerInfo[] getPlayers (JavaPlugin plugin) {

        Server server = plugin.getServer();

        List<playerInfo> players = new ArrayList<>();

        Player[] onlinePlayers = server.getOnlinePlayers().toArray(new Player[0]);

        for (Player onlinePlayer : onlinePlayers) {
            players.add(new playerInfo(onlinePlayer.getName(), onlinePlayer.getUniqueId()));
        }

        return players.toArray(new playerInfo[0]);
    }
}
