# Advertisement package — Math Whiz (target length 58 s; allowed 55–60 s)

**Rules:** record only screens from the running app, and only features marked VERIFIED in `docs/TESTING.md` after the team has tested on a device.
Anything that fails testing must be cut from the script. Nothing here mentions student accounts, scores per pupil, or features not in the app.

## Setup
Use a phone or emulator in **landscape**, screen recording 1080p, 30 fps. Run **Load demo data (fake)** first so the dashboard is full. Use a Wi-Fi connection so the Fruityvice fact is real. Don't show real pupil names (the app has none).

## Timed storyboard
| Time | Screen to record (real app) | Narration | On-screen text |
|---|---|---|---|
| 0:00–0:05 | Login screen with 🍎🍌🍇 logo | "Teaching early math should be fun… and simple." | Math Whiz |
| 0:05–0:12 | Register/Login → Home | "Math Whiz is built for Kindergarten and Grade 1 teachers." | For Kinder – Grade 1 |
| 0:12–0:22 | Create Drill: choose grade, skill, 10 questions, Multiple choice, Timed | "Set up a drill in seconds: pick the grade, the skill, and the format." | Set up in seconds |
| 0:22–0:32 | Class Mode: fruit counting question, large picture | "Put it on the TV. Big fruit pictures make counting clear for the whole class." | Made for TV & projector |
| 0:32–0:42 | Tap an option: green correct, then a red wrong with the answer shown | "Pupils answer aloud. You tap. Instant feedback keeps everyone learning." | Instant feedback |
| 0:42–0:48 | Flashcard: Reveal → Correct; streak counter rising | "Prefer flashcards? Reveal the answer and track the class streak." | Class streak 🔥 |
| 0:48–0:53 | Results screen with fruit fun fact | "End each drill with a fruit fun fact from the Fruityvice API." | Fun fruit facts |
| 0:53–0:58 | Dashboard: topic accuracy bars, most-missed types | "Then see where your class needs help." | Know where to help |
| 0:58 end card | Logo + app name | — | Math Whiz — Learn. Count. Grow. |
(If the total runs under 55 s, hold the last two shots longer; over 60 s, shorten the setup shot.)

## Narration script (≈130 words, calm friendly voice)
"Teaching early math should be fun… and simple. Math Whiz is built for Kindergarten and Grade 1 teachers. Set up a drill in seconds: pick the grade, the skill, and the format. Put it on the TV, and big fruit pictures make counting clear for the whole class. Pupils answer aloud. You tap. Instant feedback keeps everyone learning. Prefer flashcards? Reveal the answer and track the class streak. End each drill with a fruit fun fact from the Fruityvice API. Then see exactly where your class needs help. Math Whiz. Learn. Count. Grow."

## Shot-by-shot, transitions, audio, editing
* One continuous screen recording per row above; trim dead taps. Use quick 0.3 s cross-dissolves between shots; no fancy effects.
* Zoom (110%) on the picture area in the 0:22 shot and on the green/red feedback in the 0:32 shot.
* Captions: bold sans-serif, ≥ 48 px, white on a dark bar. Keep colors from the app palette (green #2E7D32, orange #EF6C00).
* Music: royalty-free light/upbeat track at −22 dB under narration; fade out in the last 2 s. Record narration in a quiet room; normalize to −16 LUFS.
* Tools: CapCut, DaVinci Resolve or Clipchamp.
* Export: MP4 (H.264), 1920×1080, 30 fps, AAC 192 kbps. Check the length is 55–60 s.

## Final checklist
- [ ] 55–60 s  - [ ] only real app screens  - [ ] each shown feature VERIFIED in TESTING.md  - [ ] narration clear, music not louder than voice
- [ ] shows: login/register, drill setup, class mode, feedback, results, API fact, dashboard, Firebase mention (optional shot of console data)
- [ ] no real pupil data  - [ ] captions readable on a phone  - [ ] credits: Fruityvice API
