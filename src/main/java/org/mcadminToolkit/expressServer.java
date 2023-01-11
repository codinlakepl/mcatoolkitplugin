package org.mcadminToolkit;

import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpsConfigurator;
import nl.altindag.ssl.SSLFactory;
import nl.altindag.ssl.util.IOUtils;
import nl.altindag.ssl.util.PemUtils;
import express.DynExpress;
import express.Express;
import express.http.RequestMethod;
import express.http.request.Request;
import express.http.response.Response;

import javax.imageio.ImageIO;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509ExtendedKeyManager;
import javax.net.ssl.X509ExtendedTrustManager;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.security.Security;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

import okhttp3.*;
import org.bouncycastle.mime.encoding.Base64OutputStream;
import org.bouncycastle.util.encoders.Base64;
import org.bouncycastle.util.encoders.Base64Encoder;
import org.bukkit.Server;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.CachedServerIcon;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mcadminToolkit.auth.CreateSessionException;
import org.mcadminToolkit.auth.NoSessionException;
import org.mcadminToolkit.auth.SessionExpirationException;
import org.mcadminToolkit.auth.session;
import org.mcadminToolkit.banlist.banlist;
import org.mcadminToolkit.playermanagement.kick;
import org.mcadminToolkit.playerslist.offlineplayerslist;
import org.mcadminToolkit.playerslist.playerInfo;
import org.mcadminToolkit.playerslist.playerslist;
import org.mcadminToolkit.serverStats.serverStats;
import org.mcadminToolkit.sqlHandler.sqlConnector;
import org.mcadminToolkit.sqlHandler.sqlStructureConstructor;

import org.mcadminToolkit.playermanagement.ban;
import org.mcadminToolkit.whitelist.whitelist;

import org.mcadminToolkit.sqlHandler.*;
import sun.misc.BASE64Encoder;

import static org.mcadminToolkit.express.utils.middleware.Middleware.cors;

public class expressServer {
    public static JavaPlugin pluginGlobal;
    public static Connection conGlobal;

    public static OkHttpClient client = null;

    public static String serverAddress;
    public static int serverPort = 0;

    public static String baseUrl = "http://127.0.0.1:3000";

    public static void initializeServer(JavaPlugin plugin, Connection con, int port, String address, String consoleEmail, String consolePassword) {

        pluginGlobal = plugin;
        conGlobal = con;
        serverAddress = address;
        serverPort = port;

        // x4wSkjCq29hwFSJ1PWqdDqGCcsHsyy

        if (!consoleEmail.equals("") || !consolePassword.equals("")) {
            client = new OkHttpClient.Builder()
                    .addInterceptor(new BasicAuthInterceptor(consoleEmail, consolePassword))
                    .build();
        }
        //Path cert = new File("cert.pem").toPath();
        //Path key = new File("key.pem").toPath();

        /*ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        InputStream cert = classloader.getResourceAsStream("rootCA.crt");
        InputStream key = classloader.getResourceAsStream("rootCA.key");*/



        X509ExtendedKeyManager keyManager = PemUtils.loadIdentityMaterial(FileSystems.getDefault().getPath("./plugins/MCAdmin-Toolkit-Connector/rootCA.crt"), FileSystems.getDefault().getPath("./plugins/MCAdmin-Toolkit-Connector/rootCA.key"));

        X509ExtendedTrustManager trustManager = PemUtils.loadTrustMaterial(FileSystems.getDefault().getPath("./plugins/MCAdmin-Toolkit-Connector/rootCA.crt"));

        SSLFactory sslFactory = SSLFactory.builder()
                .withIdentityMaterial(keyManager)
                .withTrustMaterial(trustManager)
                .build();

        SSLContext sslContext = sslFactory.getSslContext();

        Express app = new Express(new HttpsConfigurator(sslContext));
        app.bind(new Bindings());
        app.use(cors ());
        app.listen(port);

        Timer timer = new Timer();

        timer.schedule(new RegenerateAuthkeysForConsole(), 86400000, 86400000);
    }

