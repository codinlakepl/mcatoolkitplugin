package org.mcadminToolkit;

import org.bukkit.plugin.java.JavaPlugin;

public final class mcadminToolkit extends JavaPlugin {

    @Override
    public void onLoad () {
        getLogger().info("MCAdmin Toolkit Connector successfully loaded");
    }

    @Override
    public void onDisable() {}

    @Override
    public void onEnable() {
        getLogger().info("MCAdmin Toolkit Connector successfully initialized");
    }
}