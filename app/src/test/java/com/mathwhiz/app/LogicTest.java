package com.mathwhiz.app;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.mathwhiz.app.logic.FruitCatalog;
import com.mathwhiz.app.logic.FruitFacts;
import com.mathwhiz.app.logic.SessionTracker;
import com.mathwhiz.app.logic.StatsAggregator;
import com.mathwhiz.app.logic.Validators;
import com.mathwhiz.app.model.FactResult;
import com.mathwhiz.app.model.FruitDto;
import com.mathwhiz.app.model.ResponseRecord;
import com.mathwhiz.app.model.SessionResult;
import com.mathwhiz.app.model.Topic;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class LogicTest {

    private static ResponseRecord resp(String grade, String section, String skill, String topic,
                                       boolean ok, String q) {
        ResponseRecord r = new ResponseRecord();
        r.grade = grade; r.section = section; r.skillId = skill; r.topic = topic; r.correct = ok; r.question = q;
        r.fruit = "apple";
        return r;
    }

    // ---------- scoring / streak
    @Test
    public void trackerCountsScoreAndStreak() {
        SessionTracker t = new SessionTracker();
        boolean[] seq = {true, true, true, false, true, true};
        for (boolean b : seq) t.record(b);
        assertEquals(6, t.getTotal());
        assertEquals(5, t.getCorrect());
        assertEquals(1, t.getWrong());
        assertEquals(2, t.getStreak());
        assertEquals(3, t.getBestStreak());
        assertEquals(83, t.accuracyPercent());
    }

    @Test
    public void trackerEmptyIsZero() {
        assertEquals(0, new SessionTracker().accuracyPercent());
    }

    // ---------- dashboard aggregation
    @Test
    public void aggregatesByTopicAndFilters() {
        List<ResponseRecord> all = new ArrayList<>();
        all.add(resp("Kinder", "Kinder - A", "K-ADD-1", Topic.ADDITION, true, "q1"));
        all.add(resp("Kinder", "Kinder - A", "K-ADD-1", Topic.ADDITION, false, "q2"));
        all.add(resp("1", "Grade 1 - B", "G1-ADD-2", Topic.ADDITION, true, "q5"));
        all.add(resp("Kinder", "Kinder - A", "K-SUB-1", Topic.SUBTRACTION, false, "q3"));
        all.add(resp("1", "Grade 1 - B", "G1-ADD-1", Topic.ADDITION, true, "q4"));

        StatsAggregator.Stat overall = StatsAggregator.overall(all);
        assertEquals(5, overall.total);
        assertEquals(3, overall.correct);

        List<StatsAggregator.Stat> byTopic = StatsAggregator.byTopic(all);
        assertEquals(2, byTopic.size());
        assertEquals(Topic.ADDITION, byTopic.get(0).key);        // topic order, not insertion order
        assertEquals(4, byTopic.get(0).total);
        assertEquals(3, byTopic.get(0).correct);

        List<StatsAggregator.Stat> bySkill = StatsAggregator.bySkill(all);
        assertEquals(2, bySkill.size());
        assertEquals(Topic.ADDITION, bySkill.get(0).key);
        assertEquals("Addition", bySkill.get(0).label);
        assertEquals(4, bySkill.get(0).total);
        assertEquals(3, bySkill.get(0).correct);

        List<ResponseRecord> kinderOnly = StatsAggregator.filterResponses(all, "Kinder", null);
        assertEquals(3, kinderOnly.size());
        assertEquals(2, StatsAggregator.filterResponses(all, null, "Grade 1 - B").size());
        assertEquals(5, StatsAggregator.filterResponses(all, "", "").size());
        assertEquals(0, StatsAggregator.filterResponses(all, "1", "Kinder - A").size());
    }

    @Test
    public void mostMissedSortsByWrongCount() {
        List<ResponseRecord> all = new ArrayList<>();
        for (int i = 0; i < 4; i++) all.add(resp("1", "S", "G1-SUB-1", Topic.SUBTRACTION, false, "sub" + (i % 2)));
        all.add(resp("1", "S", "G1-SUB-1", Topic.SUBTRACTION, true, "x"));
        all.add(resp("1", "S", "G1-ADD-1", Topic.ADDITION, false, "add"));
        all.add(resp("1", "S", "G1-CNT-1", Topic.COUNTING, true, "cnt"));

        List<StatsAggregator.Stat> missed = StatsAggregator.mostMissed(all, 5);
        assertEquals(2, missed.size());                           // counting has no misses
        assertEquals(Topic.SUBTRACTION, missed.get(0).key);
        assertEquals(4, missed.get(0).wrong());
        assertEquals(1, StatsAggregator.mostMissed(all, 1).size());

        List<StatsAggregator.MissedQuestion> mq = StatsAggregator.missedQuestions(all, 10);
        assertEquals(2, mq.get(0).count);
        assertEquals(3, mq.size());
    }

    @Test
    public void sessionsAreFilteredAndNewestFirst() {
        List<SessionResult> list = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            SessionResult s = new SessionResult();
            s.grade = i == 2 ? "1" : "Kinder"; s.section = "S"; s.date = i * 1000L;
            list.add(s);
        }
        List<SessionResult> kinder = StatsAggregator.filterSessions(list, "Kinder", null);
        assertEquals(2, kinder.size());
        assertEquals(1000L, kinder.get(0).date);
    }

    @Test
    public void sessionsCanBeFilteredByDateAndResponsesFollowTheirSessions() {
        SessionResult older = new SessionResult();
        older.sessionId = "old";
        older.date = 99L;
        SessionResult startBoundary = new SessionResult();
        startBoundary.sessionId = "start";
        startBoundary.date = 100L;
        SessionResult endBoundary = new SessionResult();
        endBoundary.sessionId = "end";
        endBoundary.date = 200L;

        List<SessionResult> sessions = new ArrayList<>();
        sessions.add(older);
        sessions.add(startBoundary);
        sessions.add(endBoundary);
        List<SessionResult> range = StatsAggregator.filterSessionsByDateRange(sessions, 100L, 200L);
        assertEquals(1, range.size());
        assertEquals("start", range.get(0).sessionId);

        ResponseRecord matching = resp("1", "S", "skill", Topic.ADDITION, true, "q1");
        matching.sessionId = "start";
        ResponseRecord excluded = resp("1", "S", "skill", Topic.ADDITION, true, "q2");
        excluded.sessionId = "old";
        List<ResponseRecord> responses = new ArrayList<>();
        responses.add(matching);
        responses.add(excluded);
        List<ResponseRecord> filtered = StatsAggregator.filterResponsesForSessions(responses, range);
        assertEquals(1, filtered.size());
        assertEquals("q1", filtered.get(0).question);
    }

    // ---------- fruit facts / fallback
    @Test
    public void factFromApiDataAndMissingFields() {
        FruitDto dto = new FruitDto();
        dto.name = "Banana";
        dto.family = "Musaceae";
        dto.nutritions = new FruitDto.Nutritions();
        dto.nutritions.sugar = 17.2;
        dto.nutritions.calories = 96.0;
        String fact = FruitFacts.fromDto(dto, new Random(1));
        assertNotNull(fact);
        assertTrue(fact.toLowerCase().contains("banana"));

        FruitDto empty = new FruitDto();
        empty.name = "Mystery";
        assertNull(FruitFacts.fromDto(empty, new Random(1)));          // nothing usable -> caller uses fallback
        assertNull(FruitFacts.fromDto(null, new Random(1)));
        FruitDto noName = new FruitDto();
        noName.family = "X";
        assertNull(FruitFacts.fromDto(noName, new Random(1)));
    }

    @Test
    public void fallbackFactsWork() {
        assertTrue(FruitCatalog.fallbackFactCount() >= 20);
        FactResult f = FruitCatalog.fallbackFact("Grapes", new Random(1));
        assertEquals("grape", f.fruitName);
        assertTrue(!f.fromApi && !f.text.isEmpty());
        FactResult unknown = FruitCatalog.fallbackFact("Durian", new Random(1));
        assertTrue(!unknown.text.isEmpty());                           // general fact, never empty
        assertNotNull(FruitCatalog.randomFallbackFact(new Random(2)));
        assertNotNull(FruitCatalog.emojiFor("Kiwifruit"));
        assertNull(FruitCatalog.emojiFor("Durian"));
    }

    // ---------- form validation
    @Test
    public void validators() {
        assertNull(Validators.email("teacher@school.edu.ph"));
        assertNotNull(Validators.email("teacher@"));
        assertNotNull(Validators.email(""));
        assertNull(Validators.password("abcdef"));
        assertNotNull(Validators.password("abc"));
        assertNotNull(Validators.name("  "));
        assertNotNull(Validators.section(""));
        assertNull(Validators.questionCount("10"));
        assertNotNull(Validators.questionCount("0"));
        assertNotNull(Validators.questionCount("31"));
        assertNotNull(Validators.questionCount("abc"));
        assertNull(Validators.timeLimit("20"));
        assertNotNull(Validators.timeLimit("4"));
        assertNotNull(Validators.timeLimit(""));
    }
}
