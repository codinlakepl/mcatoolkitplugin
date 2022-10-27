package org.mcadminToolkit.sqlHandler;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class authKeyChecker {

    public static boolean checkAuthKey (Connection con, String uuid) {
        Statement statement;

        try {
            statement = con.createStatement();
            statement.setQueryTimeout(30);
            ResultSet results = statement.executeQuery("SELECT * FROM authkeys WHERE authKey=\"" + uuid + "\"");

            if (results.getFetchSize() < 1) return false;
        } catch (SQLException e) {
            return false;
        }

        return false;
    }
}
