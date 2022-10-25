package org.mcadminToolkit.auth;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.security.crypto.bcrypt.*;

public class session {
    public String userName;
    public String sessionKey;
    public Date expirationDate;

    public session (String userName, String sessionKey, Date expirationDate) {
        this.userName = userName;
        this.sessionKey = BCrypt.hashpw(sessionKey, BCrypt.gensalt());
        this.expirationDate = expirationDate;
    }

    public static List<session> activeSessions = new ArrayList<session>();

    public static boolean isSessionActive (int sessionIndex) {
        if (activeSessions.get(sessionIndex).expirationDate.getTime() < new Date().getTime()) {
            activeSessions.remove(sessionIndex);
            return false;
        }
        return true;
    }

    public static boolean validateSession (int sessionIndex, String sessionKey) {
        if (BCrypt.checkpw(sessionKey, activeSessions.get(sessionIndex).sessionKey)) return true;
        return false;
    }

    public static int getSessionIndex (String name) {
        session[] sessions = activeSessions.toArray(new session[0]);

        for (int i = 0; i < sessions.length; i++) {
            if (sessions[i].userName.equals(name)) return i;
        }

        return -1;
    }
}
