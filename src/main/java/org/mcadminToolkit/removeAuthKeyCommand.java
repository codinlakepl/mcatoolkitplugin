package org.mcadminToolkit;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import org.mcadminToolkit.sqlHandler.AuthKeyRemovingException;
import org.mcadminToolkit.sqlHandler.sqlConnector;
import org.mcadminToolkit.sqlHandler.authKeyRemover;

public class removeAuthKeyCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.isOp()) {
            sender.sendMessage("You must be op to perform this command");
            return false;
        }

        if (args.length < 1) {
            sender.sendMessage ("Please specify label");
            return false;
        }

        String label = args[0];

        try {
            authKeyRemover.removeAuthKey(sqlConnector.connection, label);
        } catch (AuthKeyRemovingException e) {
            sender.sendMessage("Cannot remove auth key with label that don't exists");
            return false;
        }

        sender.sendMessage("Successfully removed auth key with label '" + label + "'");

        return true;
    }
}
