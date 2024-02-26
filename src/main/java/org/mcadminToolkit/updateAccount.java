package org.mcadminToolkit;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.mcadminToolkit.sqlHandler.*;

public class updateAccount implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.isOp()) {
            sender.sendMessage("You must be op to perform this command");
            return false;
        }

        if (args.length < 2) {
            sender.sendMessage ("Please specify security level and login");
            return false;
        }

        int secLvl;

        try {
            secLvl = Integer.parseInt(args[0]);
            if (!(secLvl >= 1 && secLvl <= 6)) {
                sender.sendMessage("Security level must be a number between 1 (inclusive) and 6 (inclusive)");
                return false;
            }
        } catch (Exception e) {
            sender.sendMessage("Security level must be a number");
            return false;
        }

        String login = args[1];

        try {
            accountHandler.updateAcc(sqlConnector.connection, login, secLvl);

            sender.sendMessage("Successfully updated an account.");
        } catch (AccountException e) {
            sender.sendMessage("An error occurred");
            return false;
        } catch (LoginDontExistException e) {
            sender.sendMessage("Account with login " + login + " does not exists");
            return false;
        }
        return true;
    }
}
