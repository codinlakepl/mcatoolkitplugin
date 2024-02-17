package org.mcadminToolkit.sqlHandler;

public class OldPasswordDoesntMatch extends Exception{
    public OldPasswordDoesntMatch() {
    }

    public OldPasswordDoesntMatch(String message) {
        super(message);
    }
}
