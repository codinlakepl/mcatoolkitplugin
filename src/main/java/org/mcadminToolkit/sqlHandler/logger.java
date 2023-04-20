package org.mcadminToolkit.sqlHandler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class logger {
    public enum Sources {
        APP, MINECRAFT, CONSOLE
    }

    public static void createLog (Connection con, Sources source, String issuer, String message) throws LoggingException {
        PreparedStatement statement;

        try {
            statement = con.prepareStatement("INSERT INTO\n" +
                    "  logs(\n" +
                    "    source,\n" +
                    "    issuer,\n" +
                    "    message\n" +
                    "  ) VALUES (\n" +
                    "    ?,\n" +
                    "    ?,\n" +
                    "    ?\n" +
                    "  )");

            statement.setString(1, source.name());
            statement.setString(2, issuer);
            statement.setString(3, message);

            statement.executeUpdate();
        } catch (SQLException e) {
            throw new LoggingException();
        }
    }
}
