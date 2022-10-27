package org.mcadminToolkit;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcadminToolkit.serverStats.serverStats;
import org.mcadminToolkit.sqlHandler.AuthKeyRegistrationException;
import org.mcadminToolkit.sqlHandler.authKeyRegistration;
import org.mcadminToolkit.sqlHandler.sqlConnector;

public class createAuthKeyCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] strings) {
        JavaPlugin plugin = mcadminToolkit.getPlugin(mcadminToolkit.class);
        //String[] whitelistArray = whitelist.getWhiteList(plugin);
        try {
            String authKey = authKeyRegistration.registerNewAuthKey(sqlConnector.connection, 1);
            sender.sendMessage(authKey);
        } catch (AuthKeyRegistrationException e) {
            throw new RuntimeException(e);
        }
        return false;
    }
}
