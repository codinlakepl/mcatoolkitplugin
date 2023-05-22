package org.mcadminToolkit.sqlHandler;

import org.springframework.security.crypto.bcrypt.BCrypt;

import java.sql.*;
import java.util.UUID;

public class authKeyRegistration {

    public static String registerNewAuthKey (Connection con, int secLvl, String label) throws AuthKeyRegistrationException{
        String uuid = UUID.randomUUID().toString();

        PreparedStatement statement;

        if (authKeyLabelChecker.checkIfLabelExists(con, label)) {
            throw new AuthKeyRegistrationException();
        }

        try {
            statement = con.prepareStatement("INSERT INTO authkeys(authKey, secLvl, label) VALUES (?, ?, ?)");
            statement.setString(1, BCrypt.hashpw(uuid, BCrypt.gensalt()));
            statement.setInt(2, secLvl);
            statement.setString(3, label);
            statement.setQueryTimeout(30);

            statement.executeUpdate();
        } catch (SQLException e) {
            throw new AuthKeyRegistrationException();
        }

        return uuid;
    }
}
