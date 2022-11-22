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

import javax.net.ssl.SSLContext;
import javax.net.ssl.X509ExtendedKeyManager;
import javax.net.ssl.X509ExtendedTrustManager;
import java.io.InputStream;
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

import org.bukkit.Server;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.CachedServerIcon;
import org.json.JSONArray;
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

import static org.mcadminToolkit.express.utils.middleware.Middleware.cors;

public class expressServer {
    public static JavaPlugin pluginGlobal;
    public static Connection conGlobal;

    public static void initializeServer(JavaPlugin plugin, Connection con) {

        pluginGlobal = plugin;
        conGlobal = con;
        //Path cert = new File("cert.pem").toPath();
        //Path key = new File("key.pem").toPath();

        /*ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        InputStream cert = classloader.getResourceAsStream("rootCA.crt");
        InputStream key = classloader.getResourceAsStream("rootCA.key");*/



        X509ExtendedKeyManager keyManager = PemUtils.loadIdentityMaterial(FileSystems.getDefault().getPath("rootCA.crt"), FileSystems.getDefault().getPath("rootCA.key"));

        X509ExtendedTrustManager trustManager = PemUtils.loadTrustMaterial(FileSystems.getDefault().getPath("rootCA.crt"));

        SSLFactory sslFactory = SSLFactory.builder()
                .withIdentityMaterial(keyManager)
                .withTrustMaterial(trustManager)
                .build();

        SSLContext sslContext = sslFactory.getSslContext();

        Express app = new Express(new HttpsConfigurator(sslContext));
        app.bind(new Bindings());
        app.use(cors ());
        app.listen(2137);
        sqlStructureConstructor.checkStructure(con);
        System.out.println("All done");
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

    @DynExpress() // Default is context="/" and method=RequestMethod.GET
    public void getHuj(Request req, Response res) {
        res.send("MCAdmin Toolkit API v0.1");
    }

    @DynExpress(context = "/WHITELIST", method = RequestMethod.POST) // Both defined
    public void getWHITELIST(Request req, Response res) {
        Scanner inputBody = new Scanner(req.getBody()).useDelimiter("\\A");
        String body = inputBody.hasNext() ? inputBody.next() : "";

        int secLvl = checkSession(body);

        if (secLvl == 0) {
            res.send("login");
            return;
        }

        res.send(Arrays.toString(whitelist.getWhiteList(expressServer.pluginGlobal)));
    }

    @DynExpress(context = "/PLAYERS", method = RequestMethod.POST) // Both defined
    public void getPLAYERS(Request req, Response res) {
        Scanner inputBody = new Scanner(req.getBody()).useDelimiter("\\A");
        String body = inputBody.hasNext() ? inputBody.next() : "";

        int secLvl = checkSession(body);

        if (secLvl == 0) {
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
            onlineArray.put(info.name);
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

        int secLvl = checkSession(body);

        if (secLvl == 0) {
            res.send("login");
            return;
        }

        List<String> bans = new ArrayList<String>();
        bans.addAll(Arrays.asList(banlist.playerBanList(expressServer.pluginGlobal)));
        bans.addAll(Arrays.asList(banlist.ipBanList(expressServer.pluginGlobal)));

        res.send (Arrays.toString(bans.toArray()));
    }

    @DynExpress(context = "/BAN", method = RequestMethod.POST) // Both defined
    public void getBAN(Request req, Response res) {
        /*res.send("Accepts: BAN <username> \n" +
                "Bans specific player");*/

        Scanner inputBody = new Scanner(req.getBody()).useDelimiter("\\A");
        String body = inputBody.hasNext() ? inputBody.next() : "";

        JSONObject json = new JSONObject(body); // {"username": "IpyZ", "reason": "test123", "hours": 2 "sessionKey": "test"}

        String username = json.getString("username");
        String sessionKey = json.getString("sessionKey");
        String reason = json.getString("reason");
        int hours = json.getInt("hours");

        int secLvl = checkSession(sessionKey);

        if (secLvl == 0) {
            res.send("login");
            return;
        }

        if (!(secLvl <= 4)) {
            res.send("perms");
            return;
        }

        try {
            ban.ban(expressServer.pluginGlobal, username, reason, Date.from(new Date().toInstant().plus(Duration.ofHours(hours))));

            res.send("Done");
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

        String ip = json.getString("ip");
        String sessionKey = json.getString("sessionKey");

        int secLvl = checkSession(sessionKey);

        if (secLvl == 0) {
            res.send("login");
            return;
        }

        if (!(secLvl <= 2)) {
            res.send("perms");
            return;
        }


        try {
            ban.banIp(expressServer.pluginGlobal, UUID.fromString(ip));

            res.send("Done");
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

        String username = json.getString("username");
        String sessionKey = json.getString("sessionKey");

        int secLvl = checkSession(sessionKey);

        if (secLvl == 0) {
            res.send("login");
            return;
        }

        if (!(secLvl <= 4)) {
            res.send("perms");
            return;
        }

        try {
            ban.unban(expressServer.pluginGlobal, username);

            res.send("Done");
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

        String username = json.getString("username");
        String sessionKey = json.getString("sessionKey");
        String reason = json.getString("reason");

        int secLvl = checkSession(sessionKey);

        if (secLvl == 0) {
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
            res.send(username + " Succes!");
        }catch (Exception e){
            res.send(e.toString());
        }
    }

    @DynExpress(context = "/WHITEADD", method = RequestMethod.POST) // Both defined
    public void getWHITEADD(Request req, Response res) {
        /*res.send("Accepts: WHITEADD <username> \n" +
                "Adds specific player to whitelist");*/

        Scanner inputBody = new Scanner(req.getBody()).useDelimiter("\\A");
        String body = inputBody.hasNext() ? inputBody.next() : "";

        JSONObject json = new JSONObject(body); // {"username": "IpyZ", "sessionKey": "test"}

        String username = json.getString("username");
        String sessionKey = json.getString("sessionKey");

        int secLvl = checkSession(sessionKey);

        if (secLvl == 0) {
            res.send("login");
            return;
        }

        if (!(secLvl <= 5)) {
            res.send("perms");
            return;
        }

        try {
            whitelist.addWhitelistPlayer(expressServer.pluginGlobal, username);

            res.send("Done");
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

        if (secLvl == 0) {
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

        res.send(serverStats.cpuUsage(expressServer.pluginGlobal) + serverStats.playersOnline(expressServer.pluginGlobal) + serverStats.ramUsage(expressServer.pluginGlobal));
    }

    @DynExpress(context = "/ISWORKING") // Both defined
    public void getISWORKING(Request req, Response res) {
        res.send("Returns info that server is working or not");
    }

    @DynExpress(context = "/LOGIN", method = RequestMethod.POST)
    public void getLOGIN (Request req, Response res) {
        Scanner inputBody = new Scanner(req.getBody()).useDelimiter("\\A");
        String body = inputBody.hasNext() ? inputBody.next() : "";

        List<playerInfo> onlinePlayers = new ArrayList<playerInfo> ();

        List<playerInfo> offlinePlayers = new ArrayList<playerInfo>();

        Server server = expressServer.pluginGlobal.getServer();

        CachedServerIcon icon = server.getServerIcon();

        String iconString = icon.toString();

        onlinePlayers.addAll(Arrays.asList(playerslist.getPlayers(expressServer.pluginGlobal)));
        offlinePlayers.addAll(Arrays.asList(offlineplayerslist.getOfflinePlayers(expressServer.pluginGlobal)));

        JSONObject obj = new JSONObject();
        obj.put ("players", onlinePlayers.size() + "/" + offlinePlayers.size());
        obj.put ("serverType", "bukkit");
        obj.put ("icon", iconString);

        try {
            String generatedSessionKey = session.createSession(body);
            obj.put ("sessionKey", generatedSessionKey);

            res.send(obj.toString());
        } catch (CreateSessionException e) {
            res.send("error");
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