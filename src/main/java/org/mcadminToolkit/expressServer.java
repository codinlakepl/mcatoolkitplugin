package org.mcadminToolkit;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.io.Receiver;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.HttpString;
import nl.altindag.ssl.SSLFactory;
import nl.altindag.ssl.util.PemUtils;

import javax.imageio.ImageIO;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509ExtendedKeyManager;
import javax.net.ssl.X509ExtendedTrustManager;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.FileSystems;
import java.sql.Connection;
import java.time.Duration;
import java.util.*;
import org.bukkit.Server;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mcadminToolkit.auth.CreateAccountException;
import org.mcadminToolkit.auth.InvalidSessionException;
import org.mcadminToolkit.auth.account;
import org.mcadminToolkit.auth.jwtHandler;
import org.mcadminToolkit.banlist.banlist;
import org.mcadminToolkit.playermanagement.kick;
import org.mcadminToolkit.playerslist.offlineplayerslist;
import org.mcadminToolkit.playerslist.playerInfo;
import org.mcadminToolkit.playerslist.playerslist;
import org.mcadminToolkit.serverStats.serverStats;

import org.mcadminToolkit.playermanagement.ban;
import org.mcadminToolkit.sqlHandler.LoggingException;
import org.mcadminToolkit.sqlHandler.logger;
import org.mcadminToolkit.whitelist.whitelist;

public class expressServer {
    public static JavaPlugin pluginGlobal;
    public static Connection conGlobal;

    public static void initializeServer(JavaPlugin plugin, Connection con, int port) {

        pluginGlobal = plugin;
        conGlobal = con;

        X509ExtendedKeyManager keyManager = PemUtils.loadIdentityMaterial(FileSystems.getDefault().getPath(new File (pluginGlobal.getDataFolder(), "rootCA.crt").getPath()), FileSystems.getDefault().getPath(new File (pluginGlobal.getDataFolder(), "rootCA.key").getPath()));

        X509ExtendedTrustManager trustManager = PemUtils.loadTrustMaterial(FileSystems.getDefault().getPath(new File (pluginGlobal.getDataFolder(), "rootCA.crt").getPath()));

        SSLFactory sslFactory = SSLFactory.builder()
                .withIdentityMaterial(keyManager)
                .withTrustMaterial(trustManager)
                .build();

        SSLContext sslContext = sslFactory.getSslContext();

        Undertow server = Undertow.builder()
                .addHttpsListener(port, "0.0.0.0", sslContext)
                .setHandler(Handlers.pathTemplate()
                        .add("/", new GetIndex())
                        .add("/WHITELIST", new PostWhitelist())
                        .add("/PLAYERS", new PostPlayers())
                        .add("/BANLIST", new PostBanlist())
                        .add("/BAN", new PostBan())
                        .add("/BANIP", new PostBanIP())
                        .add("/UNBAN", new PostUnban())
                        .add("/UNBANIP", new PostUnbanIP())
                        .add("/KICK", new PostKick())
                        .add("/WHITEON", new PostWhiteOn())
                        .add("/WHITEOFF", new PostWhiteOff())
                        .add("/WHITEADD", new PostWhiteAdd())
                        .add("/WHITEREMOVE", new PostWhiteRemove())
                        .add("/STATS", new PostStats())
                        .add("/LOGS", new PostLogs())
                        .add("/LOGIN", new PostLogin())
                        .add("/GETAUTHKEY", new PostAuthKey())
                )
                .build();

        server.start();

        plugin.getLogger().info("Server https initialized successfully");
    }
}

class ServerCommons {
    static account checkSession (String authHeader) {

        account acc;

        String[] authSplit = authHeader.split(" ");

        if (authSplit.length < 2 || authSplit[0] != "Bearer") {
            return null;
        }

        String token = authSplit[1];

        try {
            acc = jwtHandler.verifyToken(token);
        } catch (InvalidSessionException e) {
            return null;
        }

        return acc;
    }
}

class GetIndex implements HttpHandler {
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        exchange.getResponseSender().send("MCAdmin Toolkit API v0.1");
    }
}

