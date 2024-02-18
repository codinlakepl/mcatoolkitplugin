package org.mcadminToolkit;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.JSONObject;
import org.mcadminToolkit.sqlHandler.*;

import java.io.*;
import java.sql.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

import com.waheed.create.certificate.SelfSignedCertificate;

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
        this.getCommand("createAuthKey").setExecutor(new createAccount());
        this.getCommand("listAuthKeys").setExecutor(new listAccounts());
        this.getCommand("removeAuthKey").setExecutor(new removeAuthKeyCommand());

        File catalog = getDataFolder();

        if (!catalog.exists()) {
            catalog.mkdir();
        }

        // Config ---------------------

        saveDefaultConfig();

        FileConfiguration config = getConfig();

        int port = config.getInt("port");

        ConfigurationSection yamlCommandLogging = config.getConfigurationSection("commandLogging");
        ConfigurationSection yamlAppLogging = config.getConfigurationSection("appLogging");

        Set<String> yamlCommandLoggingKeys = yamlCommandLogging.getKeys(false);
        Set<String> yamlAppLoggingKeys = yamlAppLogging.getKeys(false);

        appLogging = new JSONObject();
        commandLogging = new JSONObject();

        for (String key : yamlCommandLoggingKeys) {
            ConfigurationSection command = yamlCommandLogging.getConfigurationSection(key);

            JSONObject commandJson = new JSONObject();
            commandJson.put("log", command.getBoolean("log"));
            commandJson.put("push", command.getBoolean("push"));

            commandLogging.put(key, commandJson);
        }

        for (String key : yamlAppLoggingKeys) {
            ConfigurationSection command = yamlAppLogging.getConfigurationSection(key);

            JSONObject commandJson = new JSONObject();
            commandJson.put("log", command.getBoolean("log"));
            commandJson.put("push", command.getBoolean("push"));

            appLogging.put(key, commandJson);
        }

        // Cert stuff ---------------------

        File certFile = new File (catalog, "rootCA.crt");
        File keyFile = new File(catalog, "rootCA.key");

        if (!certFile.exists() || !keyFile.exists()) {
            getLogger().info("Can't get cert files");
            getLogger().info("Regenerating cert files...");

            try {
                SelfSignedCertificate selfSignedCertificate = new SelfSignedCertificate(
                        "RSA",
                        "CN=" +
                                config.getString("cn") +
                                ", O=" +
                                config.getString("o") +
                                ", OU=" +
                                config.getString("ou") +
                                ", L=" +
                                config.getString("l") +
                                ", ST=" +
                                config.getString("st") +
                                ", C=" +
                                config.getString("c"),
                        config.getInt("certBits")
                );

                Base64.Encoder encoder = Base64.getEncoder();

                String publicKey = "-----BEGIN CERTIFICATE-----\n";
                publicKey += encoder.encodeToString(
                        selfSignedCertificate.cert.getEncoded()
                );
                publicKey += "\n-----END CERTIFICATE-----\n";

                FileWriter certWriter = new FileWriter(certFile, false);
                certWriter.write(publicKey);
                certWriter.close();

                String privateKey = "-----BEGIN PRIVATE KEY-----\n";
                privateKey += encoder.encodeToString(
                        selfSignedCertificate.privateKey.getEncoded()
                );
                privateKey += "\n-----END PRIVATE KEY-----\n";

                FileWriter keyWriter = new FileWriter(keyFile, false);
                keyWriter.write(privateKey);
                keyWriter.close();
            } catch (Exception e) {
                getLogger().warning("Wrong cert configuration or other error occurred while creating default cert files");
                getLogger().warning(e.getMessage());
                getLogger().warning("Can't start https server - plugin won't work");

                return;
            }

        }

        // Database stuff ---------------------

        //db init
        Connection con = null;
        try{
            saveResource("database.db", false);
            con = sqlConnector.connect(new File(catalog, "database.db"));
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
                        org.mcadminToolkit.sqlHandler.logger.createLog(finalCon, org.mcadminToolkit.sqlHandler.logger.Sources.SYSTEM, "SYSTEM", "Server stopped", date);
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