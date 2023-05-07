package org.mcadminToolkit;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class commandListener implements Listener {

    @EventHandler
    public void onPlayerCommandPreprocessEvent (PlayerCommandPreprocessEvent event) {
        expressServer.pluginGlobal.getLogger().info("Cancelled: " + (event.isCancelled() ? "true" : "false"));
    }
}
