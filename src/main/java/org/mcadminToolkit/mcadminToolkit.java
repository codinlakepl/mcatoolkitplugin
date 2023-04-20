package org.mcadminToolkit;

import jdk.nashorn.internal.parser.JSONParser;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.JSONObject;
import org.mcadminToolkit.sqlHandler.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;

public final class mcadminToolkit extends JavaPlugin {
    public JavaPlugin plugin = this;

    public static JSONObject commandLogging;
    public static JSONObject appLogging;

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
        /*this.getCommand("test").setExecutor(new testCommand());
        this.getCommand("whitelistGet").setExecutor(new whitelistGetCommand());*/
        this.getCommand("createAuthKey").setExecutor(new createAuthKeyCommand());
        this.getCommand("listAuthKeys").setExecutor(new listAuthKeysCommand());
        this.getCommand("removeAuthKey").setExecutor(new removeAuthKeyCommand());

        File catalog = new File ("./plugins/MCAdmin-Toolkit-Connector");

        if (!catalog.exists()) {
            catalog.mkdir();
        }

        File configFile = new File ("./plugins/MCAdmin-Toolkit-Connector/config.json");

        if (!configFile.exists()) {
            String config = configBuilder.build();

            try {
                configFile.createNewFile();
            } catch (IOException e) {
                getLogger().warning("Can't create config file");
                getLogger().warning("Can't start https server - plugin won't work");
                return;
            }

            try {
                FileWriter configWriter = new FileWriter(configFile);
                configWriter.write(config);
                configWriter.close();
            } catch (IOException e) {
                getLogger().warning("Can't write to config file");
                getLogger().warning("Can't start https server - plugin won't work");
                return;
            }
        }

        int port = 0;

        try {
            String configText = new String(Files.readAllBytes(Paths.get("./plugins/MCAdmin-Toolkit-Connector/config.json")), StandardCharsets.UTF_8);

            JSONObject config = new JSONObject(configText);
            port = config.getInt("port");

            commandLogging = config.getJSONObject("commandLogging");
            appLogging = config.getJSONObject("appLogging");
        } catch (IOException e) {
            getLogger().warning("Can't read to config file");
            getLogger().warning("Can't start https server - plugin won't work");
            return;
        }

        File certFile = new File ("./plugins/MCAdmin-Toolkit-Connector/rootCA.crt");
        File keyFile = new File("./plugins/MCAdmin-Toolkit-Connector/rootCA.key");

        if (!certFile.exists() || !keyFile.exists()) {
            getLogger().warning("Can't get cert files");
            getLogger().warning("Please create cert files and restart server");
            getLogger().warning("Can't start https server - plugin won't work");
            return;
        }

        //db init
        Connection con;
        try{
            con = sqlConnector.connect("database.db");
            sqlStructureConstructor.checkStructure(con);
            //express init
            JavaPlugin plugin = mcadminToolkit.getPlugin(mcadminToolkit.class);
            expressServer.initializeServer(plugin, con, port);
        }catch (Exception e) {
            System.out.println(e);
        }
    }
}