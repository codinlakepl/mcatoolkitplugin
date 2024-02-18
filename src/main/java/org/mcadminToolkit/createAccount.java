package org.mcadminToolkit;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.mcadminToolkit.sqlHandler.*;

public class createAccount implements CommandExecutor {

    public static String actualAuthKey;
    public static String accessCode;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
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
            String tempPass = accountHandler.createAcc(sqlConnector.connection, login, secLvl);

            sender.sendMessage("Successfully created an account.\nThis is temp password that has to be changed after first login in app:\n" + tempPass);
        } catch (TooLongLoginException e) {
            sender.sendMessage("Login has to be max 50 chars long");
            return false;
        } catch (AccountException e) {
            sender.sendMessage("An error occurred");
            return false;
        } catch (LoginExistsException e) {
            sender.sendMessage("Account with login " + login + " already exists");
            return false;
        }
        return true;
    }
}