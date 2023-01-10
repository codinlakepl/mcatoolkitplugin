package org.mcadminToolkit;

import jdk.nashorn.internal.parser.JSONParser;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.JSONException;
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

        File catalog = new File ("./plugins/MCAdmin-Toolkit-Connector");

        if (!catalog.exists()) {
            catalog.mkdir();
        }

        File configFile = new File ("./plugins/MCAdmin-Toolkit-Connector/config.json");

        if (!configFile.exists()) {
            JSONObject config = new JSONObject();
            config.put("port", 4096);
            config.put("address", "");
            JSONObject consoleConfig = new JSONObject();
            consoleConfig.put("password", "");
            consoleConfig.put("email", "");
            config.put("consoleLogin", consoleConfig);

            try {
                configFile.createNewFile();
            } catch (IOException e) {
                getLogger().info("Can't create config file");
                System.exit(1);
            }

            try {
                FileWriter configWriter = new FileWriter(configFile);
                configWriter.write(config.toString());
                configWriter.close();
            } catch (IOException e) {
                getLogger().info("Can't write to config file");
                System.exit(1);
            }
        }

        int port = 0;
        String address = "";
        String consoleEmail = "";
        String consolePassword = "";

        try {
            String configText = new String(Files.readAllBytes(Paths.get("./plugins/MCAdmin-Toolkit-Connector/config.json")), StandardCharsets.UTF_8);

            JSONObject config = new JSONObject(configText);
            port = config.getInt("port");
            address = config.getString("address");

            JSONObject consoleConfig = config.getJSONObject("consoleLogin");
            consoleEmail = consoleConfig.getString("email");
            consolePassword = consoleConfig.getString("password");
        } catch (IOException e) {
            getLogger().info("Can't read the config file");
            System.exit(1);
        }

        File certFile = new File ("./plugins/MCAdmin-Toolkit-Connector/rootCA.crt");
        File keyFile = new File("./plugins/MCAdmin-Toolkit-Connector/rootCA.key");

        if (!certFile.exists() || !keyFile.exists()) {
            getLogger().info("Can't get cert files");
            System.exit(1);
        }

        //db init
        Connection con;
        try{
            con = sqlConnector.connect("database.db");
            sqlStructureConstructor.checkStructure(con);
            //express init
            JavaPlugin plugin = mcadminToolkit.getPlugin(mcadminToolkit.class);
            expressServer.initializeServer(plugin, con, port, address, consoleEmail, consolePassword);
            try {
                expressServer.generateAuthkeysForConsole();
            } catch (IOException | JSONException e) {
                getLogger().info("Can't connect to console, check your login credentials");
            }
        }catch (Exception e) {
            System.out.println(e);
        }
    }
}