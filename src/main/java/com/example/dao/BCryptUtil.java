package com.example.dao;

import org.mindrot.jbcrypt.BCrypt;

public class BCryptUtil {
    // $2a$12$ = version + cost factor (12)
    // Les 22 suivants sont le sel (Base64)
    private static final String FIXED_SALT = "$2a$12$abcdefghijklmnopqrstuv"; // ✅ 22 caractères exactement

    public static String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, FIXED_SALT);
    }

    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        // On re-hash le mot de passe brut avec le SEL FIXE
        String hashedInput = hashPassword(plainPassword);
        // Et on compare avec celui stocké en BDD
        return hashedInput.equals(hashedPassword);
    }

}