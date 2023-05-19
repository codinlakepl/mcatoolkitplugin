package org.mcadminToolkit.sqlHandler;

import java.sql.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class logger {
    public enum Sources {
        APP, MINECRAFT, CONSOLE, SYSTEM
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
            throw new LoggingException(e.getMessage());
        }
    }

    public static String[] getLast10Logs (Connection con) throws LoggingException {
        Statement statement;

        try {
            statement = con.createStatement();
            statement.setQueryTimeout(30);

            ResultSet rs = statement.executeQuery("SELECT * FROM logs ORDER BY issueTime DESC LIMIT 10");

            List<String> logs = new ArrayList<>();

            while (rs.next()) {
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                //Date date = rs.getDate("issueTime");

                Date date = df.parse(rs.getString("issueTime"));

                String issueTime = df.format(date);
                String source = rs.getString("source");
                String issuer = rs.getString("issuer");
                String message = rs.getString("message");

                String log = issueTime + " [" + source + "/" + issuer + "]: " + message;

                logs.add(log);
            }

            return logs.toArray(new String[0]);
        } catch (SQLException e) {
            throw new LoggingException(e.getMessage());
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}