class PostWhitelist implements HttpHandler {
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        exchange.getResponseHeaders().put(new HttpString("Access-Control-Allow-Origin"), "*");

        HttpString method = exchange.getRequestMethod();

        if (!method.equalToString("POST")) {
            exchange.setStatusCode(405);
            return;
        }

        exchange.getRequestReceiver().receiveFullBytes(new Receiver.FullBytesCallback() {
            @Override
            public void handle(HttpServerExchange exchange, byte[] message) {

                String authHeader = exchange.getRequestHeaders().get("Authorization").toString();

                account acc = ServerCommons.checkSession(authHeader);

                // request to login if user isn't logged in or, extedning session time failed
                // input text: session key
                if (acc == null /*&& !extendSession(body)*/) {
                    exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
                    exchange.getResponseSender().send("login");
                    return;
                }

                JSONArray jsonArray = new JSONArray();

                String[] arr = whitelist.getWhiteList(expressServer.pluginGlobal);

                for (String player :
                        arr) {
                    jsonArray.put(player);
                }

                JSONObject obj = new JSONObject();
                obj.put("isEnabled", Boolean.parseBoolean(whitelist.checkWhitelistStatus(expressServer.pluginGlobal)));
                obj.put("players", jsonArray);

                exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
                exchange.getResponseSender().send(obj.toString());
            }
        });
    }
}

class PostPlayers implements HttpHandler {
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        exchange.getResponseHeaders().put(new HttpString("Access-Control-Allow-Origin"), "*");

        HttpString method = exchange.getRequestMethod();

        if (!method.equalToString("POST")) {
            exchange.setStatusCode(405);
            return;
        }

        exchange.getRequestReceiver().receiveFullBytes(new Receiver.FullBytesCallback() {
            @Override
            public void handle(HttpServerExchange exchange, byte[] message) {
                String authHeader = exchange.getRequestHeaders().get("Authorization").toString();

                account acc = ServerCommons.checkSession(authHeader);

                if (acc == null /*&& !extendSession(body)*/) {
                    exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
                    exchange.getResponseSender().send("login");
                    return;
                }

                List<playerInfo> onlinePlayers = new ArrayList<playerInfo> ();

                List<playerInfo> offlinePlayers = new ArrayList<playerInfo>();

                onlinePlayers.addAll(Arrays.asList(playerslist.getPlayers(expressServer.pluginGlobal)));
                offlinePlayers.addAll(Arrays.asList(offlineplayerslist.getOfflinePlayers(expressServer.pluginGlobal)));

                JSONArray onlineArray = new JSONArray();
                JSONArray offlineArray = new JSONArray();

                for (playerInfo info : onlinePlayers) {
                    //playerNicknames.add (info.name);

                    JSONObject player = new JSONObject();
                    player.put("name", info.name);
                    player.put("uuid", info.uuid);

                    onlineArray.put(player);
                }

                for (playerInfo info : offlinePlayers) {
                    //playerNicknames.add (info.name);
                    offlineArray.put(info.name);
                }

                JSONObject obj = new JSONObject();
                obj.put ("online", onlineArray);
                obj.put ("offline", offlineArray);

                exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
                exchange.getResponseSender().send(obj.toString());
            }
        });
    }
}

class PostBanlist implements HttpHandler {
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        exchange.getResponseHeaders().put(new HttpString("Access-Control-Allow-Origin"), "*");

        HttpString method = exchange.getRequestMethod();

        if (!method.equalToString("POST")) {
            exchange.setStatusCode(405);
            return;
        }

        exchange.getRequestReceiver().receiveFullBytes(new Receiver.FullBytesCallback() {
            @Override
            public void handle(HttpServerExchange exchange, byte[] message) {
                String authHeader = exchange.getRequestHeaders().get("Authorization").toString();

                account acc = ServerCommons.checkSession(authHeader);

                if (acc == null /*&& !extendSession(body)*/) {
                    exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
                    exchange.getResponseSender().send("login");
                    return;
                }

                List<String> normalBans = new ArrayList<String>();
                List<String> ipBans = new ArrayList<String>();
                normalBans.addAll(Arrays.asList(banlist.playerBanList(expressServer.pluginGlobal)));
                ipBans.addAll(Arrays.asList(banlist.ipBanList(expressServer.pluginGlobal)));

                JSONArray normalBansJson = new JSONArray (normalBans);
                JSONArray ipBansJson = new JSONArray (ipBans);

                JSONObject json = new JSONObject();
                json.put("normalBans", normalBansJson);
                json.put("ipBans", ipBans);

                exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
                exchange.getResponseSender().send(json.toString());

            }
        });
    }
}

