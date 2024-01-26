package org.mcadminToolkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.json.JSONObject;
import org.mcadminToolkit.sqlHandler.LoggingException;
import org.mcadminToolkit.sqlHandler.logger;
import java.util.Iterator;

public class commandListener implements Listener {

    @EventHandler
    public void onPlayerCommandPreprocessEvent (PlayerCommandPreprocessEvent event) {
        String message = event.getMessage();

        String commandName = message.split(" ")[0];

        if (commandName.charAt(0) == '/') {
            commandName = commandName.substring(1);
        }

        JSONObject configuredCommands = mcadminToolkit.commandLogging;

        for (Iterator<String> it = configuredCommands.keys(); it.hasNext(); ) {
            String command = it.next();

            if (commandName.equals(command)) {
                JSONObject commandProperties = configuredCommands.getJSONObject(command);

                boolean log = commandProperties.getBoolean("log");

                if (log) {
                    try {
                        logger.createLog(expressServer.conGlobal, logger.Sources.MINECRAFT, event.getPlayer().getName(), "Executed command " + message);
                    } catch (LoggingException e) {
                        throw new RuntimeException(e);
                    }
                }

                break;
            }

        }
    }

    @EventHandler
    public void onServerCommandPreprocessEvent (ServerCommandEvent event) {
        String message = event.getCommand();

        String commandName = message.split(" ")[0];

        JSONObject configuredCommands = mcadminToolkit.commandLogging;

        for (Iterator<String> it = configuredCommands.keys(); it.hasNext(); ) {
            String command = it.next();

            if (commandName.equals(command)) {
                JSONObject commandProperties = configuredCommands.getJSONObject(command);

                boolean log = commandProperties.getBoolean("log");

                if (log) {
                    try {
                        logger.createLog(expressServer.conGlobal, logger.Sources.CONSOLE, "SERVER", "Executed command " + message);
                    } catch (LoggingException e) {
                        throw new RuntimeException(e);
                    }
                }

                break;
            }

        }
    }
}
