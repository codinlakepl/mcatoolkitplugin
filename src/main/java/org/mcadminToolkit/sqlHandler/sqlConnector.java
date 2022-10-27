package org.mcadminToolkit.sqlHandler;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class sqlConnector {

    public static Connection connection;

    static String staticPath = "./";

    public static Connection connect (String filename) {
        String url = "jdbc:sqlite:" + staticPath + filename;

        Connection con;

        try {
            con = DriverManager.getConnection(url);

            if (con != null) {
                connection = con;
                return con;
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            System.err.println("SQLITE doesn't work, try manually deleting db file");
            System.exit(1);
        }

        return null;
    }
}
