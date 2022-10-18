package org.mcadminToolkit;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcadminToolkit.whitelist.whitelist;
import org.mcadminToolkit.mcadminToolkit;

public class testCommand implements CommandExecutor {
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        sender.sendMessage("Plugin działa więc sie ciesz :P");
        return false;
    }
}
