package org.mcadminToolkit.sqlHandler;

public class LoginExistsException extends Exception {
    public LoginExistsException() {}
    public LoginExistsException(String message) {
        super(message);
    }
}
