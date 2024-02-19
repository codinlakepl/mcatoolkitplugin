package org.mcadminToolkit.auth;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.mcadminToolkit.sqlHandler.authKeyChecker;
import org.mcadminToolkit.sqlHandler.sqlConnector;
import org.springframework.security.crypto.bcrypt.*;

public class session {
    public String authKey;
    public String sessionKey;
    public Date expirationDate;
    public int secLevel;
    public String label;

    public session (String authKey, String sessionKey, String label, Date expirationDate, int secLevel) {
        this.authKey = authKey;
        this.sessionKey = BCrypt.hashpw(sessionKey, BCrypt.gensalt());
        this.expirationDate = expirationDate;
        this.secLevel = secLevel;
        this.label = label;
    }

    public static List<session> activeSessions = new ArrayList<session>();

    public static int isSessionActive (int sessionIndex) throws SessionExpirationException {

        if (activeSessions.get(sessionIndex).expirationDate.getTime() < new Date().getTime()) {
            activeSessions.remove(sessionIndex);
            throw new SessionExpirationException();
        }

        int secLvl = activeSessions.get(sessionIndex).secLevel;

        return secLvl;
    }

    public static String getSessionLabel (int sessionIndex) throws SessionExpirationException {
        if (activeSessions.get(sessionIndex).expirationDate.getTime() < new Date().getTime()) {
            activeSessions.remove(sessionIndex);
            throw new SessionExpirationException();
        }

        String label = activeSessions.get (sessionIndex).label;

        return label;
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

    public static String createSession (String authKey) throws CreateAccountException {

        account acc = authKeyChecker.checkAuthKey(sqlConnector.connection, authKey);

        if (acc != null/*authKey.equals("DUPA")*/) {
            String sessionKey = UUID.randomUUID().toString();
            session newSession = new session(authKey, sessionKey, acc.label, Date.from(new Date().toInstant().plus(Duration.ofHours(1))), acc.secLevel);
            activeSessions.add(newSession);
            return sessionKey;
        }

        throw new CreateAccountException();
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


