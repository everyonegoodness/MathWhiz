# Firebase setup (team action required)
1. https://console.firebase.google.com → **Add project** (Spark/free plan is enough).
2. **Add Android app**: package name `com.mathwhiz.app`. Download **google-services.json** → put in `MathWhiz/app/`. Do NOT commit it.
3. **Build ▸ Authentication ▸ Sign-in method ▸ Email/Password → Enable.**
4. **Build ▸ Realtime Database ▸ Create database** (choose the region nearest you; start in locked mode).
   If your region is not US, re-download `google-services.json` after creating the database so it contains `firebase_url`.
5. **Realtime Database ▸ Rules**: paste the contents of `database.rules.json` → **Publish**.
6. Run the app → Register → create and run a drill → Dashboard. In the console, confirm `users/`, `sessions/`, `responses/` appear.

## Data structure
```
users/{uid}        name, email, createdAt
drills/{id}        teacherUid, title, grade("Kinder"|"1"), section, skillId, topic, minNumber, maxNumber,
                   questionCount, format, mode, timeLimitSec, createdAt
sessions/{id}      teacherUid, drillId, title, grade, section, skillId, topic, format, mode, date,
                   totalCorrect, totalQuestions, bestStreak
responses/{id}     sessionId, teacherUid, grade, section*, skillId, topic, fruit*, question, correctAnswer,
                   chosenOption, correct, timedOut, timeSec, timestamp        (* added to the starter schema)
```
## Security rules summary
Default deny. A teacher can only read/write their own `users/{uid}`; drills/sessions/responses can only be listed with
`orderByChild('teacherUid').equalTo(<own uid>)` and only written when `teacherUid` equals the signed-in uid. Field types, ranges
and allowed values are validated; unknown fields are rejected. **Rules have not been run against the emulator — test them (docs/TESTING.md, F-5..F-8).**
