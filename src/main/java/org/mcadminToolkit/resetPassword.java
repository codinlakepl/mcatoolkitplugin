package org.mcadminToolkit;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.mcadminToolkit.sqlHandler.*;

public class resetPassword implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
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
            String tempPass = accountHandler.resetPass(sqlConnector.connection, login);

            sender.sendMessage("Successfully reset password for an account.\nThis is temp password that has to be changed after login in app:\n" + tempPass);
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
