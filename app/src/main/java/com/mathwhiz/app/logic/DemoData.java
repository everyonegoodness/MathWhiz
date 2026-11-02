package com.mathwhiz.app.logic;

import com.mathwhiz.app.model.Fruit;
import com.mathwhiz.app.model.Grade;
import com.mathwhiz.app.model.Question;
import com.mathwhiz.app.model.ResponseRecord;
import com.mathwhiz.app.model.SessionResult;
import com.mathwhiz.app.model.Skill;
import com.mathwhiz.app.model.Topic;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Builds FAKE sample sessions (Kinder and Grade 1) so the dashboard is not empty on demo day.
 * Uses no real student data. Shown to the teacher as "Load demo data (fake)".
 */
public final class DemoData {
    private DemoData() {}

    public static class Pack {
        public final SessionResult session;
        public final List<ResponseRecord> responses;

        Pack(SessionResult session, List<ResponseRecord> responses) {
            this.session = session;
            this.responses = responses;
        }
    }

    public static List<Pack> build(String teacherUid, Random rnd, long nowMillis) {
        String[][] plan = {
                // skillId, section, daysAgo
                {"K-CNT-1", "Kinder - Rosal", "9"},
                {"K-NUM-1", "Kinder - Rosal", "7"},
                {"K-ADD-1", "Kinder - Rosal", "5"},
                {"K-SUB-1", "Kinder - Rosal", "3"},
                {"G1-ADD-1", "Grade 1 - Sampaguita", "8"},
                {"G1-SUB-1", "Grade 1 - Sampaguita", "6"},
                {"G1-CMP-1", "Grade 1 - Sampaguita", "4"},
                {"G1-ADD-2", "Grade 1 - Sampaguita", "2"},
                {"G1-SUB-2", "Grade 1 - Sampaguita", "1"},
        };
        List<Fruit> fruits = FruitCatalog.fallbackFruits();
        DrillGenerator gen = new DrillGenerator(rnd);
        List<Pack> out = new ArrayList<>();

        for (String[] row : plan) {
            Skill skill = CurriculumGuide.byId(row[0]);
            if (skill == null) continue;
            int count = 10;
            long when = nowMillis - Long.parseLong(row[2]) * 24L * 3600L * 1000L;
            List<Question> qs = gen.generate(skill, skill.minNumber, skill.maxNumber, count, fruits);

            SessionResult s = new SessionResult();
            s.teacherUid = teacherUid;
            s.drillId = "demo";
            s.title = "DEMO: " + skill.title;
            s.grade = skill.grade;
            s.section = row[1];
            s.skillId = skill.id;
            s.topic = skill.topic;
            s.format = "flashcard";
            s.mode = "untimed";
            s.date = when;
            s.totalQuestions = count;

            SessionTracker tracker = new SessionTracker();
            List<ResponseRecord> rs = new ArrayList<>();
            for (int i = 0; i < qs.size(); i++) {
                Question q = qs.get(i);
                boolean ok = rnd.nextDouble() < chanceCorrect(skill);
                tracker.record(ok);
                ResponseRecord r = new ResponseRecord();
                r.teacherUid = teacherUid;
                r.grade = skill.grade;
                r.section = row[1];
                r.skillId = skill.id;
                r.topic = skill.topic;
                r.fruit = q.fruit;
                r.question = q.prompt;
                r.correctAnswer = q.answer;
                r.chosenOption = "";
                r.correct = ok;
                r.timedOut = false;
                r.timeSec = 4 + rnd.nextInt(10);
                r.timestamp = when + i * 20000L;
                rs.add(r);
            }
            s.totalCorrect = tracker.getCorrect();
            s.bestStreak = tracker.getBestStreak();
            out.add(new Pack(s, rs));
        }
        return out;
    }

    private static double chanceCorrect(Skill skill) {
        switch (skill.topic) {
            case Topic.COUNTING: return 0.90;
            case Topic.NUMBER_RECOGNITION: return 0.85;
            case Topic.COMPARING: return 0.75;
            case Topic.ADDITION: return skill.grade.equals(Grade.KINDER) ? 0.75 : 0.65;
            case Topic.SUBTRACTION: return 0.50;
            default: return 0.7;
        }
    }
}
