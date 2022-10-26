package org.mcadminToolkit;

import org.bukkit.plugin.java.JavaPlugin;

public final class mcadminToolkit extends JavaPlugin {
    public JavaPlugin plugin = this;
    @Override
    public void onLoad () {
        getLogger().info("MCAdmin Toolkit Connector successfully loaded");
    }

    @Override
    public void onDisable() {

    }

    @Override
    public void onEnable() {
        getLogger().info("MCAdmin Toolkit Connector successfully initialized");
        this.getCommand("test").setExecutor(new testCommand());
        this.getCommand("whitelistGet").setExecutor(new whitelistGetCommand());

        //express init
        JavaPlugin plugin = mcadminToolkit.getPlugin(mcadminToolkit.class);
        expressServer.initializeServer(plugin);
    }
}