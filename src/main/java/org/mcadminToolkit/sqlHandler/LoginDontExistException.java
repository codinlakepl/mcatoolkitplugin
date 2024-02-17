package org.mcadminToolkit.sqlHandler;

public class LoginDontExistException extends Exception {
    public LoginDontExistException() {}
    public LoginDontExistException(String message) {
        super(message);
    }
}
