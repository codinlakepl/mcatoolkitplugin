package org.mcadminToolkit.auth;

public class NoSessionException extends Exception {
    public NoSessionException () {}
    public NoSessionException (String message) {
        super(message);
    }
}