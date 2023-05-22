package org.mcadminToolkit.sqlHandler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class authKeyRemover {

    public static void removeAuthKey (Connection con, String label) throws AuthKeyRemovingException {
        PreparedStatement statement;

        if (!authKeyLabelChecker.checkIfLabelExists(con, label)) {
            throw new AuthKeyRemovingException();
        }

        try {
            statement = con.prepareStatement("DELETE FROM authkeys WHERE label = ?");
            statement.setString(1, label);
            statement.setQueryTimeout(30);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new AuthKeyRemovingException();
        }
    }
}
