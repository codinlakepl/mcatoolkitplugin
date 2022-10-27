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

    public session (String authKey, String sessionKey, Date expirationDate) {
        this.authKey = authKey;
        this.sessionKey = BCrypt.hashpw(sessionKey, BCrypt.gensalt());
        this.expirationDate = expirationDate;
    }

    public static List<session> activeSessions = new ArrayList<session>();

    public static void isSessionActive (int sessionIndex) throws SessionExpirationException {
        if (activeSessions.get(sessionIndex).expirationDate.getTime() < new Date().getTime()) {
            activeSessions.remove(sessionIndex);
            throw new SessionExpirationException();
        }
    }

    public static void validateSession (int sessionIndex, String sessionKey) throws InvalidSessionException {
        if (!BCrypt.checkpw(sessionKey, activeSessions.get(sessionIndex).sessionKey)) throw new InvalidSessionException();
    }

    public static int getSessionIndex (String authKey) throws NoSessionException {
        session[] sessions = activeSessions.toArray(new session[0]);

        for (int i = 0; i < sessions.length; i++) {
            if (sessions[i].authKey.equals(authKey)) return i;
        }

        throw new NoSessionException();
    }

    public static String createSession (String authKey) throws CreateSessionException {
        if (authKeyChecker.checkAuthKey(sqlConnector.connection, authKey)) {
            String sessionKey = UUID.randomUUID().toString();
            session newSession = new session(authKey, sessionKey, Date.from(new Date().toInstant().plus(Duration.ofHours(1))));
            activeSessions.add(newSession);
            return sessionKey;
        }

        throw new CreateSessionException();
    }
}


