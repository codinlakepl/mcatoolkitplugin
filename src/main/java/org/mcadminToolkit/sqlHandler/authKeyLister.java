package org.mcadminToolkit.sqlHandler;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

public class authKeyLister {

    public static String[] listAuthKeys (Connection con) throws AuthKeyListingException {
        Set<String> labels = new HashSet<String>();

        Statement statement;

        try {
            statement = con.createStatement();
            statement.setQueryTimeout(30);

            ResultSet resultSet = statement.executeQuery("SELECT label FROM authkeys");

            while (resultSet.next()) {
                labels.add(resultSet.getString("label"));
            }
        } catch (SQLException e) {
            throw new AuthKeyListingException();
        }

        return labels.toArray(new String[0]);
    }
}
