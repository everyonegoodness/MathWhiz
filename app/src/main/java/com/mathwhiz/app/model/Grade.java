package com.mathwhiz.app.model;

/** Stored values are "Kinder" and "1" (as in the approved RTDB structure). */
public final class Grade {
    public static final String KINDER = "Kinder";
    public static final String GRADE_1 = "1";

    private Grade() {}

    public static String label(String grade) {
        if (KINDER.equals(grade)) return "Kindergarten";
        if (GRADE_1.equals(grade)) return "Grade 1";
        return grade == null ? "" : grade;
    }
}
