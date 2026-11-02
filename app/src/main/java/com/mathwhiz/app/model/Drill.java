package com.mathwhiz.app.model;

import java.io.Serializable;
import com.google.firebase.database.Exclude;

/** Saved under drills/{drillId}. Public fields + empty constructor = Firebase can map it. */
public class Drill implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final String FORMAT_FLASHCARD = "flashcard";
    public static final String FORMAT_MULTIPLE_CHOICE = "multipleChoice";
    public static final String MODE_TIMED = "timed";
    public static final String MODE_UNTIMED = "untimed";

    public String teacherUid = "";
    public String title = "";
    public String grade = Grade.KINDER;
    public String section = "";
    public String skillId = "";
    public String topic = "";
    public int minNumber;
    public int maxNumber;
    public int questionCount;
    public String format = FORMAT_FLASHCARD;
    public String mode = MODE_UNTIMED;
    public int timeLimitSec;
    public long createdAt;

    public Drill() {}

    @Exclude
    public boolean formatIsMultipleChoice() { return FORMAT_MULTIPLE_CHOICE.equals(format); }

    @Exclude
    public boolean modeIsTimed() { return MODE_TIMED.equals(mode); }
}
