package org.mcadminToolkit.sqlHandler;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class sqlConnector {

    static String staticPath = "./";

    public static Connection connect (String filename) {
        String url = "jdbc:sqlite:" + staticPath + filename;

        Connection con;

        try {
            con = DriverManager.getConnection(url);

            if (con != null) return con;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return null;
    }
}
