package org.mcadminToolkit.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.mcadminToolkit.sqlHandler.*;
import org.mcadminToolkit.utils.passwordGenerator;

import java.sql.Connection;
import java.util.Date;
import java.util.UUID;

public class jwtHandler {

    static Algorithm algorithm = null;
    static JWTVerifier verifier = null;

    public static void initializeJWT () {
        String secret = passwordGenerator.generatePassword(20);

        algorithm = Algorithm.HMAC256(secret);
        verifier = JWT.require(algorithm)
                .withIssuer("CodinLake")
                .build();
    }

    public static String generateToken (Connection con, String login, String password) throws LoginDontExistException, CreateSessionException, WrongPasswordException, RequirePasswordChangeException {
        int secLvl;

        try {
            secLvl = accountHandler.checkIfAccountExist(con, login, password);

            if (secLvl == -1) {
                throw new LoginDontExistException();
            }
        } catch (AccountException e) {
            throw new CreateSessionException(e.getMessage());
        }

        String token = JWT.create()
            .withIssuer("CodinLake")
            .withSubject("MCAdmin-Toolkit")
            .withClaim("login", login)
            .withClaim("secLvl", secLvl)
            .withIssuedAt(new Date())
            .withExpiresAt(new Date(System.currentTimeMillis() + 1800000L))
            .withJWTId(UUID.randomUUID().toString())
            .withNotBefore(new Date(System.currentTimeMillis() - 60000L))
            .sign(algorithm);

        return token;
    }

    public static String generateToken (Connection con, int accountId, String login) throws LoginDontExistException, CreateSessionException {
        int secLvl;

        try {
            secLvl = accountHandler.checkIfAccountExist(con, accountId);

            if (secLvl == -1) {
                throw new LoginDontExistException();
            }
        } catch (AccountException e) {
            throw new CreateSessionException(e.getMessage());
        }

        String token = JWT.create()
                .withIssuer("CodinLake")
                .withSubject("MCAdmin-Toolkit")
                .withClaim("login", login)
                .withClaim("secLvl", secLvl)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + 1800000L))
                .withJWTId(UUID.randomUUID().toString())
                .withNotBefore(new Date(System.currentTimeMillis() - 60000L))
                .sign(algorithm);

        return token;
    }

    public static account verifyToken (String token) throws InvalidSessionException {

        DecodedJWT decodedJWT;

        try {
            decodedJWT = verifier.verify(token);
        } catch (JWTVerificationException e) {
            throw new InvalidSessionException(e.getMessage());
        }

        Claim login = decodedJWT.getClaim("login");
        Claim secLvl = decodedJWT.getClaim("secLvl");

        account acc = new account(secLvl.asInt(), login.asString());

        return acc;
    }
}
