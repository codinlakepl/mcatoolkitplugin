package org.mcadminToolkit.auth;

import org.mcadminToolkit.mcadminToolkit;
import org.mcadminToolkit.sqlHandler.AccountException;
import org.mcadminToolkit.sqlHandler.LoginDontExistException;
import org.mcadminToolkit.sqlHandler.RequirePasswordChangeException;
import org.mcadminToolkit.sqlHandler.WrongPasswordException;
import org.mcadminToolkit.utils.passwordGenerator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class sessionHandler {
    public static session createSession (Connection con, String login, String password, String device, String model) throws CreateSessionException, LoginDontExistException, RequirePasswordChangeException, WrongPasswordException {
        String jwtToken = jwtHandler.generateToken(con, login, password);
        String refreshKey = org.mcadminToolkit.utils.passwordGenerator.generatePassword(300, true, true, true, passwordGenerator.SpecialCharactersMode.CURATED);

        PreparedStatement statement;

        try {
            statement = con.prepareStatement("INSERT INTO sessions (" +
                        "accountId," +
                        "refreshKey," +
                        "device," +
                        "model" +
                    ")" +
                    "SELECT " +
                        "(SELECT id FROM accounts WHERE login = ? LIMIT 1)," +
                        "?, ?, ?");

            statement.setString(1, login);
            statement.setString(2, refreshKey);
            statement.setString(3, device);
            statement.setString(4, model);

            statement.executeUpdate();
        } catch (SQLException e) {
            throw new CreateSessionException(e.getMessage());
        }

        return new session(refreshKey, jwtToken);
    }

    public static String verifySession (Connection con, String refreshKey, String device, String model) throws InvalidSessionException, CreateSessionException, LoginDontExistException {
        PreparedStatement statement;
        int accountId;
        String login;

        try {
            statement = con.prepareStatement("SELECT " +
                        "sessions.accountId, " +
                        "accounts.login " +
                    "FROM sessions " +
                    "INNER JOIN accounts ON accounts.id = sessions.accountId " +
                    "WHERE " +
                        "CAST((JULIANDAY('now') - JULIANDAY(sessions.last_used_at)) AS INTEGER) < ? AND " +
                        "sessions.refreshKey = ? AND " +
                        "sessions.device = ? AND " +
                        "sessions.model = ?");
            
            statement.setInt(1, mcadminToolkit.sessionLife);
            statement.setString(2, refreshKey);
            statement.setString(3, device);
            statement.setString(4, model);

            ResultSet resultSet = statement.executeQuery();

            if (!resultSet.next()) throw new InvalidSessionException();

            accountId = resultSet.getInt(1);
            login = resultSet.getString(2);

            resultSet.close();
            statement.close();

            statement = con.prepareStatement("UPDATE sessions SET last_used_at = CURRENT_TIMESTAMP WHERE refreshKey = ?");
            statement.setString(1, refreshKey);

            statement.executeUpdate();
        } catch (SQLException e) {
            throw new CreateSessionException(e.getMessage());
        }

        return jwtHandler.generateToken(con, accountId, login);
    }
}
