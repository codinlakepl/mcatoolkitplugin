package org.mcadminToolkit;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import org.mcadminToolkit.sqlHandler.AccountException;
import org.mcadminToolkit.sqlHandler.LoginDontExistException;
import org.mcadminToolkit.sqlHandler.accountHandler;
import org.mcadminToolkit.sqlHandler.sqlConnector;

public class removeAccount implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.isOp()) {
            sender.sendMessage("You must be op to perform this command");
            return false;
        }

        if (args.length < 1) {
            sender.sendMessage ("Please specify login");
            return false;
        }

        String login = args[0];

        try {
            accountHandler.deleteAcc(sqlConnector.connection, login);
        } catch (AccountException e) {
            sender.sendMessage("An error occurred");
            return false;
        } catch (LoginDontExistException e) {
            sender.sendMessage("Cannot remove account that doesn't exist");
            return false;
        }

        sender.sendMessage("Successfully removed account with login '" + login + "'");

        return true;
    }
}
