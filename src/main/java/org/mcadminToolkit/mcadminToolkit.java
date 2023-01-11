package org.mcadminToolkit;

import jdk.nashorn.internal.parser.JSONParser;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
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
        this.getCommand ("regenerateConsoleAuthkeys").setExecutor(new regenerateConsoleAuthkeysCommand());

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

    @EventHandler
    public void onCommandPreprocess (PlayerCommandPreprocessEvent event) {
        if (expressServer.client == null) return;

        String message = event.getMessage();

        String[] array = message.split(" ");

        if (array.length >= 2) {
            if (array[0].equalsIgnoreCase("/ban") || array[0].equalsIgnoreCase("ban") ||
            array[0].equalsIgnoreCase("/ban-ip") || array[0].equalsIgnoreCase("ban-ip")) {
                MediaType JSON = MediaType.get ("application/json; charset=utf-8");

                JSONObject jsonObject = new JSONObject();

                String player = event.getPlayer().getName();

                jsonObject.put("user", player);
                jsonObject.put("log", "Attempt to ban player " + array[1]);
                jsonObject.put("sendPush", true);

                RequestBody body = RequestBody.create(jsonObject.toString(), JSON);
                Request request = new Request.Builder()
                        .url (expressServer.baseUrl + "/addLog")
                        .post(body)
                        .build();

                try {
                    expressServer.client.newCall(request).execute();
                } catch (IOException e) {
                    return;
                }
            }
        }
    }
}