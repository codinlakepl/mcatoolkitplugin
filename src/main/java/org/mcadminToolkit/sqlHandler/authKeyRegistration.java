package org.mcadminToolkit.sqlHandler;

import org.springframework.security.crypto.bcrypt.BCrypt;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

public class authKeyRegistration {

    public static String registerNewAuthKey (Connection con, int secLvl) throws AuthKeyRegistrationException{
        String uuid = UUID.randomUUID().toString();

        Statement statement;

        try {
            statement = con.createStatement();
            statement.setQueryTimeout(30);

            statement.executeUpdate("INSERT INTO authkeys(authKey, secLvl) VALUES (\"" + BCrypt.hashpw(uuid, BCrypt.gensalt()) + "\", " + secLvl + ")");
        } catch (SQLException e) {
            throw new AuthKeyRegistrationException();
        }

        return uuid;
    }
}
