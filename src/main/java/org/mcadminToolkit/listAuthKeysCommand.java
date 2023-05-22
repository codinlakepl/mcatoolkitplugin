package org.mcadminToolkit;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import org.mcadminToolkit.sqlHandler.AuthKeyListingException;
import org.mcadminToolkit.sqlHandler.authKeyLister;
import org.mcadminToolkit.sqlHandler.sqlConnector;

public class listAuthKeysCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!sender.isOp()) {
            sender.sendMessage("You must be op to perform this command");
            return false;
        }

        String[] labels;

        try {
            labels = authKeyLister.listAuthKeys(sqlConnector.connection);
        } catch (AuthKeyListingException e) {
            sender.sendMessage("An error occurred");
            return false;
        }

        String message = "";

        message += "Registered auth keys:\n";

        for (int i = 0; i < labels.length; i++) {
            message += labels[i] + "\n";
        }

        sender.sendMessage(message);

        return true;
    }
}
