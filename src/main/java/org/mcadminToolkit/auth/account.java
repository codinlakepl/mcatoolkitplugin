package org.mcadminToolkit.auth;

public class account {
    public String authKey;
    public int secLevel;
    public String label;

    public account (String authKey, int secLevel, String label) {
        this.authKey = authKey;
        this.secLevel = secLevel;
        this.label = label;
    }
}
