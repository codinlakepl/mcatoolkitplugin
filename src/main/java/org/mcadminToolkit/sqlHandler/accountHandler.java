package org.mcadminToolkit.sqlHandler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.mcadminToolkit.utils.passwordGenerator.generatePassword;

public class accountHandler {

    public static String createAccount (Connection con, String login, int secLvl) throws CreateAccountException, TooLongLoginException, LoginExistsException {
        PreparedStatement statement;

        if (login.length() > 50) {
            throw new TooLongLoginException();
        }

        try {
            statement = con.prepareStatement("SELECT * FROM accounts WHERE login = ?");
            statement.setString(1, login);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                resultSet.close();
                statement.close();

                throw new LoginExistsException();
            }

            statement.close();

            String password = generatePassword (6);

            statement = con.prepareStatement("INSERT INTO accounts (" +
                    "login," +
                    "secLvl," +
                    "password," +
                    "requireChange)" +
                    " VALUES (?, ?, ?, 1)");

            statement.setString(1, login);
            statement.setInt(2, secLvl);
            statement.setString(3, password);

            statement.executeUpdate();

            return password;

        } catch (SQLException e) {
            throw new CreateAccountException(e.getMessage());
        }
    }
}
