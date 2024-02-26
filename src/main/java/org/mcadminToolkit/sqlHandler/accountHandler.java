package org.mcadminToolkit.sqlHandler;

import org.springframework.security.crypto.bcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.mcadminToolkit.utils.passwordGenerator.generatePassword;

public class accountHandler {

    final static String loginAllowedCharacters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_";

    public static String createAcc (Connection con, String login, int secLvl) throws AccountException, TooLongLoginException, LoginExistsException, LoginContainsDisallowedCharacterException {
        // todo make configurable length of generated pass
        PreparedStatement statement;

        if (login.length() > 50) {
            throw new TooLongLoginException();
        }

        for (int i = 0; i < login.length(); i++) {
            boolean doesNotContainDisallowedCharacters = false;
            char character = login.charAt(i);

            for (int j = 0; j < loginAllowedCharacters.length(); j++) {
                if (loginAllowedCharacters.charAt(j) == character) doesNotContainDisallowedCharacters = true;
            }

            if (!doesNotContainDisallowedCharacters) throw new LoginContainsDisallowedCharacterException();
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

            statement = con.prepareStatement("UPDATE accounts SET password = ?, requireChange = 0 WHERE login = ?");

            String hashedPass = BCrypt.hashpw(newPass, BCrypt.gensalt());

            statement.setString(1, hashedPass);
            statement.setString(2, login);

            statement.executeUpdate();
        } catch (SQLException e) {
            throw new AccountException(e.getMessage());
        }
    }

    public static void updateAcc (Connection con, String login, int secLvl) throws LoginDontExistException, AccountException {
        PreparedStatement statement;

        try {
            statement = con.prepareStatement("SELECT * FROM accounts WHERE login = ?");
            statement.setString(1, login);
            ResultSet resultSet = statement.executeQuery();
            if (!resultSet.next()) {
                statement.close();

                throw new LoginDontExistException();
            }
            resultSet.close();

            statement.close();

            statement = con.prepareStatement("UPDATE accounts SET secLvl = ? WHERE login = ?");

            statement.setInt(1, secLvl);
            statement.setString(2, login);

            statement.executeUpdate();
        } catch (SQLException e) {
            throw new AccountException(e.getMessage());
        }
    }

    public static String resetPass (Connection con, String login) throws LoginDontExistException, AccountException {
        // todo make configurable length of generated pass

        PreparedStatement statement;

        try {
            statement = con.prepareStatement("SELECT * FROM accounts WHERE login = ?");
            statement.setString(1, login);
            ResultSet resultSet = statement.executeQuery();
            if (!resultSet.next()) {
                statement.close();

                throw new LoginDontExistException();
            }
            resultSet.close();

            statement.close();

            statement = con.prepareStatement("UPDATE accounts SET password = ?, requireChange = 1 WHERE login = ?");

            String password = generatePassword (6);

            String hashedPass = BCrypt.hashpw(password, BCrypt.gensalt());

            statement.setString(1, hashedPass);
            statement.setString(2, login);

            statement.executeUpdate();

            return password;
        } catch (SQLException e) {
            throw new AccountException(e.getMessage());
        }
    }

    public static void deleteAcc(Connection con, String login) throws LoginDontExistException, AccountException {
        PreparedStatement statement;

        try {
            statement = con.prepareStatement("SELECT * FROM accounts WHERE login = ?");
            statement.setString(1, login);
            ResultSet resultSet = statement.executeQuery();
            if (!resultSet.next()) {
                statement.close();

                throw new LoginDontExistException();
            }

            resultSet.close();
            statement.close();

            statement = con.prepareStatement("DELETE FROM accounts WHERE login = ?");
            statement.setString(1, login);

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new AccountException(e.getMessage());
        }
    }

    public static String[] listAccs (Connection con) throws AccountException {
        PreparedStatement statement;

        try {
            statement = con.prepareStatement("SELECT login FROM accounts");

            ResultSet resultSet = statement.executeQuery();

            List<String> logins = new ArrayList<String>();

            while (resultSet.next()) {
                logins.add(resultSet.getString(1));
            }

            return logins.toArray(new String[0]);
        } catch (SQLException e) {
            throw new AccountException(e.getMessage());
        }
    }

    public static int checkIfAccountExist (Connection con, String login, String password) throws AccountException, WrongPasswordException, RequirePasswordChangeException {
        PreparedStatement statement;

        try {
            statement = con.prepareStatement("SELECT secLvl, password, requireChange FROM accounts WHERE login = ?");

            statement.setString(1, login);

            ResultSet resultSet = statement.executeQuery();

            if (!resultSet.next()) {
                return -1;
            }

            String hashedPass = resultSet.getString(2);

            if (!BCrypt.checkpw(password, hashedPass)) {
                throw new WrongPasswordException();
            }

            if (resultSet.getBoolean(3)) {
                throw new RequirePasswordChangeException();
            }

            return resultSet.getInt(1);
        } catch (SQLException e) {
            throw new AccountException(e.getMessage());
        }
    }

    public static int checkIfAccountExist (Connection con, int accountId) throws AccountException {
        PreparedStatement statement;

        try {
            statement = con.prepareStatement("SELECT secLvl FROM accounts WHERE id = ?");

            statement.setInt(1, accountId);

            ResultSet resultSet = statement.executeQuery();

            if (!resultSet.next()) {
                return -1;
            }

            return resultSet.getInt(1);
        } catch (SQLException e) {
            throw new AccountException(e.getMessage());
        }
    }
}
