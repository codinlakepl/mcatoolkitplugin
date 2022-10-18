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
import java.io.File;
import java.nio.file.Path;

public class expressServer {

    public static void main(String[] args) {

        //Path cert = new File("cert.pem").toPath();
        //Path key = new File("key.pem").toPath();

        //X509ExtendedKeyManager keyManager = PemUtils.loadIdentityMaterial(cert, key, "123123".toCharArray());
        //X509ExtendedTrustManager trustManager = PemUtils.loadTrustMaterial(cert);

        //SSLFactory sslFactory = SSLFactory.builder()
        //        .withIdentityMaterial(keyManager)
        //        .withTrustMaterial(trustManager)
        //        .build();

        //SSLContext sslContext = sslFactory.getSslContext();

        Express app = new Express();
        app.bind(new Bindings());
        app.listen(2137);
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

    @DynExpress(context = "/BAN") // Both defined
    public void getBAN(Request req, Response res) {
        res.send("Accepts: BAN <username> \n" +
                "Bans specific player");
    }

    @DynExpress(context = "/BANIP") // Both defined
    public void getBANIP(Request req, Response res) {
        res.send("Accepts: BANIP <ip> \n" +
                "Bans specific ip");
    }

    @DynExpress(context = "/KICK") // Both defined
    public void getKICK(Request req, Response res) {
        res.send("Accepts: KICK <username> \n" +
                "Kicks specific player from server");
    }

    @DynExpress(context = "/WHITEADD") // Both defined
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