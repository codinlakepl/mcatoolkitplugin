package org.mcadminToolkit.sqlHandler;

import org.mcadminToolkit.auth.account;
import org.springframework.security.crypto.bcrypt.BCrypt;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class authKeyChecker {

    public static account checkAuthKey (Connection con, String uuid) {
        Statement statement;

        ResultSet results;

        try {
            statement = con.createStatement();
            statement.setQueryTimeout(30);
            results = statement.executeQuery("SELECT * FROM authkeys");
            do {
                try {
                    if (BCrypt.checkpw(uuid, results.getString("authKey"))) {
                        return new account(results.getString("authKey"), results.getInt("secLvl"));
                    }
                } catch (Exception e) {
                    break;
                }
            } while (results.next());

            if (results.getFetchSize() < 1) return null;

            return new account(results.getString("authKey"), results.getInt("secLvl"));
        } catch (SQLException e) {
            return null;
        }
    }
}
