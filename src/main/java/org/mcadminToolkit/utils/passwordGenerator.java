package org.mcadminToolkit.utils;

import java.util.Random;

public class passwordGenerator {
    static final String capitalChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    static final String uncapitalChars = "abcdefghijklmnopqrstuvwxyz";
    static final String numbers = "1234567890";
    static final String specialCharacters = "!@#$%^&*()[{]}-_=+;:'\",<.>/?";
    static final String curatedSpecialCharacters = "-_,.";

    public enum SpecialCharactersMode {
        ALL,
        CURATED,
        NONE
    };

    public static String generatePassword (int len) {
        String chars = capitalChars + uncapitalChars + numbers + specialCharacters;

        String password = "";

        for (int i = 0; i < len; i++) {
            Random randomizer = new Random();
            int random = randomizer.nextInt(chars.length());
            password += chars.charAt(random);
        }

        return password;
    }

    public static String generatePassword (int len, boolean useCapital, boolean useUncapital, boolean useNumbers, SpecialCharactersMode specialCharactersMode) {
        String chars =
                (useCapital ? capitalChars : "") +
                (useUncapital ? uncapitalChars : "") +
                (useNumbers ?  numbers : "");

        switch (specialCharactersMode) {
            case ALL:
                chars += specialCharacters;
                break;
            case CURATED:
                chars += curatedSpecialCharacters;
                break;
        }

        String password = "";

        for (int i = 0; i < len; i++) {
            Random randomizer = new Random();
            int random = randomizer.nextInt(chars.length());
            password += chars.charAt(random);
        }

        return password;
    }
}
