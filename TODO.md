# Emily TODO

## Next

- Push this project to GitHub so it can be used from a second computer.
- Put an OpenAI API key in `backend/.env` on the development computer.
- Run the Emily Coach backend and test `GET /health`.
- Test `POST /api/coach` with a sample health summary.
- Wire the Android app's Create Coach Summary button to call the backend.
- Test Health Connect permissions on the live Samsung phone with only some categories granted.
- Confirm Import Today fills steps, sleep, heart rate, resting heart rate, workouts, active calories, and weight when those permissions are granted.
- Improve the Health Connect card message so it lists which selected categories are granted and which still need permission.
- Make empty data states cleaner when Health Connect has permission but no data for today.

## Coach Work

- Decide what data Emily Coach should send to ChatGPT.
- Keep the private backend as the only place that stores the OpenAI API key.
- Add a real coach response screen after the backend exists.
- Keep the coach language non-medical: summarize, explain patterns, ask questions, and suggest small wellness steps.

## App Polish

- Tighten mobile layouts so long labels cannot wrap awkwardly inside small boxes.
- Add better loading and error states during Import Today.
- Add a simple settings screen for app version, Health Connect status, and privacy notes.
- Add a manual refresh timestamp after data import.

## Data Upgrades

- Move saved check-ins from `SharedPreferences` to Room or DataStore.
- Store imported snapshots by date so trends are easier to review later.
- Add optional charts for steps, sleep, resting heart rate, and workouts.
- Add blood pressure, oxygen saturation, nutrition, or other Health Connect types only after the basic flow is stable.

## Release Prep

- Create a GitHub repository and push `master`.
- Add screenshots after the app UI settles.
- Add a privacy policy before any public release.
- Create a signed release build only when the app is ready to share outside local testing.
