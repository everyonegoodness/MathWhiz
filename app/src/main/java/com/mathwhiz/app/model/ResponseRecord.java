package com.mathwhiz.app.model;

/** Saved under responses/{responseId}: one teacher Correct/Wrong tap (or MC choice) for the whole class. */
public class ResponseRecord {
    public String sessionId = "";
    public String teacherUid = "";
    public String grade = "";
    public String section = "";   // added to the starter schema so the dashboard can filter by section
    public String skillId = "";
    public String topic = "";
    public String fruit = "";     // added: lets the review list show a fact about the fruit
    public String question = "";
    public String correctAnswer = "";
    public String chosenOption = "";
    public boolean correct;
    public boolean timedOut;
    public double timeSec;
    public long timestamp;

    public ResponseRecord() {}
}
