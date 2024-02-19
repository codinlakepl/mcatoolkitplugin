package org.mcadminToolkit.auth;

public class account {
    public int secLevel;
    public String label;

    public account (int secLevel, String login) {
        this.secLevel = secLevel;
        this.label = login;
    }
}
