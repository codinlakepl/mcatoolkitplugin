package org.mcadminToolkit.auth;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONObject;
import org.mcadminToolkit.expressServer;
import org.mcadminToolkit.sqlHandler.authKeyChecker;
import org.mcadminToolkit.sqlHandler.sqlConnector;
import org.springframework.security.crypto.bcrypt.*;

public class session {
    public String authKey;
    public String sessionKey;
    public Date expirationDate;
    public int secLevel;

    public session (String authKey, String sessionKey, Date expirationDate, int secLevel) {
        this.authKey = authKey;
        this.sessionKey = BCrypt.hashpw(sessionKey, BCrypt.gensalt());
        this.expirationDate = expirationDate;
        this.secLevel = secLevel;
    }

    public static List<session> activeSessions = new ArrayList<session>();

    public static int isSessionActive (int sessionIndex) throws SessionExpirationException {

        int secLvl = activeSessions.get(sessionIndex).secLevel;

        if (activeSessions.get(sessionIndex).expirationDate.getTime() < new Date().getTime()) {
            activeSessions.remove(sessionIndex);
            throw new SessionExpirationException();
        }

        return secLvl;
    }

    /*public static void validateSession (int sessionIndex, String sessionKey) throws InvalidSessionException {
        if (!BCrypt.checkpw(sessionKey, activeSessions.get(sessionIndex).sessionKey)) throw new InvalidSessionException();
    }*/

    public static int getSessionIndex (String sessionKey) throws NoSessionException {
        session[] sessions = activeSessions.toArray(new session[0]);

        for (int i = 0; i < sessions.length; i++) {
            if (BCrypt.checkpw(sessionKey, sessions[i].sessionKey)) return i;
        }

        throw new NoSessionException();
    }

    public static String createSession (String authKey) throws CreateSessionException {

        account acc = authKeyChecker.checkAuthKey(sqlConnector.connection, authKey);

        if (acc != null/*authKey.equals("DUPA")*/) {
            String sessionKey = UUID.randomUUID().toString();
            session newSession = new session(authKey, sessionKey, Date.from(new Date().toInstant().plus(Duration.ofHours(1))), acc.secLevel);
            activeSessions.add(newSession);
            return sessionKey;
        }

        MediaType JSON = MediaType.get("application/json; charset=utf-8");

        RequestBody body = RequestBody.create("{\"authkey\": \"" + authKey + "\"}", JSON);

        Request request = new Request.Builder()
                .url(expressServer.baseUrl + "/checkAuthkey")
                .post(body)
                .build();

        String jsonText = "";

        try (Response response = expressServer.client.newCall(request).execute()) {
            jsonText = response.body().string();
        } catch (IOException e) {
            throw new CreateSessionException();
        }

        JSONObject jsonObject = new JSONObject(jsonText);

        if (jsonObject.has("error")) {
            throw new CreateSessionException();
        }

        String sessionKey = UUID.randomUUID().toString();
        session newSession = new session(authKey, sessionKey, Date.from(new Date().toInstant().plus(Duration.ofHours(1))), jsonObject.getInt("secLvl"));
        activeSessions.add(newSession);
        return sessionKey;
    }

    public static void extendSession (int sessionIndex) throws NoSessionException {

        session s = null;

        try {
            s = activeSessions.get(sessionIndex);
        } catch (IndexOutOfBoundsException e) {
            throw new NoSessionException ();
        }

        try {
            isSessionActive (sessionIndex);
        } catch (SessionExpirationException e) {
            throw new NoSessionException();
        }

        s.expirationDate = Date.from(new Date().toInstant().plus(Duration.ofHours(1)));
    }
}


