package org.mcadminToolkit.sqlHandler;

public class RequirePasswordChangeException extends Exception {
    public RequirePasswordChangeException() {
    }

    public RequirePasswordChangeException(String message) {
        super(message);
    }
}
