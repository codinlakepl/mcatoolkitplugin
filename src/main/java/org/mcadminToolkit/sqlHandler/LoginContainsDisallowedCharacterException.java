package org.mcadminToolkit.sqlHandler;

public class LoginContainsDisallowedCharacterException extends Exception {
    public LoginContainsDisallowedCharacterException() {
    }

    public LoginContainsDisallowedCharacterException(String message) {
        super(message);
    }
}