class PostBan implements HttpHandler {

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        exchange.getResponseHeaders().put(new HttpString("Access-Control-Allow-Origin"), "*");

        HttpString method = exchange.getRequestMethod();

        if (!method.equalToString("POST")) {
            exchange.setStatusCode(405);
            return;
        }

        exchange.getRequestReceiver().receiveFullBytes(new Receiver.FullBytesCallback() {
            @Override
            public void handle(HttpServerExchange exchange, byte[] message) {
                String body = new String(message);

                JSONObject json = new JSONObject(body); // {"username": "IpyZ", "reason": "test123", "hours": 2}

                // input json: username, sessionKey, reason, hours
                String username = json.getString("username");
                String reason = json.getString("reason");
                int hours = json.getInt("hours");

                String authHeader = exchange.getRequestHeaders().get("Authorization").toString();

                account acc = ServerCommons.checkSession(authHeader);

                if (acc == null) {
                    exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
                    exchange.getResponseSender().send("login");
                    return;
                }

                if (!(acc.secLevel <= 4)) {
                    exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
                    exchange.getResponseSender().send("perms");
                    return;
                }

                try {
                    ban.ban(expressServer.pluginGlobal, username, reason, Date.from(new Date().toInstant().plus(Duration.ofHours(hours))));

                    exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
                    exchange.getResponseSender().send("Success");
                } catch (Exception e) {
                    exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
                    exchange.getResponseSender().send(e.getMessage());
                }

                boolean log = mcadminToolkit.appLogging.getJSONObject("ban").getBoolean("log");

                if (log) {
                    try {
                        logger.createLog(expressServer.conGlobal, logger.Sources.APP, acc.label, "Banned player " + username);
                    } catch (LoggingException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
    }
}

class PostBanIP implements HttpHandler {

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        exchange.getResponseHeaders().put(new HttpString("Access-Control-Allow-Origin"), "*");

        HttpString method = exchange.getRequestMethod();

        if (!method.equalToString("POST")) {
            exchange.setStatusCode(405);
            return;
        }

        exchange.getRequestReceiver().receiveFullBytes(new Receiver.FullBytesCallback() {
            @Override
            public void handle(HttpServerExchange exchange, byte[] message) {
                String body = new String(message);

                JSONObject json = new JSONObject(body); // {"ip": "127.0.0.1"}

                // input json: username, sessionKey, reason
                String playerName = json.getString("username");
                String reason = json.getString("reason");

                String authHeader = exchange.getRequestHeaders().get("Authorization").toString();

                account acc = ServerCommons.checkSession(authHeader);

                if (acc == null) {
                    exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
                    exchange.getResponseSender().send("login");
                    return;
                }

                if (!(acc.secLevel <= 2)) {
                    exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
                    exchange.getResponseSender().send("perms");
                    return;
                }


                try {
                    ban.banIp(expressServer.pluginGlobal, playerName, reason);

                    exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
                    exchange.getResponseSender().send("Success");
                } catch (Exception e) {
                    exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
                    exchange.getResponseSender().send(e.getMessage());
                }

                boolean log = mcadminToolkit.appLogging.getJSONObject("banIp").getBoolean("log");

                if (log) {
                    try {
                        logger.createLog(expressServer.conGlobal, logger.Sources.APP, acc.label, "IP banned player " + playerName);
                    } catch (LoggingException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
    }
}

class PostUnban implements HttpHandler {

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        exchange.getResponseHeaders().put(new HttpString("Access-Control-Allow-Origin"), "*");

        HttpString method = exchange.getRequestMethod();

        if (!method.equalToString("POST")) {
            exchange.setStatusCode(405);
            return;
        }

        exchange.getRequestReceiver().receiveFullBytes(new Receiver.FullBytesCallback() {
            @Override
            public void handle(HttpServerExchange exchange, byte[] message) {
                String body = new String(message);

                JSONObject json = new JSONObject(body); // {"username": "IpyZ"}

                // input json: username, sessionKey
                String username = json.getString("username");

                String authHeader = exchange.getRequestHeaders().get("Authorization").toString();

                account acc = ServerCommons.checkSession(authHeader);

                if (acc == null) {
                    exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
                    exchange.getResponseSender().send("login");
                    return;
                }

                if (!(acc.secLevel <= 4)) {
                    exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
                    exchange.getResponseSender().send("perms");
                    return;
                }

                try {
                    ban.unban(expressServer.pluginGlobal, username);

                    exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
                    exchange.getResponseSender().send("Success");
                } catch (Exception e) {
                    exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
                    exchange.getResponseSender().send(e.getMessage());
                }

                boolean log = mcadminToolkit.appLogging.getJSONObject("unban").getBoolean("log");

                if (log) {
                    try {
                        logger.createLog(expressServer.conGlobal, logger.Sources.APP, acc.label, "Unbanned player " + username);
                    } catch (LoggingException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
    }
}

class PostUnbanIP implements HttpHandler {

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        exchange.getResponseHeaders().put(new HttpString("Access-Control-Allow-Origin"), "*");

        HttpString method = exchange.getRequestMethod();

        if (!method.equalToString("POST")) {
            exchange.setStatusCode(405);
            return;
        }

        exchange.getRequestReceiver().receiveFullBytes(new Receiver.FullBytesCallback() {
            @Override
            public void handle(HttpServerExchange exchange, byte[] message) {
                String body = new String(message);

                JSONObject json = new JSONObject(body); // {"ip": "127.0.0.1"}

                // input json: ip, sessionKey
                String ip = json.getString("ip");

                String authHeader = exchange.getRequestHeaders().get("Authorization").toString();

                account acc = ServerCommons.checkSession(authHeader);

                if (acc == null) {
                    exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
                    exchange.getResponseSender().send("login");
                    return;
                }

                if (!(acc.secLevel <= 2)) {
                    exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
                    exchange.getResponseSender().send("perms");
                    return;
                }

                try {
                    ban.unbanIp(expressServer.pluginGlobal, ip);

                    exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
                    exchange.getResponseSender().send("Success");
                } catch (Exception e) {
                    exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
                    exchange.getResponseSender().send(e.getMessage());
                }

                boolean log = mcadminToolkit.appLogging.getJSONObject("unbanIp").getBoolean("log");

                if (log) {
                    try {
                        logger.createLog(expressServer.conGlobal, logger.Sources.APP, acc.label, "Unbanned ip " + ip);
                    } catch (LoggingException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
    }
}

class PostKick implements HttpHandler {

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        exchange.getResponseHeaders().put(new HttpString("Access-Control-Allow-Origin"), "*");

        HttpString method = exchange.getRequestMethod();

        if (!method.equalToString("POST")) {
            exchange.setStatusCode(405);
            return;
        }

        exchange.getRequestReceiver().receiveFullBytes(new Receiver.FullBytesCallback() {
            @Override
            public void handle(HttpServerExchange exchange, byte[] message) {
                String body = new String(message);

                JSONObject json = new JSONObject(body); // {"username": "IpyZ", "reason": "test123"}

                // input json: username, sessionKey, reason
                String username = json.getString("username");
                String sessionKey = json.getString("sessionKey");
                String reason = json.getString("reason");

                String authHeader = exchange.getRequestHeaders().get("Authorization").toString();

                account acc = ServerCommons.checkSession(authHeader);

                if (acc == null) {
                    exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
                    exchange.getResponseSender().send("login");
                    return;
                }

                if (!(acc.secLevel <= 5)) {
                    exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
                    exchange.getResponseSender().send("perms");
                    return;
                }

                try{
                    //ban.ban(expressServer.pluginGlobal, body, "TEST", Date.from(Instant.now()));
                    kick.kick(expressServer.pluginGlobal, username, reason);
                    exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
                    exchange.getResponseSender().send("Success");
                }catch (Exception e){
                    exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
                    exchange.getResponseSender().send(e.getMessage());
                }

                boolean log = mcadminToolkit.appLogging.getJSONObject("kick").getBoolean("log");

                if (log) {
                    try {
                        logger.createLog(expressServer.conGlobal, logger.Sources.APP, acc.label, "Kicked player " + username);
                    } catch (LoggingException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
    }
}

class PostWhiteOn implements HttpHandler {

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        exchange.getResponseHeaders().put(new HttpString("Access-Control-Allow-Origin"), "*");

        HttpString method = exchange.getRequestMethod();

        if (!method.equalToString("POST")) {
            exchange.setStatusCode(405);
            return;
        }

        exchange.getRequestReceiver().receiveFullBytes(new Receiver.FullBytesCallback() {
            @Override
            public void handle(HttpServerExchange exchange, byte[] message) {
                String authHeader = exchange.getRequestHeaders().get("Authorization").toString();

                account acc = ServerCommons.checkSession(authHeader);

                if (acc == null) {
                    exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
                    exchange.getResponseSender().send("login");
                    return;
                }

                if (!(acc.secLevel <= 5)) {
                    exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
                    exchange.getResponseSender().send("perms");
                    return;
                }

                String output;

                try {
                    output = whitelist.enableWhitelist(expressServer.pluginGlobal);

                    exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
                    exchange.getResponseSender().send(output);
                } catch (Exception e) {
                    exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
                    exchange.getResponseSender().send(e.getMessage());
                }

                boolean log = mcadminToolkit.appLogging.getJSONObject("whitelistOnOff").getBoolean("log");

                if (log) {
                    try {
                        logger.createLog(expressServer.conGlobal, logger.Sources.APP, acc.label, "Enabled whitelist");
                    } catch (LoggingException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
    }
}

class PostWhiteOff implements HttpHandler {

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        exchange.getResponseHeaders().put(new HttpString("Access-Control-Allow-Origin"), "*");

        HttpString method = exchange.getRequestMethod();

        if (!method.equalToString("POST")) {
            exchange.setStatusCode(405);
            return;
        }

        exchange.getRequestReceiver().receiveFullBytes(new Receiver.FullBytesCallback() {
            @Override
            public void handle(HttpServerExchange exchange, byte[] message) {
                String authHeader = exchange.getRequestHeaders().get("Authorization").toString();

                account acc = ServerCommons.checkSession(authHeader);

                if (acc == null) {
                    exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
                    exchange.getResponseSender().send("login");
                    return;
                }

                if (!(acc.secLevel <= 5)) {
                    exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
                    exchange.getResponseSender().send("perms");
                    return;
                }

                String output;

                try {
                    output = whitelist.disableWhitelist(expressServer.pluginGlobal);

                    exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
                    exchange.getResponseSender().send(output);
                } catch (Exception e) {
                    exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
                    exchange.getResponseSender().send(e.getMessage());
                }

                boolean log = mcadminToolkit.appLogging.getJSONObject("whitelistOnOff").getBoolean("log");

                if (log) {
                    try {
                        logger.createLog(expressServer.conGlobal, logger.Sources.APP, acc.label, "Disabled whitelist");
                    } catch (LoggingException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
    }
}

class PostWhiteAdd implements HttpHandler {

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        exchange.getResponseHeaders().put(new HttpString("Access-Control-Allow-Origin"), "*");

        HttpString method = exchange.getRequestMethod();

        if (!method.equalToString("POST")) {
            exchange.setStatusCode(405);
            return;
        }

        exchange.getRequestReceiver().receiveFullBytes(new Receiver.FullBytesCallback() {
            @Override
            public void handle(HttpServerExchange exchange, byte[] message) {
                String body = new String(message);

                JSONObject json = new JSONObject(body); // {"username": "IpyZ"}

                // input json: username, sessionKey
                String username = json.getString("username");

                String authHeader = exchange.getRequestHeaders().get("Authorization").toString();

                account acc = ServerCommons.checkSession(authHeader);

                if (acc == null) {
                    exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
                    exchange.getResponseSender().send("login");
                    return;
                }

                if (!(acc.secLevel <= 5)) {
                    exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
                    exchange.getResponseSender().send("perms");
                    return;
                }

                String output;

                try {
                    output = whitelist.addWhitelistPlayer(expressServer.pluginGlobal, username);

                    exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
                    exchange.getResponseSender().send(output);
                } catch (Exception e) {
                    exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
                    exchange.getResponseSender().send(e.getMessage());
                }

                boolean log = mcadminToolkit.appLogging.getJSONObject("whitelistAddRemovePlayer").getBoolean("log");

                if (log) {
                    try {
                        logger.createLog(expressServer.conGlobal, logger.Sources.APP, acc.label, "Added player " + username + " to whitelist");
                    } catch (LoggingException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
    }
}

class PostWhiteRemove implements HttpHandler {

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        exchange.getResponseHeaders().put(new HttpString("Access-Control-Allow-Origin"), "*");

        HttpString method = exchange.getRequestMethod();

        if (!method.equalToString("POST")) {
            exchange.setStatusCode(405);
            return;
        }

        exchange.getRequestReceiver().receiveFullBytes(new Receiver.FullBytesCallback() {
            @Override
            public void handle(HttpServerExchange exchange, byte[] message) {
                String body = new String(message);

                JSONObject json = new JSONObject(body); // {"username": "IpyZ"}

                // input json: username, sessionKey
                String username = json.getString("username");

                String authHeader = exchange.getRequestHeaders().get("Authorization").toString();

                account acc = ServerCommons.checkSession(authHeader);

                if (acc == null) {
                    exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
                    exchange.getResponseSender().send("login");
                    return;
                }

                if (!(acc.secLevel <= 5)) {
                    exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
                    exchange.getResponseSender().send("perms");
                    return;
                }

                String output;

                try {
                    output = whitelist.removeWhitelistPlayer(expressServer.pluginGlobal, username);

                    exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
                    exchange.getResponseSender().send(output);
                } catch (Exception e) {
                    exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
                    exchange.getResponseSender().send(e.getMessage());
                }

                boolean log = mcadminToolkit.appLogging.getJSONObject("whitelistAddRemovePlayer").getBoolean("log");

                if (log) {
                    try {
                        logger.createLog(expressServer.conGlobal, logger.Sources.APP, acc.label, "Removed player " + username + " from whitelist");
                    } catch (LoggingException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
    }
}

class PostStats implements HttpHandler {

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        exchange.getResponseHeaders().put(new HttpString("Access-Control-Allow-Origin"), "*");

        HttpString method = exchange.getRequestMethod();

        if (!method.equalToString("POST")) {
            exchange.setStatusCode(405);
            return;
        }

        exchange.getRequestReceiver().receiveFullBytes(new Receiver.FullBytesCallback() {
            @Override
            public void handle(HttpServerExchange exchange, byte[] message) {
                String authHeader = exchange.getRequestHeaders().get("Authorization").toString();

                account acc = ServerCommons.checkSession(authHeader);

                if (acc == null /*&& !extendSession(body)*/) {
                    exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
                    exchange.getResponseSender().send("login");
                    return;
                }

                JSONObject obj = new JSONObject();
                obj.put ("cpuLoad", serverStats.cpuUsage(expressServer.pluginGlobal));
                obj.put ("playersOnline", serverStats.playersOnline(expressServer.pluginGlobal));
                obj.put ("ramUsage", serverStats.ramUsage(expressServer.pluginGlobal));

                String[] logs;
                try {
                    logs = logger.getLast10Logs(expressServer.conGlobal);
                } catch (LoggingException e) {
                    throw new RuntimeException(e);
                }

                JSONArray arr = new JSONArray();

                for (int i = 0; i < logs.length; i++) {
                    arr.put(logs[i]);
                }

                obj.put ("logs", arr);

                exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
                exchange.getResponseSender().send(obj.toString());
            }
        });
    }
}

class PostLogs implements HttpHandler {

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        exchange.getResponseHeaders().put(new HttpString("Access-Control-Allow-Origin"), "*");

        HttpString method = exchange.getRequestMethod();

        if (!method.equalToString("POST")) {
            exchange.setStatusCode(405);
            return;
        }

        exchange.getRequestReceiver().receiveFullBytes(new Receiver.FullBytesCallback() {
            @Override
            public void handle(HttpServerExchange exchange, byte[] message) {
                String authHeader = exchange.getRequestHeaders().get("Authorization").toString();

                account acc = ServerCommons.checkSession(authHeader);

                if (acc == null /*&& !extendSession(body)*/) {
                    exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
                    exchange.getResponseSender().send("login");
                    return;
                }

                String[] logs;

                try {
                    logs = logger.getAllLogs(expressServer.conGlobal);
                } catch (LoggingException e) {
                    throw new RuntimeException(e);
                }

                String solidLogs = "";

                for (int i = 0; i < logs.length; i++) {
                    solidLogs += logs[i] + "\n";
                }

                exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
                exchange.getResponseSender().send(solidLogs);
            }
        });
    }
}

class PostLogin implements HttpHandler {

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        exchange.getResponseHeaders().put(new HttpString("Access-Control-Allow-Origin"), "*");

        HttpString method = exchange.getRequestMethod();

        if (!method.equalToString("POST")) {
            exchange.setStatusCode(405);
            return;
        }

        exchange.getRequestReceiver().receiveFullBytes(new Receiver.FullBytesCallback() {
            @Override
            public void handle(HttpServerExchange exchange, byte[] message) {
                String authHeader = exchange.getRequestHeaders().get("Authorization").toString();

                List<playerInfo> onlinePlayers = new ArrayList<playerInfo> ();

                Server server = expressServer.pluginGlobal.getServer();

                BufferedImage img = null;

                String b64Img = null;

                File iconFile = new File ("server-icon.png");
                if (iconFile.exists() && !iconFile.isDirectory()) {
                    try {
                        img = ImageIO.read(iconFile);

                        ByteArrayOutputStream os = new ByteArrayOutputStream();
                        ImageIO.write(img, "png", os);

                        b64Img = Base64.getEncoder().encodeToString(os.toByteArray());
                    } catch (IOException e) {
                        b64Img = "none";
                    }
                } else {
                    b64Img = "none";
                }

                onlinePlayers.addAll(Arrays.asList(playerslist.getPlayers(expressServer.pluginGlobal)));
                //offlinePlayers.addAll(Arrays.asList(offlineplayerslist.getOfflinePlayers(expressServer.pluginGlobal)));

                JSONObject obj = new JSONObject();
                obj.put ("players", onlinePlayers.size() + "/" + server.getMaxPlayers());
                obj.put ("serverType", "bukkit");
                obj.put ("icon", b64Img);

                try {
                    // todo login hasło w kurwa logowaniu, japierodle XD
                    String generatedSessionKey = jwtHandler.generateToken(expressServer.conGlobal, );
                    obj.put ("sessionKey", generatedSessionKey);

                    exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
                    exchange.getResponseSender().send(obj.toString());
                } catch (CreateAccountException e) {
                    exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
                    exchange.getResponseSender().send(e.getMessage());
                }
            }
        });
    }
}

class PostAuthKey implements HttpHandler {

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        exchange.getResponseHeaders().put(new HttpString("Access-Control-Allow-Origin"), "*");

        HttpString method = exchange.getRequestMethod();

        if (!method.equalToString("POST")) {
            exchange.setStatusCode(405);
            return;
        }

        exchange.getRequestReceiver().receiveFullBytes(new Receiver.FullBytesCallback() {
            @Override
            public void handle(HttpServerExchange exchange, byte[] message) {
                String body = new String(message);

                if (body.equals(createAccount.accessCode) && !createAccount.actualAuthKey.equals("")) {
                    exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
                    exchange.getResponseSender().send(createAccount.actualAuthKey);
                }
            }
        });
    }
}