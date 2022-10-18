package org.mcadminToolkit;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcadminToolkit.whitelist.whitelist;
import org.mcadminToolkit.serverStats.serverStats;

public class whitelistGetCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] strings) {
        JavaPlugin plugin = mcadminToolkit.getPlugin(mcadminToolkit.class);
        //String[] whitelistArray = whitelist.getWhiteList(plugin);
        String cpuUsage = serverStats.playersOnline(plugin);
        sender.sendMessage("Server CPU usage is: " + cpuUsage);
        return false;
    }
}
