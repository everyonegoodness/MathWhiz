package com.mathwhiz.app.model;

/** Saved under sessions/{sessionId}. Class-level totals only: no per-student scores. */
public class SessionResult {
    public String sessionId = "";
    public String teacherUid = "";
    public String drillId = "";
    public String title = "";
    public String grade = "";
    public String section = "";
    public String skillId = "";
    public String topic = "";
    public String format = "";
    public String mode = "";
    public long date;
    public int totalCorrect;
    public int totalQuestions;
    public int bestStreak;

    public SessionResult() {}
}
