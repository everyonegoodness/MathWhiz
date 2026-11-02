package com.mathwhiz.app.logic;

import java.util.regex.Pattern;

/** Pure input checks shared by the forms. Each returns an error message, or null when the input is valid. */
public final class Validators {
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private Validators() {}

    public static String email(String v) {
        if (v == null || v.trim().isEmpty()) return "Please enter your email.";
        if (!EMAIL.matcher(v.trim()).matches()) return "That email does not look right.";
        return null;
    }

    public static String password(String v) {
        if (v == null || v.isEmpty()) return "Please enter a password.";
        if (v.length() < 6) return "Password must have at least 6 characters.";
        return null;
    }

    public static String name(String v) {
        if (v == null || v.trim().isEmpty()) return "Please enter your name.";
        if (v.trim().length() > 60) return "Name is too long (60 characters max).";
        return null;
    }

    public static String section(String v) {
        if (v == null || v.trim().isEmpty()) return "Please enter the section name.";
        if (v.trim().length() > 40) return "Section name is too long (40 characters max).";
        return null;
    }

    /** Parses an integer field; returns null when empty or not a whole number. */
    public static Integer parseInt(String v) {
        if (v == null) return null;
        try {
            return Integer.parseInt(v.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static String questionCount(String v) {
        Integer n = parseInt(v);
        if (n == null || n < 1 || n > 30) return "Questions must be a whole number from 1 to 30.";
        return null;
    }

    public static String timeLimit(String v) {
        Integer n = parseInt(v);
        if (n == null || n < 5 || n > 120) return "Seconds per question must be from 5 to 120.";
        return null;
    }
}
