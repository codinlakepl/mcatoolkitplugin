package org.mcadminToolkit.auth;

public class InvalidSessionException extends Exception {
    public InvalidSessionException () {}
    public InvalidSessionException (String message) {
        super(message);
    }
}