    public static void generateAuthkeysForConsole () throws IOException, JSONException {

        if (client == null) return;

        okhttp3.Request connectionsCountRequest = new okhttp3.Request.Builder()
                .url (baseUrl + "/getConnectionsCount")
                .build();

        int count;

        try (okhttp3.Response response = client.newCall(connectionsCountRequest).execute ()) {
            JSONObject jsonObject = new JSONObject(response.body().string());
            if (jsonObject.has ("error")) {
                throw new IOException();
            }

            count = jsonObject.getInt("count");
        }

        JSONArray authkeys = new JSONArray();

        for (int i = 0; i < count; i++) {
            authkeys.put(UUID.randomUUID().toString());
        }

        JSONObject jsonObject = new JSONObject();
        jsonObject.put ("address", serverAddress);
        jsonObject.put("port", serverPort);
        jsonObject.put("authkeys", authkeys);

        MediaType JSON = MediaType.get ("application/json; charset=utf-8");

        RequestBody body = RequestBody.create(jsonObject.toString(), JSON);

        okhttp3.Request setNewAuthkeysRequest = new okhttp3.Request.Builder()
                .url(baseUrl + "/setNewAuthkeys")
                .post(body)
                .build();

        try (okhttp3.Response response = client.newCall(setNewAuthkeysRequest).execute ()) {
            if (new JSONObject(response.body().string()).has("error")) {
                throw new IOException();
            }
        }
    }
}

class RegenerateAuthkeysForConsole extends TimerTask {
    public void run () {
        try {
            expressServer.generateAuthkeysForConsole();
        } catch (IOException | JSONException e) {
            return;
        }
    }
}

class BasicAuthInterceptor implements Interceptor {
    String credentials;

    public BasicAuthInterceptor (String user, String password) {
        this.credentials = Credentials.basic(user, password);
    }

    @Override
    public okhttp3.Response intercept (Chain chain) throws IOException {
        okhttp3.Request request = chain.request();

        okhttp3.Request authenticatedRequest = request.newBuilder()
                .header("Authorization", credentials).build();
        return chain.proceed(authenticatedRequest);
    }
}

class Bindings {

    int checkSession (String sessionKey) {

        int secLvl;

        try {
            secLvl = session.isSessionActive(session.getSessionIndex(sessionKey));
        } catch (NoSessionException | SessionExpirationException e) {
            return 0;
        }

        return secLvl;
    }

    boolean extendSession (String sessionKey) {
        try {
            session.extendSession(session.getSessionIndex(sessionKey));
        } catch (NoSessionException e) {
            return false;
        }

        return true;
    }

    void sendLog (String log, boolean sendPush) {

        if (expressServer.client == null) {
            return;
        }

        MediaType JSON = MediaType.get ("application/json; charset=utf-8");

        JSONObject jsonObject = new JSONObject();

        jsonObject.put("user", "APP");
        jsonObject.put("log", log);
        jsonObject.put("sendPush", sendPush);

        RequestBody body = RequestBody.create(jsonObject.toString(), JSON);
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url (expressServer.baseUrl + "/addLog")
                .post(body)
                .build();

        try {
            expressServer.client.newCall(request).execute();
        } catch (IOException e) {
            return;
        }
    }

    @DynExpress() // Default is context="/" and method=RequestMethod.GET
    public void getHuj(Request req, Response res) {
        res.send("MCAdmin Toolkit API v0.1");
    }

