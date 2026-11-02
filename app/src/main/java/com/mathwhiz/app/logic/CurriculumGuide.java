package com.mathwhiz.app.logic;

import com.mathwhiz.app.model.Grade;
import com.mathwhiz.app.model.Skill;
import com.mathwhiz.app.model.Topic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Fixed per-level curriculum skill table.
 *
 * !! PLACEHOLDER CONTENT !! The approved plan requires this table to be built from DepEd's MATATAG
 * guides by Documentation A (AIRA) and cited. Those citations were not provided, so every row below uses
 * a clearly marked placeholder source. Replace titles, ranges and sourceRef with the verified table; nothing
 * else in the app needs to change.
 */
public final class CurriculumGuide {
    public static final String PLACEHOLDER = "PLACEHOLDER - replace with MATATAG citation from Documentation A";

    private static final List<Skill> SKILLS;

    static {
        List<Skill> s = new ArrayList<>();
        // ---- Kindergarten ----
        s.add(new Skill("K-CNT-1", Grade.KINDER, Topic.COUNTING, "Count objects 1-5", 1, 5, Skill.STYLE_NONE, PLACEHOLDER));
        s.add(new Skill("K-CNT-2", Grade.KINDER, Topic.COUNTING, "Count objects 1-10", 1, 10, Skill.STYLE_NONE, PLACEHOLDER));
        s.add(new Skill("K-NUM-1", Grade.KINDER, Topic.NUMBER_RECOGNITION, "Recognize numerals 1-10", 1, 10, Skill.STYLE_NONE, PLACEHOLDER));
        s.add(new Skill("K-CMP-1", Grade.KINDER, Topic.COMPARING, "Which group has more or fewer (up to 10)", 1, 10, Skill.STYLE_GROUPS, PLACEHOLDER));
        s.add(new Skill("K-ADD-1", Grade.KINDER, Topic.ADDITION, "Join groups", 1, 10, Skill.STYLE_NONE, PLACEHOLDER));
        s.add(new Skill("K-SUB-1", Grade.KINDER, Topic.SUBTRACTION, "Take away", 1, 10, Skill.STYLE_NONE, PLACEHOLDER));
        // ---- Grade 1 ----
        s.add(new Skill("G1-CNT-1", Grade.GRADE_1, Topic.COUNTING, "Count objects up to 20", 1, 20, Skill.STYLE_NONE, PLACEHOLDER));
        s.add(new Skill("G1-NUM-1", Grade.GRADE_1, Topic.NUMBER_RECOGNITION, "Recognize numerals up to 20", 1, 20, Skill.STYLE_NONE, PLACEHOLDER));
        s.add(new Skill("G1-NUM-2", Grade.GRADE_1, Topic.NUMBER_RECOGNITION, "Recognize numerals up to 100", 1, 100, Skill.STYLE_NONE, PLACEHOLDER));
        s.add(new Skill("G1-CMP-1", Grade.GRADE_1, Topic.COMPARING, "Biggest or smallest number (up to 20)", 1, 20, Skill.STYLE_NUMBERS, PLACEHOLDER));
        s.add(new Skill("G1-CMP-2", Grade.GRADE_1, Topic.COMPARING, "Biggest or smallest number (up to 100)", 1, 100, Skill.STYLE_NUMBERS, PLACEHOLDER));
        s.add(new Skill("G1-ADD-1", Grade.GRADE_1, Topic.ADDITION, "Add, sums up to 10", 1, 10, Skill.STYLE_NONE, PLACEHOLDER));
        s.add(new Skill("G1-ADD-2", Grade.GRADE_1, Topic.ADDITION, "Add, sums up to 20", 1, 20, Skill.STYLE_NONE, PLACEHOLDER));
        s.add(new Skill("G1-SUB-1", Grade.GRADE_1, Topic.SUBTRACTION, "Subtract, from up to 10", 1, 10, Skill.STYLE_NONE, PLACEHOLDER));
        s.add(new Skill("G1-SUB-2", Grade.GRADE_1, Topic.SUBTRACTION, "Subtract, from up to 20", 1, 20, Skill.STYLE_NONE, PLACEHOLDER));
        SKILLS = Collections.unmodifiableList(s);
    }

    private CurriculumGuide() {}

    public static List<Skill> all() { return SKILLS; }

    public static List<Skill> forGrade(String grade) {
        Map<String, Skill> byTopic = new LinkedHashMap<>();
        for (Skill skill : SKILLS) {
            if (!skill.grade.equals(grade)) continue;
            Skill current = byTopic.get(skill.topic);
            // Keep one picker entry per topic, using the widest range available for that grade.
            if (current == null || skill.maxNumber > current.maxNumber) {
                byTopic.put(skill.topic, skill);
            }
        }
        return new ArrayList<>(byTopic.values());
    }

    public static Skill byId(String id) {
        for (Skill s : SKILLS) if (s.id.equals(id)) return s;
        return null;
    }

    /** Position in the table, used to sort skills consistently. Unknown ids go last. */
    public static int indexOf(String id) {
        for (int i = 0; i < SKILLS.size(); i++) if (SKILLS.get(i).id.equals(id)) return i;
        return Integer.MAX_VALUE;
    }
}
