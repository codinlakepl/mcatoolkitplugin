package org.mcadminToolkit.whitelist;

import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Array;
import java.util.HashSet;
import java.util.Set;

public class whitelist {
    public static String[] getWhiteList(JavaPlugin plugin){
        Server server = plugin.getServer();
        Set<String> whitelistTMP = new HashSet<String>();

        Set<OfflinePlayer> whitelistPlayers = server.getWhitelistedPlayers();

        for (OfflinePlayer player: whitelistPlayers){
            String playerName = player.getName();
            whitelistTMP.add(playerName);
        }
        return  whitelistTMP.toArray(new String[0]);
    }

    public static String enableWhitelist (JavaPlugin plugin) {
        Server server = plugin.getServer();

        try {
            server.dispatchCommand(server.getConsoleSender(), "whitelist on");
            server.reloadWhitelist();
        } catch (Exception err) {
            return err.toString();
        }

        return "Success";
    }

    public static String disableWhitelist (JavaPlugin plugin) {
        Server server = plugin.getServer();

        try {
            server.dispatchCommand(server.getConsoleSender(), "whitelist off");
            server.reloadWhitelist();
        } catch (Exception err) {
            return err.toString();
        }

        return "Success";
    }

    public static String addWhitelistPlayer(JavaPlugin plugin, String userName){
        Server server = plugin.getServer();

        try{
            server.dispatchCommand(server.getConsoleSender(), "whitelist add " + userName);
            server.reloadWhitelist();
        }catch (Exception err){
            return err.toString();
        }

        return "Success";
    }

    public static String removeWhitelistPlayer (JavaPlugin plugin, String userName) {
        Server server = plugin.getServer();

        try{
            server.dispatchCommand(server.getConsoleSender(), "whitelist remove " + userName);
            server.reloadWhitelist();
        }catch (Exception err){
            return err.toString();
        }

        return "Success";
    }
}
