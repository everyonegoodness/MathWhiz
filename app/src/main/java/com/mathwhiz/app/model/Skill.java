package com.mathwhiz.app.model;

/** One row of the curriculum skill table (fixed list in code, NOT stored in Firebase). */
public final class Skill {
    public static final String STYLE_NONE = "";
    public static final String STYLE_GROUPS = "groups";    // comparing groups of fruit
    public static final String STYLE_NUMBERS = "numbers";  // comparing written numerals

    public final String id;
    public final String grade;
    public final String topic;
    public final String title;
    public final int minNumber;
    public final int maxNumber;
    public final String style;
    public final String sourceRef;

    public Skill(String id, String grade, String topic, String title,
                 int minNumber, int maxNumber, String style, String sourceRef) {
        this.id = id;
        this.grade = grade;
        this.topic = topic;
        this.title = title;
        this.minNumber = minNumber;
        this.maxNumber = maxNumber;
        this.style = style;
        this.sourceRef = sourceRef;
    }
}
