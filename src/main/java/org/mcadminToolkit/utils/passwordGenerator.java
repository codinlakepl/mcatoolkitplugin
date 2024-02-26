package org.mcadminToolkit.utils;

import java.util.Random;

public class passwordGenerator {
    static final String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890!@#$%^&*()[{]}-_=+;:'\",<.>/?";

    public static String generatePassword (int len) {
        String password = "";

        for (int i = 0; i < len; i++) {
            Random randomizer = new Random();
            int random = randomizer.nextInt(chars.length());
            password += chars.charAt(random);
        }

        return password;
    }
}
