# Developer guide
**Flow:** Login → Home → CreateDrill → ClassMode → Results → Dashboard. Data: UI → Repository → Firebase/Retrofit. Pure logic lives in `logic/` and has no Android imports (unit-testable).

| Role | Files | Notes |
|---|---|---|
| Frontend | `ui/*.java`, `res/layout/*.xml` | Each Activity binds ids with `findViewById`; keep ids unchanged or update both sides |
| Backend | `data/*`, `logic/DrillGenerator`, `StatsAggregator`, `database.rules.json` | Change a DB field → update model, rules, `docs/FIREBASE_SETUP.md` together |
| UI/UX | `res/values/*`, `res/drawable/*`, `activity_class_mode.xml` | Classroom rule: large text, icon + color for feedback |

* Add a skill: one line in `CurriculumGuide` (id, grade, topic, range, style). No other change needed.
* Add a topic: `Topic`, a builder in `DrillGenerator.make`, label in `Topic.label`, tests. (Out of approved scope: multiplication/division/fractions.)
* Run unit tests: `./gradlew testDebugUnitTest` (or right-click `app/src/test` in Android Studio).
* Repositories deliver callbacks on the main thread. Writes are queued offline; `onRejected` reports server refusals (shown as a toast).
