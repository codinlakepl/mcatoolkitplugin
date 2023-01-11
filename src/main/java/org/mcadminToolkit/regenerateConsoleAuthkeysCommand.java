package org.mcadminToolkit;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.json.JSONException;

import java.io.IOException;

public class regenerateConsoleAuthkeysCommand implements CommandExecutor {

    @Override
    public boolean onCommand (CommandSender sender, Command command, String s, String[] args) {
        if (!sender.isOp()) {
            sender.sendMessage("You must be op to perform this command");
            return false;
        }

        try {
            expressServer.generateAuthkeysForConsole();
        } catch (IOException | JSONException e) {
            sender.sendMessage("Auth keys was not generated successfully. Make sure, your config file is valid");
            return false;
        }

        return true;
    }
}
