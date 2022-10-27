package org.mcadminToolkit.auth;

public class SessionExpirationException extends Exception {
    public SessionExpirationException () {}
    public SessionExpirationException (String message) {
        super(message);
    }
}