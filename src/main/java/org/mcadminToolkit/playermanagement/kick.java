package org.mcadminToolkit.playermanagement;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public class kick {

    public static void kick (JavaPlugin plugin, String playerName, String reason) {
        Server server = plugin.getServer();

        Player[] players = server.getOnlinePlayers().toArray(new Player[0]);

        for (Player player : players) {
            if (player.getName().equals(playerName)) {
                Bukkit.getScheduler().runTask(plugin, new Runnable() {
                    @Override
                    public void run() {
                        player.kickPlayer(reason);
                    }
                });
                break;
            }
        }
    }
}
