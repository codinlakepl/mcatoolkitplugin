package org.mcadminToolkit.sqlHandler;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.io.File;

public class sqlConnector {

    public static Connection connection;

    static String staticPath = "./plugins/MCAdmin-Toolkit-Connector/";

    public static Connection connect (File dbFile) {
        String url = "jdbc:sqlite:" + dbFile.getPath();

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
