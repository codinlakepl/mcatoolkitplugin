package org.mcadminToolkit;

import com.sun.net.httpserver.HttpsConfigurator;
import nl.altindag.ssl.SSLFactory;
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
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.mcadminToolkit.sqlHandler.sqlConnector;
import org.mcadminToolkit.sqlHandler.sqlStructureConstructor;
public class expressServer {

    public static void initializeServer() {

        //Path cert = new File("cert.pem").toPath();
        //Path key = new File("key.pem").toPath();

        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        InputStream cert = classloader.getResourceAsStream("rootCA.crt");
        InputStream key = classloader.getResourceAsStream("rootCA.key");


        X509ExtendedKeyManager keyManager = PemUtils.loadIdentityMaterial(cert, key);

        try {
            cert.close();
            cert = classloader.getResourceAsStream("rootCA.crt");
        } catch (Exception e) {
            System.out.println("Can't close previous stream");
            System.exit(1);
        }

        X509ExtendedTrustManager trustManager = PemUtils.loadTrustMaterial(cert);

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
        res.send("Returns: WHITELIST[\"user1\", \"user2\"] \n Returns whitelisted players array");
    }

    @DynExpress(context = "/PLAYERS") // Both defined
    public void getPLAYERS(Request req, Response res) {
        res.send("Returns: PLAYERS [{\"nick\": \"Diratix\", \"online\": true}, {\"nick\": \"IpyZ\", \"online\":false}] \n " +
                "Returns all players object.");
    }

    @DynExpress(context = "/BANLIST") // Both defined
    public void getBANLIST(Request req, Response res) {
        res.send("[{\"value\": \"IpyZ\", \"ip\": false}, {\"value\": \"192.168.0.1\", \"ip\": true}] \n" +
                "Returns banned players object");
    }

    @DynExpress(context = "/BAN", method = RequestMethod.POST) // Both defined
    public void getBAN(Request req, Response res) {
        res.send("Accepts: BAN <username> \n" +
                "Bans specific player");
    }

    @DynExpress(context = "/BANIP", method = RequestMethod.POST) // Both defined
    public void getBANIP(Request req, Response res) {
        res.send("Accepts: BANIP <ip> \n" +
                "Bans specific ip");
    }

    @DynExpress(context = "/KICK", method = RequestMethod.POST) // Both defined
    public void getKICK(Request req, Response res) {
        res.send("Accepts: KICK <username> \n" +
                "Kicks specific player from server");
    }

    @DynExpress(context = "/WHITEADD", method = RequestMethod.POST) // Both defined
    public void getWHITEADD(Request req, Response res) {
        res.send("Accepts: WHITEADD <username> \n" +
                "Adds specific player to whitelist");
    }

    @DynExpress(context = "/STATS") // Both defined
    public void getSTATS(Request req, Response res) {
        res.send("Returns server stats (CPU, RAM usage ect.)");
    }

    @DynExpress(context = "/ISWORKING") // Both defined
    public void getISWORKING(Request req, Response res) {
        res.send("Returns info that server is working or not");
    }

    @DynExpress(method = RequestMethod.POST) // Only the method is defined, "/" is used as context
    public void postIndex(Request req, Response res) {
        res.send("POST to index");
    }
}