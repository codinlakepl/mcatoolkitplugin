package org.mcadminToolkit.sqlHandler;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class sqlStructureConstructor {

    static void createStructure (Connection con) {
        Statement statement;
        try {
            statement = con.createStatement();
            statement.setQueryTimeout(30);

            statement.executeUpdate("CREATE TABLE accounts (id NOT NULL AI INT, name VARCHAR(20), pass TEXT, secLvl INT, PRIMARY KEY(id))");
            statement.executeUpdate("CREATE TABLE authKeys (id NOT NULL AI INT, authKey TEXT, secLvl INT)");
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    public static void checkStructure (Connection con) {
        Statement statement;

        try {
            statement = con.createStatement();
            statement.setQueryTimeout(30);

            statement.executeQuery("SELECT * FROM accounts");
            statement.executeQuery("SELECT * FROM authKeys");
        } catch (SQLException e) {
            createStructure(con);
        }
    }
}