    @DynExpress(context = "/WHITELIST", method = RequestMethod.POST) // Both defined
    public void getWHITELIST(Request req, Response res) {
        Scanner inputBody = new Scanner(req.getBody()).useDelimiter("\\A");
        String body = inputBody.hasNext() ? inputBody.next() : "";

        int secLvl = checkSession(body);

        // request to login if user isn't logged in or, extedning session time failed
        // input text: session key
        if (secLvl == 0 /*&& !extendSession(body)*/) {
            res.send("login");
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

        res.send(obj.toString());
    }

    @DynExpress(context = "/PLAYERS", method = RequestMethod.POST) // Both defined
    public void getPLAYERS(Request req, Response res) {
        Scanner inputBody = new Scanner(req.getBody()).useDelimiter("\\A");
        String body = inputBody.hasNext() ? inputBody.next() : "";

        // input text: session key
        int secLvl = checkSession(body);

        if (secLvl == 0 /*&& !extendSession(body)*/) {
            res.send("login");
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

        //res.send(Arrays.toString(playerNicknames.toArray()));
        res.send(obj.toString());
    }

    @DynExpress(context = "/BANLIST", method = RequestMethod.POST) // Both defined
    public void getBANLIST(Request req, Response res) {
        Scanner inputBody = new Scanner(req.getBody()).useDelimiter("\\A");
        String body = inputBody.hasNext() ? inputBody.next() : "";

        // input text: session key
        int secLvl = checkSession(body);

        if (secLvl == 0 /*&& !extendSession(body)*/) {
            res.send("login");
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

        res.send (json.toString());
    }

    @DynExpress(context = "/BAN", method = RequestMethod.POST) // Both defined
    public void getBAN(Request req, Response res) {
        /*res.send("Accepts: BAN <username> \n" +
                "Bans specific player");*/

        Scanner inputBody = new Scanner(req.getBody()).useDelimiter("\\A");
        String body = inputBody.hasNext() ? inputBody.next() : "";

        JSONObject json = new JSONObject(body); // {"username": "IpyZ", "reason": "test123", "hours": 2 "sessionKey": "test"}

        // input json: username, sessionKey, reason, hours
        String username = json.getString("username");
        String sessionKey = json.getString("sessionKey");
        String reason = json.getString("reason");
        int hours = json.getInt("hours");

        int secLvl = checkSession(sessionKey);

        if (secLvl == 0 && !extendSession(sessionKey)) {
            res.send("login");
            return;
        }

        if (!(secLvl <= 4)) {
            res.send("perms");
            return;
        }

        try {
            ban.ban(expressServer.pluginGlobal, username, reason, Date.from(new Date().toInstant().plus(Duration.ofHours(hours))));

            sendLog("Banned player " + username, false);

            res.send("Success");
        } catch (Exception e) {
            res.send(e.getMessage());
        }
    }

    @DynExpress(context = "/BANIP", method = RequestMethod.POST) // Both defined
    public void getBANIP(Request req, Response res) {
        /*res.send("Accepts: BANIP <ip> \n" +
                "Bans specific ip");*/

        Scanner inputBody = new Scanner(req.getBody()).useDelimiter("\\A");
        String body = inputBody.hasNext() ? inputBody.next() : "";

        JSONObject json = new JSONObject(body); // {"ip": "127.0.0.1", "sessionKey": "test"}

        // input json: username, sessionKey, reason
        String playerName = json.getString("username");
        String sessionKey = json.getString("sessionKey");
        String reason = json.getString("reason");

        int secLvl = checkSession(sessionKey);

        if (secLvl == 0 && !extendSession(sessionKey)) {
            res.send("login");
            return;
        }

        if (!(secLvl <= 2)) {
            res.send("perms");
            return;
        }


        try {
            ban.banIp(expressServer.pluginGlobal, playerName, reason);

            sendLog("Banned player " + playerName, false);

            res.send("Success");
        } catch (Exception e) {
            res.send(e.getMessage());
        }
    }

    @DynExpress(context = "/UNBAN", method = RequestMethod.POST) // Both defined
    public void getUNBAN(Request req, Response res) {
        /*res.send("Accepts: BANIP <ip> \n" +
                "Bans specific ip");*/

        Scanner inputBody = new Scanner(req.getBody()).useDelimiter("\\A");
        String body = inputBody.hasNext() ? inputBody.next() : "";

        JSONObject json = new JSONObject(body); // {"username": "IpyZ", "sessionKey": "test"}

        // input json: username, sessionKey
        String username = json.getString("username");
        String sessionKey = json.getString("sessionKey");

        int secLvl = checkSession(sessionKey);

        if (secLvl == 0 && !extendSession(sessionKey)) {
            res.send("login");
            return;
        }

        if (!(secLvl <= 4)) {
            res.send("perms");
            return;
        }

        try {
            ban.unban(expressServer.pluginGlobal, username);

            sendLog("Unbanned player " + username, false);

            res.send("Success");
        } catch (Exception e) {
            res.send(e.getMessage());
        }
    }

    @DynExpress(context = "/UNBANIP", method = RequestMethod.POST)
    public void getUNBANIP (Request req, Response res) {
        Scanner inputBody = new Scanner(req.getBody()).useDelimiter("\\A");
        String body = inputBody.hasNext() ? inputBody.next() : "";

        JSONObject json = new JSONObject(body); // {"ip": "127.0.0.1", "sessionKey": "test"}

        // input json: ip, sessionKey
        String ip = json.getString("ip");
        String sessionKey = json.getString("sessionKey");

        int secLvl = checkSession(sessionKey);

        if (secLvl == 0 && !extendSession(sessionKey)) {
            res.send("login");
            return;
        }

        if (!(secLvl <= 2)) {
            res.send("perms");
            return;
        }

        try {
            ban.unbanIp(expressServer.pluginGlobal, ip);

            sendLog("Unbanned player " + ip, false);

            res.send("Success");
        } catch (Exception e) {
            res.send(e.getMessage());
        }
    }

    @DynExpress(context = "/KICK", method = RequestMethod.POST) // Both defined
    public void getKICK(Request req, Response res) {
        /*res.send("Accepts: KICK <username> \n" +
                "Kicks specific player from server");*/
        Scanner inputBody = new Scanner(req.getBody()).useDelimiter("\\A");
        String body = inputBody.hasNext() ? inputBody.next() : "";

        JSONObject json = new JSONObject(body); // {"username": "IpyZ", "reason": "test123", "sessionKey": "test"}

        // input json: username, sessionKey, reason
        String username = json.getString("username");
        String sessionKey = json.getString("sessionKey");
        String reason = json.getString("reason");

        int secLvl = checkSession(sessionKey);

        if (secLvl == 0 && !extendSession(sessionKey)) {
            res.send("login");
            return;
        }

        if (!(secLvl <= 5)) {
            res.send("perms");
            return;
        }

        try{
            //ban.ban(expressServer.pluginGlobal, body, "TEST", Date.from(Instant.now()));
            kick.kick(expressServer.pluginGlobal, username, reason);

            sendLog("Kicked player " + username, false);

            res.send("Success");
        }catch (Exception e){
            res.send(e.toString());
        }
    }

    @DynExpress(context = "/WHITEON", method = RequestMethod.POST)
    public void getWHITEON (Request req, Response res) {
        Scanner inputBody = new Scanner(req.getBody()).useDelimiter("\\A");
        String body = inputBody.hasNext() ? inputBody.next() : "";

        int secLvl = checkSession(body);

        if (secLvl == 0 && !extendSession(body)) {
            res.send("login");
            return;
        }

        if (!(secLvl <= 5)) {
            res.send("perms");
            return;
        }

        String output;

        try {
            output = whitelist.enableWhitelist(expressServer.pluginGlobal);

            sendLog("Whitelist enabled", false);

            res.send(output);
        } catch (Exception e) {
            res.send(e.getMessage());
        }
    }

    @DynExpress(context = "/WHITEOFF", method = RequestMethod.POST)
    public void getWHITEOFF (Request req, Response res) {
        Scanner inputBody = new Scanner(req.getBody()).useDelimiter("\\A");
        String body = inputBody.hasNext() ? inputBody.next() : "";

        int secLvl = checkSession(body);

        if (secLvl == 0 && !extendSession(body)) {
            res.send("login");
            return;
        }

        if (!(secLvl <= 5)) {
            res.send("perms");
            return;
        }

        String output;

        try {
            output = whitelist.disableWhitelist(expressServer.pluginGlobal);

            sendLog("Whitelist disabled", false);

            res.send(output);
        } catch (Exception e) {
            res.send(e.getMessage());
        }
    }

    @DynExpress(context = "/WHITEADD", method = RequestMethod.POST) // Both defined
    public void getWHITEADD(Request req, Response res) {
        /*res.send("Accepts: WHITEADD <username> \n" +
                "Adds specific player to whitelist");*/

        Scanner inputBody = new Scanner(req.getBody()).useDelimiter("\\A");
        String body = inputBody.hasNext() ? inputBody.next() : "";

        JSONObject json = new JSONObject(body); // {"username": "IpyZ", "sessionKey": "test"}

        // input json: username, sessionKey
        String username = json.getString("username");
        String sessionKey = json.getString("sessionKey");

        int secLvl = checkSession(sessionKey);

        if (secLvl == 0 && !extendSession(sessionKey)) {
            res.send("login");
            return;
        }

        if (!(secLvl <= 5)) {
            res.send("perms");
            return;
        }

        String output;

        try {
            output = whitelist.addWhitelistPlayer(expressServer.pluginGlobal, username);

            sendLog("Player " + username + " added to whitelist", false);

            res.send(output);
        } catch (Exception e) {
            res.send(e.getMessage());
        }
    }

    @DynExpress(context = "/WHITEREMOVE", method = RequestMethod.POST) // Both defined
    public void getWHITEREMOVE(Request req, Response res) {
        /*res.send("Accepts: WHITEADD <username> \n" +
                "Adds specific player to whitelist");*/

        Scanner inputBody = new Scanner(req.getBody()).useDelimiter("\\A");
        String body = inputBody.hasNext() ? inputBody.next() : "";

        JSONObject json = new JSONObject(body); // {"username": "IpyZ", "sessionKey": "test"}

        // input json: username, sessionKey
        String username = json.getString("username");
        String sessionKey = json.getString("sessionKey");

        int secLvl = checkSession(sessionKey);

        if (secLvl == 0 && !extendSession(sessionKey)) {
            res.send("login");
            return;
        }

        if (!(secLvl <= 5)) {
            res.send("perms");
            return;
        }

        String output;

        try {
            output = whitelist.removeWhitelistPlayer(expressServer.pluginGlobal, username);

            sendLog("Player " + username + " removed from whitelist", false);

            res.send(output);
        } catch (Exception e) {
            res.send(e.getMessage());
        }
    }

    @DynExpress(context = "/STATS", method = RequestMethod.POST) // Both defined
    public void getSTATS(Request req, Response res) {
        //res.send("Returns server stats (CPU, RAM usage ect.)");

        Scanner inputBody = new Scanner(req.getBody()).useDelimiter("\\A");
        String body = inputBody.hasNext() ? inputBody.next() : "";

        int secLvl = checkSession(body);

        if (secLvl == 0 /*&& !extendSession(body)*/) {
            res.send("login");
            return;
        }

        if (!(secLvl <= 3)) {
            res.send("perms");
            return;
        }

        JSONObject obj = new JSONObject();
        obj.put ("cpuLoad", serverStats.cpuUsage(expressServer.pluginGlobal));
        obj.put ("playersOnline", serverStats.playersOnline(expressServer.pluginGlobal));
        obj.put ("ramUsage", serverStats.ramUsage(expressServer.pluginGlobal));

        res.send(obj.toString());
    }

    @DynExpress(context = "/LOGIN", method = RequestMethod.POST)
    public void getLOGIN (Request req, Response res) {
        Scanner inputBody = new Scanner(req.getBody()).useDelimiter("\\A");
        String body = inputBody.hasNext() ? inputBody.next() : "";

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

                BASE64Encoder encoder = new BASE64Encoder();

                b64Img = encoder.encode(os.toByteArray());
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
            String generatedSessionKey = session.createSession(body);
            obj.put ("sessionKey", generatedSessionKey);

            res.send(obj.toString());
        } catch (CreateSessionException e) {
            res.send(e.getMessage());
        }
    }

    @DynExpress(context = "/GETAUTHKEY", method = RequestMethod.POST)
    public void getGETAUTHKEY (Request req, Response res) {
        Scanner inputBody = new Scanner(req.getBody()).useDelimiter("\\A");
        String body = inputBody.hasNext() ? inputBody.next() : "";

        if (body.equals(createAuthKeyCommand.accessCode) && !createAuthKeyCommand.actualAuthKey.equals("")) {
            res.send(createAuthKeyCommand.actualAuthKey);
        }
    }

    /*@DynExpress(context = "/REGISTER", method = RequestMethod.POST)
    public void getREGISTER (Request req, Response res) throws AuthKeyRegistrationException {
        Scanner inputBody = new Scanner(req.getBody()).useDelimiter("\\A");
        String body = inputBody.hasNext() ? inputBody.next() : "";

        JSONObject json = new JSONObject(body);
        int secLvl = json.getInt("secLvl");

        String authKey = authKeyRegistration.registerNewAuthKey(sqlConnector.connection, secLvl);
        res.send(authKey);
    }*/

    @DynExpress(method = RequestMethod.POST) // Only the method is defined, "/" is used as context
    public void postIndex(Request req, Response res) {
        res.send("POST to index");
    }
}