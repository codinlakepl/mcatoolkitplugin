package org.mcadminToolkit.sqlHandler;

import org.springframework.security.crypto.bcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.mcadminToolkit.utils.passwordGenerator.generatePassword;

public class accountHandler {

    public static String createAcc (Connection con, String login, int secLvl) throws AccountException, TooLongLoginException, LoginExistsException {
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
            String hashedPass = BCrypt.hashpw(password, BCrypt.gensalt());

            statement = con.prepareStatement("INSERT INTO accounts (" +
                    "login," +
                    "secLvl," +
                    "password," +
                    "requireChange)" +
                    " VALUES (?, ?, ?, 1)");

            statement.setString(1, login);
            statement.setInt(2, secLvl);
            statement.setString(3, hashedPass);

            statement.executeUpdate();

            return password;

        } catch (SQLException e) {
            throw new AccountException(e.getMessage());
        }
    }

    // changePass
    // --- Connection con, String login, String oldPass, String newPass
    // --- if old pass doesn't match, throw an exception
    // --- if login didn't exist, throw an exception
    // --- set new password
    // --- change requireChange to true

    public static void changePass (Connection con, String login, String oldPass, String newPass) throws AccountException, LoginDontExistException, OldPasswordDoesntMatch {
        PreparedStatement statement;

        try {
            statement = con.prepareStatement("SELECT * FROM accounts WHERE login = ?");
            statement.setString(1, login);
            ResultSet resultSet = statement.executeQuery();
            if (!resultSet.next()) {
                statement.close();

                throw new LoginDontExistException();
            }

            String oldPassHash = resultSet.getString("password");
            if (!BCrypt.checkpw(oldPass, oldPassHash)) {
                throw new OldPasswordDoesntMatch();
            }
            resultSet.close();

            statement.close();

            statement = con.prepareStatement("UPDATE accounts SET password = ? WHERE login = ?");

            String hashedPass = BCrypt.hashpw(newPass, BCrypt.gensalt());

            statement.setString(1, hashedPass);
            statement.setString(2, login);

            statement.executeUpdate();
        } catch (SQLException e) {
            throw new AccountException(e.getMessage());
        }
    }

    // resetPass
    // --- Connection con, String login
    // --- generate 6 character long password
    // --- if login didn't exist, throw an exception
    // --- set this password
    // --- change requireChange to true
    // updateAcc
    // --- Connection con, String login, int secLvl
    // --- if login didn't exist, throw an exception
    // --- set new secLvl
    // deleteAcc
    // --- Connection con, String login, int secLvl
    // --- if login didn't exist, throw an exception
    // --- deleteAcc
}
