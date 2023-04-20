package org.mcadminToolkit;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcadminToolkit.sqlHandler.AuthKeyRegistrationException;
import org.mcadminToolkit.sqlHandler.authKeyRegistration;
import org.mcadminToolkit.sqlHandler.sqlConnector;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class createAuthKeyCommand implements CommandExecutor {

    public static String actualAuthKey;
    public static String accessCode;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!sender.isOp()) {
            sender.sendMessage("You must be op to perform this command");
            return false;
        }

        if (args.length < 2) {
            sender.sendMessage ("Please specify security level and label");
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

        String label = args[1];

        try {
            String authKey = authKeyRegistration.registerNewAuthKey(sqlConnector.connection, secLvl, label);
            String code = "";

            String alphabet = "abcdefghijklmnoprstquwxyzABCDEFGHJIKLMNOPRSTQUWXYZ1234567890";

            for (int i = 0; i < 5; i++) {
                code = code + alphabet.charAt(new Random().nextInt(alphabet.length()));
            }

            actualAuthKey = authKey;
            accessCode = code;

            Timer timer = new Timer ();
            timer.schedule(new authKeyRemover(), TimeUnit.MINUTES.toMillis(5));

            sender.sendMessage("Successfully generated auth key.\nThis is code to download it in MC-Admin-Toolkit application:\n" + code + "\nIt will resist 5 minutes");
        } catch (AuthKeyRegistrationException e) {
            sender.sendMessage("An error occurred");
            return false;
        }
        return true;
    }
}

// After 5 minutes removes opportunity to download freshly created auth key
class authKeyRemover extends TimerTask {
    public authKeyRemover () {}

    public void run () {
        createAuthKeyCommand.actualAuthKey = "";
    }
}