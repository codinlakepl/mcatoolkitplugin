package org.mcadminToolkit.playermanagement;

import org.bukkit.BanList;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Date;
import java.util.UUID;

public class ban {
    public static void ban (JavaPlugin plugin, String playerName, String reason, Date expires) {
        Server server = plugin.getServer();

        server.getBanList(BanList.Type.NAME).addBan(playerName, reason, expires, "console");
    }

    public static void banIp (JavaPlugin plugin, UUID playerUUID) {
        Server server = plugin.getServer();

        Player[] players = server.getOnlinePlayers().toArray(new Player[0]);

        for (Player player : players) {
            if (player.getUniqueId().equals(playerUUID)) {
                server.banIP(player.getAddress().toString());
                break;
            }
        }
    }

    public static void unban (JavaPlugin plugin, String playerName) {
        Server server = plugin.getServer();

        server.getBanList(BanList.Type.NAME).pardon(playerName);
    }

    public static void unbanIp (JavaPlugin plugin, String playerIp) {
        Server server = plugin.getServer();

        server.unbanIP(playerIp);
    }
}
