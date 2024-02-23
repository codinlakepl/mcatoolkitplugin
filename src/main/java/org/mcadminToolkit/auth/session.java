package org.mcadminToolkit.auth;

public class session {
    public String refreshKey;
    public String jwtToken;

    public session(String refreshKey, String jwtToken) {
        this.refreshKey = refreshKey;
        this.jwtToken = jwtToken;
    }
}
