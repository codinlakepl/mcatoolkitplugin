package org.mcadminToolkit;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import org.mcadminToolkit.sqlHandler.AccountException;
import org.mcadminToolkit.sqlHandler.accountHandler;
import org.mcadminToolkit.sqlHandler.sqlConnector;

public class listAccounts implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!sender.isOp()) {
            sender.sendMessage("You must be op to perform this command");
            return false;
        }

        String[] logins;

        try {
            logins = accountHandler.listAccs(sqlConnector.connection);
        } catch (AccountException e) {
            sender.sendMessage("An error occurred");
            return false;
        }

        String message = "";

        message += "Registered accounts:\n";

        for (int i = 0; i < logins.length; i++) {
            message += logins[i] + "\n";
        }

        sender.sendMessage(message);

        return true;
    }
}
