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

            statement.executeUpdate("create table\n" +
                    "  `authkeys` (\n" +
                    "    `id` integer not null primary key autoincrement,\n" +
                    "    `authKey` TEXT null,\n" +
                    "    `secLvl` INT null,\n" +
                    "    `created_at` datetime not null default CURRENT_TIMESTAMP\n" +
                    "  )");
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            System.err.println("SQLITE doesn't work, try manually deleting db file");
            System.exit(1);
        }
    }

    public static void checkStructure (Connection con) {
        Statement statement;
        try {
            statement = con.createStatement();
            statement.setQueryTimeout(30);

            statement.executeQuery("SELECT * FROM authKeys");
        } catch (SQLException e) {
            createStructure(con);
        }
    }
}
