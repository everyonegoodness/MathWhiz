# Math Whiz — Guided Arithmetic Practice for Kindergarten to Grade 1

Teacher-operated classroom app for a TV/projector. Pupils answer aloud; the teacher taps. **Teacher accounts only**
(no student login, class codes or per-student scores).

Java • XML layouts • Activities + Repository classes • Firebase Auth + Realtime Database • Fruityvice API (Retrofit)

## Status — read this first
| Area | State |
|---|---|
| Drill generator, scoring/streak, stats, fruit facts, validators | **Verified**: 22 JUnit tests run and pass (see `docs/TESTING.md`) |
| All Android screens, Firebase, Fruityvice calls, security rules | **Implemented, NOT yet built or run.** The generating environment had no Android SDK or Firebase access. Expect a few compile/runtime fixes on first build. |
| Skill table | **Placeholder** — replace with the cited MATATAG table (`logic/CurriculumGuide.java`) |

## Features (first build)
Teacher register/login • Home • Drill setup (grade, section, skill, count, flashcard / multiple choice, timed / untimed) •
Class mode (Reveal → Correct/Wrong → Next; or tap A–D) • Right/wrong feedback with icon + color • Class score + streak •
Results + Fruityvice fun fact (offline fallback) • Dashboard (grade/section filters, accuracy by topic and skill,
most-missed types, missed questions, session history).
Not included (not chosen for this build): teacher notes, Fruit of the Day, delete/edit drills.

## Open and run
1. Install **Android Studio** (Koala or newer; uses JDK 17).
2. Follow **`docs/FIREBASE_SETUP.md`** (create project, enable Email/Password + Realtime Database, download
   `google-services.json` into `app/`, paste `database.rules.json`). **The build fails without `google-services.json`.**
3. `File ▸ Open` this folder. Android Studio creates the Gradle wrapper and syncs. Run on a device/emulator (Android 7+).
   Class mode is landscape-only.

Never commit `google-services.json` (already in `.gitignore`).

## Project layout
```
app/src/main/java/com/mathwhiz/app/
  model/   Drill, SessionResult, ResponseRecord, Skill, Question, Fruit, FruitDto ...  (plain Java, no Android)
  logic/   DrillGenerator, CurriculumGuide, SessionTracker, StatsAggregator, FruitCatalog, FruitFacts, Validators
  data/    AuthRepository, DrillRepository, SessionRepository, FruitRepository, FruityviceApi  (Firebase + Retrofit)
  ui/      Login, Register, Home, CreateDrill, DrillList, ClassMode, Results, Dashboard
app/src/main/res/  layout/ values/(colors, styles, strings) drawable/
app/src/test/      JUnit tests for model/ + logic/
database.rules.json   Firebase security rules
docs/                 FIREBASE_SETUP, TESTING, DEVELOPER_GUIDE, AD_PACKAGE
```

## Who edits what (see `docs/DEVELOPER_GUIDE.md`)
Frontend → `ui/` + `res/layout` • Backend → `data/`, `logic/`, `database.rules.json` • UI/UX → `res/values`, `res/drawable`.

## Customizing the design
* Colors: `res/values/colors.xml`. Button/text sizes: `res/values/styles.xml` (`Widget.MathWhiz.Button*`, `Text.*`).
* Class-mode text sizes: `activity_class_mode.xml` (`autoSizeMaxTextSize`).
* Fruit pictures are emoji (`logic/FruitCatalog.java`). To use drawings, add drawables and swap the emoji for an `ImageView`.
* Strings: `res/values/strings.xml` (many dynamic messages are still in Java).

## Troubleshooting
* **"google-services.json is missing"** → step 2 above.
* **Fruit shows as a box** → old Android version lacks that emoji (mango/avocado/coconut need Android 8–9+). Remove it from `FruitCatalog`.
* **Fruityvice unreachable** → app says "Using the built-in fruit list"; drills and facts still work (by design).
* **PERMISSION_DENIED** → rules not published, or signed out. Re-publish `database.rules.json`.
* **Dashboard loads forever offline** → times out after 12 s with a retry button; data cached from earlier online use may not show.
* **Query needs index warning in Logcat** → publish the rules (they contain `.indexOn`).
