package com.mathwhiz.app.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** The five math topics allowed by the approved scope. No multiplication, division or fractions. */
public final class Topic {
    public static final String COUNTING = "counting";
    public static final String NUMBER_RECOGNITION = "number_recognition";
    public static final String COMPARING = "comparing";
    public static final String ADDITION = "addition";
    public static final String SUBTRACTION = "subtraction";

    public static final List<String> ALL = Collections.unmodifiableList(
            Arrays.asList(COUNTING, NUMBER_RECOGNITION, COMPARING, ADDITION, SUBTRACTION));

    private Topic() {}

    public static String label(String topic) {
        if (topic == null) return "";
        switch (topic) {
            case COUNTING: return "Counting";
            case NUMBER_RECOGNITION: return "Number recognition";
            case COMPARING: return "Comparing numbers";
            case ADDITION: return "Addition";
            case SUBTRACTION: return "Subtraction";
            default: return topic;
        }
    }
}
