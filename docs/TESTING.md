# Test log — honest status
Legend: **VERIFIED** = actually executed and passed · **UNTESTED** = implemented, never run · **FAILED** = ran, failed.

## A. Executed in the build environment (JDK 21 + JUnit 4.13.2, pure-Java layers only)
Command: `javac` model+logic, then `org.junit.runner.JUnitCore DrillGeneratorTest LogicTest` → **OK (22 tests)**.
A deliberate bug (subtraction allowed negatives) was injected and the suite failed as expected, so the tests do detect errors.

| ID | What | Status |
|---|---|---|
| L-1 | Every skill × 40 seeds × 30 questions: 4 distinct options, correct option once, answer text matches | VERIFIED |
| L-2 | Addition: answers correct, sums within range | VERIFIED |
| L-3 | Subtraction never negative (all options ≥ 0), minuend within range | VERIFIED |
| L-4 | Counting picture count == answer | VERIFIED |
| L-5 | Number recognition numeral→word | VERIFIED |
| L-6 | Comparing picks biggest/smallest group/number; 4 different values | VERIFIED |
| L-7 | Teacher-narrowed range respected; invalid ranges rejected | VERIFIED |
| L-8 | Score, streak, best streak, accuracy | VERIFIED |
| L-9 | Dashboard aggregation, grade/section filters, most-missed ordering | VERIFIED |
| L-10 | Fruit fact from API-shaped data; missing fields → null → fallback; ≥20 offline facts | VERIFIED (parsing only, not live API) |
| L-11 | Form validators (email, password, counts, time) | VERIFIED |
| L-12 | Demo data consistent for both grades | VERIFIED |
Static checks also run: all XML well-formed; every `R.id/layout/string/color/drawable` and `@style` reference resolves; each Activity's ids exist in its layout.

## B. NOT yet run — team must execute on a device/emulator (rubric areas)
| ID | Test | Rubric | Status |
|---|---|---|---|
| F-1 | App builds and installs (Android 7+) | Functionality | UNTESTED |
| F-2 | Register → Home; wrong password message; duplicate email message; offline message | Firebase | UNTESTED |
| F-3 | Create drill with invalid input (blank section, count 0/31, max above range, time 4) shows errors | Functionality | UNTESTED |
| F-4 | Flashcard run: Reveal → Correct/Wrong → Next → Results; score matches taps | Functionality | UNTESTED |
| F-5 | Multiple choice run: correct turns green, wrong turns red + answer shown | Functionality | UNTESTED |
| F-6 | Timed run: countdown, time-up counts as wrong, resumes after backgrounding | Functionality | UNTESTED |
| F-7 | Back during drill → confirm dialog; nothing saved if ended | Functionality | UNTESTED |
| F-8 | Console shows sessions/ + responses/ with correct teacherUid after a drill | Firebase | UNTESTED |
| F-9 | Dashboard filters (grade, section) change numbers; empty and error/retry states | Functionality | UNTESTED |
| F-10 | Rules: Teacher B cannot read/write Teacher A's data (use Rules Playground/emulator); unauthenticated denied; bad field rejected | Firebase | UNTESTED |
| F-11 | Airplane mode: drill still runs with built-in fruits, results sync on reconnect | Functionality | UNTESTED |
| A-1 | Fruityvice: Logcat shows HTTP 200 for `/api/fruit/all`; results screen says "Data from Fruityvice" | API | UNTESTED (API not reachable from build environment) |
| A-2 | Fruityvice blocked/airplane: results screen says offline fact, no crash | API | UNTESTED |
| U-1 | Landscape on TV/projector or screen mirror: text readable from the back row; buttons ≥ 60 dp | UI/UX | UNTESTED |
| U-2 | Emoji render on the lowest-version device you own | UI/UX | UNTESTED |
Record date, device, result and screenshots beside each row. Do not mark PASSED until done.

## C. Things to verify about Fruityvice before claiming integration
Open `https://www.fruityvice.com/api/fruit/banana` in a browser. The code expects: `name`, `family`, `genus`, `order`, `nutritions{calories,fat,sugar,carbohydrates,protein}`. If the real JSON differs, edit `model/FruitDto.java` only.
