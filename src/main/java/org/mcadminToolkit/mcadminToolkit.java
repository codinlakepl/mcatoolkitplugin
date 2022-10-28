package org.mcadminToolkit;

import org.bukkit.plugin.java.JavaPlugin;
import org.mcadminToolkit.sqlHandler.*;

import java.sql.Connection;
import java.sql.SQLException;

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

        //db init
        Connection con;
        try{
            con = sqlConnector.connect("database.db");
            sqlStructureConstructor.checkStructure(con);
            //express init
            JavaPlugin plugin = mcadminToolkit.getPlugin(mcadminToolkit.class);
            expressServer.initializeServer(plugin, con);
        }catch (Exception e) {
            System.out.println(e);
        }
    }
}