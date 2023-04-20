package org.mcadminToolkit.sqlHandler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class authKeyLabelChecker {

    public static boolean checkIfLabelExists (Connection con, String label) {
        PreparedStatement statement;

        try {
            statement = con.prepareStatement("SELECT * FROM authkeys WHERE label = ?");
            statement.setString(1, label);
            statement.setQueryTimeout(30);

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return true;
            }
        } catch (SQLException e) {
            return false;
        }

        return false;
    }
}
