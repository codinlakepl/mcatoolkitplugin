package org.mcadminToolkit;

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

import org.bukkit.plugin.java.JavaPlugin;
import org.mcadminToolkit.auth.CreateSessionException;
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

public class expressServer {
    public static JavaPlugin pluginGlobal;

    public static void initializeServer(JavaPlugin plugin) {

        pluginGlobal = plugin;

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
        app.listen(2137);

        Connection con = sqlConnector.connect("TEST.db");
        sqlStructureConstructor.checkStructure(con);
        System.out.println("All done");
    }
}

class Bindings {

    @DynExpress() // Default is context="/" and method=RequestMethod.GET
    public void getHuj(Request req, Response res) {
        res.send("MCAdmin Toolkit API v0.1");
    }

    @DynExpress(context = "/WHITELIST") // Both defined
    public void getWHITELIST(Request req, Response res) {
        res.send(Arrays.toString(whitelist.getWhiteList(expressServer.pluginGlobal)));
    }

    @DynExpress(context = "/PLAYERS") // Both defined
    public void getPLAYERS(Request req, Response res) {
        List<String> players = new ArrayList<String> ();

        List<playerInfo> infos = new ArrayList<playerInfo>();

        infos.addAll(Arrays.asList(playerslist.getPlayers(expressServer.pluginGlobal)));
        infos.addAll(Arrays.asList(offlineplayerslist.getOfflinePlayers(expressServer.pluginGlobal)));

        List<String> playerNicknames = new ArrayList<String>();

        for (playerInfo info : infos) {
            playerNicknames.add (info.name);
        }

        res.send(Arrays.toString(playerNicknames.toArray()));
    }

    @DynExpress(context = "/BANLIST") // Both defined
    public void getBANLIST(Request req, Response res) {
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

        try {
            ban.ban(expressServer.pluginGlobal, body, "test123", Date.from(new Date().toInstant().plus(Duration.ofHours(2))));

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

        try {
            ban.banIp(expressServer.pluginGlobal, UUID.fromString(body));

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

        try {
            ban.unban(expressServer.pluginGlobal, body);

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
        try{
            //ban.ban(expressServer.pluginGlobal, body, "TEST", Date.from(Instant.now()));
            kick.kick(expressServer.pluginGlobal, body, "abc");
            res.send(body + " Succes!");
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

        try {
            whitelist.addWhitelistPlayer(expressServer.pluginGlobal, body);

            res.send("Done");
        } catch (Exception e) {
            res.send(e.getMessage());
        }
    }

    @DynExpress(context = "/STATS") // Both defined
    public void getSTATS(Request req, Response res) {
        //res.send("Returns server stats (CPU, RAM usage ect.)");

        res.send(serverStats.cpuUsage(expressServer.pluginGlobal) + serverStats.playersOnline(expressServer.pluginGlobal) + serverStats.ramUsage(expressServer.pluginGlobal));
    }

    @DynExpress(context = "/ISWORKING") // Both defined
    public void getISWORKING(Request req, Response res) {
        res.send("Returns info that server is working or not");
    }

    @DynExpress(context = "/REGISTER", method = RequestMethod.POST)
    public void getREGISTER (Request req, Response res) {
        Scanner inputBody = new Scanner(req.getBody()).useDelimiter("\\A");
        String body = inputBody.hasNext() ? inputBody.next() : "";

        try {
            String generatedSessionKey = session.createSession(body);

            res.send(generatedSessionKey);
        } catch (CreateSessionException e) {
            res.send("error");
        }
    }

    @DynExpress(method = RequestMethod.POST) // Only the method is defined, "/" is used as context
    public void postIndex(Request req, Response res) {
        res.send("POST to index");
    }
}