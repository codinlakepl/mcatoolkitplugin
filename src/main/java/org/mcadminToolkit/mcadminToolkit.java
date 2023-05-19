package org.mcadminToolkit;

import org.bukkit.plugin.java.JavaPlugin;
import org.json.JSONObject;
import org.mcadminToolkit.sqlHandler.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

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
        Connection con = null;
        try{
            con = sqlConnector.connect("database.db");
            sqlStructureConstructor.checkStructure(con);
            //express init
            JavaPlugin plugin = mcadminToolkit.getPlugin(mcadminToolkit.class);
            expressServer.initializeServer(plugin, con, port);
        }catch (Exception e) {
            System.out.println(e);
        }

        getServer().getPluginManager().registerEvents(new commandListener(), this);

        Connection finalCon = con;
        getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
            @Override
            public void run() {
                Statement statement;

                try {
                    statement = finalCon.createStatement();
                    statement.setQueryTimeout(30);

                    ResultSet lastExecutionTime = statement.executeQuery("SELECT\n" +
                            "  *\n" +
                            "FROM\n" +
                            "  `workCheckers`\n" +
                            "ORDER BY\n" +
                            "  `executionTime` DESC\n" +
                            "LIMIT 1");

                    if (lastExecutionTime.next()) {
                        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                        //Date date = lastExecutionTime.getDate("executionTime");
                        Date date = df.parse(lastExecutionTime.getString("executionTime"));
                        org.mcadminToolkit.sqlHandler.logger.createLog(finalCon, org.mcadminToolkit.sqlHandler.logger.Sources.SYSTEM, "SYSTEM", "Server stopped on " + df.format(date));
                    }

                    org.mcadminToolkit.sqlHandler.logger.createLog(finalCon, org.mcadminToolkit.sqlHandler.logger.Sources.SYSTEM, "SYSTEM", "Server started");
                } catch (LoggingException e) {
                    throw new RuntimeException(e);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }

                Timer timer = new Timer();

                timer.schedule(new SaveExecutionTime(), 10000, 10000);
            }
        });
    }
}

class SaveExecutionTime extends TimerTask {
    public void run () {
        Connection con = expressServer.conGlobal;

        Statement statement;
        PreparedStatement preparedStatement;
        try {
            statement = con.createStatement();
            statement.setQueryTimeout(30);

            statement.executeUpdate("INSERT INTO workCheckers DEFAULT VALUES");

            ResultSet rs = statement.executeQuery("SELECT count(*) AS numberOfRows FROM workCheckers");

            if (rs.next()) {

                int numberOfRows = rs.getInt("numberOfRows");

                if (numberOfRows > 3) {
                    statement.executeUpdate("DELETE FROM workCheckers WHERE id IN (\n" +
                            "  SELECT id FROM workCheckers ORDER BY executionTime ASC LIMIT " + (numberOfRows - 1) + "\n" +
                            ")");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}