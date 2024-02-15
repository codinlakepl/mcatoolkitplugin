package org.mcadminToolkit.sqlHandler;

public class TooLongLoginException extends Exception {
    public TooLongLoginException() {}
    public TooLongLoginException(String message) {
        super(message);
    }
}
