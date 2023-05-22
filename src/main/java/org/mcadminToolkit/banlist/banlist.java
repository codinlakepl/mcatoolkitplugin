package org.mcadminToolkit.banlist;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class banlist {
    public static String[] playerBanList(JavaPlugin plugin){
        Server server = plugin.getServer();

        Set<OfflinePlayer> bannedPlayersSet = server.getBannedPlayers();
        List<String> bannedPlayersStrings = new ArrayList<>();

        for (OfflinePlayer bannedPlayer : bannedPlayersSet){
            bannedPlayersStrings.add(bannedPlayer.getName());
        }

        return bannedPlayersStrings.toArray(new String[0]);
    }

    public static String[] ipBanList(JavaPlugin plugin){
        Server server = plugin.getServer();

        Set<String> bannedIpSet = server.getIPBans();

        return bannedIpSet.toArray(new String[0]);
    }
}
