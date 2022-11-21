package org.mcadminToolkit.auth;

public class account {
    public String authKey;
    public int secLevel;

    public account (String authKey, int secLevel) {
        this.authKey = authKey;
        this.secLevel = secLevel;
    }
}
