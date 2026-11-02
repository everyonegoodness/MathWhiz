package com.mathwhiz.app.logic;

import com.mathwhiz.app.model.ResponseRecord;
import com.mathwhiz.app.model.SessionResult;
import com.mathwhiz.app.model.Topic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Class-level statistics for the dashboard. Counts responses; never per-student. */
public final class StatsAggregator {
    private StatsAggregator() {}

    public static class Stat {
        public final String key;
        public final String label;
        public int total;
        public int correct;

        public Stat(String key, String label) {
            this.key = key;
            this.label = label;
        }

        public int wrong() { return total - correct; }
        public double accuracy() { return total == 0 ? 0 : 100.0 * correct / total; }
    }

    public static class MissedQuestion {
        public final String question;
        public final String fruit;
        public final String topic;
        public int count;

        public MissedQuestion(String question, String fruit, String topic) {
            this.question = question;
            this.fruit = fruit;
            this.topic = topic;
        }
    }

    private static boolean matches(String filter, String value) {
        return filter == null || filter.isEmpty() || filter.equals(value);
    }

    public static List<ResponseRecord> filterResponses(List<ResponseRecord> all, String grade, String section) {
        List<ResponseRecord> out = new ArrayList<>();
        for (ResponseRecord r : all) {
            if (matches(grade, r.grade) && matches(section, r.section)) out.add(r);
        }
        return out;
    }

    public static List<SessionResult> filterSessions(List<SessionResult> all, String grade, String section) {
        List<SessionResult> out = new ArrayList<>();
        for (SessionResult s : all) {
            if (matches(grade, s.grade) && matches(section, s.section)) out.add(s);
        }
        Collections.sort(out, new Comparator<SessionResult>() {
            @Override
            public int compare(SessionResult a, SessionResult b) {
                return Long.compare(b.date, a.date);   // newest first
            }
        });
        return out;
    }

    public static List<SessionResult> filterSessionsByDateRange(List<SessionResult> all,
                                                                  long startInclusive,
                                                                  long endExclusive) {
        List<SessionResult> out = new ArrayList<>();
        for (SessionResult session : all) {
            if (session.date >= startInclusive && session.date < endExclusive) out.add(session);
        }
        Collections.sort(out, new Comparator<SessionResult>() {
            @Override
            public int compare(SessionResult a, SessionResult b) {
                return Long.compare(b.date, a.date);
            }
        });
        return out;
    }

    public static List<ResponseRecord> filterResponsesForSessions(List<ResponseRecord> responses,
                                                                   List<SessionResult> sessions) {
        Map<String, Boolean> sessionIds = new LinkedHashMap<>();
        for (SessionResult session : sessions) {
            if (session.sessionId != null && !session.sessionId.isEmpty()) {
                sessionIds.put(session.sessionId, true);
            }
        }
        List<ResponseRecord> out = new ArrayList<>();
        for (ResponseRecord response : responses) {
            if (response.sessionId != null && sessionIds.containsKey(response.sessionId)) out.add(response);
        }
        return out;
    }

    public static Stat overall(List<ResponseRecord> rs) {
        Stat s = new Stat("all", "All questions");
        for (ResponseRecord r : rs) {
            s.total++;
            if (r.correct) s.correct++;
        }
        return s;
    }

    public static List<Stat> byTopic(List<ResponseRecord> rs) {
        Map<String, Stat> map = new LinkedHashMap<>();
        for (String t : Topic.ALL) map.put(t, new Stat(t, Topic.label(t)));
        for (ResponseRecord r : rs) {
            Stat s = map.get(r.topic);
            if (s == null) continue;
            s.total++;
            if (r.correct) s.correct++;
        }
        List<Stat> out = new ArrayList<>();
        for (Stat s : map.values()) if (s.total > 0) out.add(s);
        return out;
    }

    public static List<Stat> bySkill(List<ResponseRecord> rs) {
        return byTopic(rs);
    }

    /** Skills (problem types) the class misses most: most wrong answers first, then lowest accuracy. */
    public static List<Stat> mostMissed(List<ResponseRecord> rs, int limit) {
        List<Stat> out = new ArrayList<>();
        for (Stat s : bySkill(rs)) if (s.wrong() > 0) out.add(s);
        Collections.sort(out, new Comparator<Stat>() {
            @Override
            public int compare(Stat a, Stat b) {
                if (a.wrong() != b.wrong()) return Integer.compare(b.wrong(), a.wrong());
                return Double.compare(a.accuracy(), b.accuracy());
            }
        });
        return out.size() > limit ? new ArrayList<>(out.subList(0, limit)) : out;
    }

    /** The specific questions that were answered wrong most often. */
    public static List<MissedQuestion> missedQuestions(List<ResponseRecord> rs, int limit) {
        Map<String, MissedQuestion> map = new LinkedHashMap<>();
        for (ResponseRecord r : rs) {
            if (r.correct) continue;
            MissedQuestion m = map.get(r.question);
            if (m == null) {
                m = new MissedQuestion(r.question, r.fruit, r.topic);
                map.put(r.question, m);
            }
            m.count++;
        }
        List<MissedQuestion> out = new ArrayList<>(map.values());
        Collections.sort(out, new Comparator<MissedQuestion>() {
            @Override
            public int compare(MissedQuestion a, MissedQuestion b) {
                return Integer.compare(b.count, a.count);
            }
        });
        return out.size() > limit ? new ArrayList<>(out.subList(0, limit)) : out;
    }
}
