# Emily TODO

## Next

- Keep the main product focus on Coach and Data Review.
- Refine the Profile tab after Troy tests the first local limits and notes fields.
- Test the real Coach backend call from the Samsung phone using the Profile backend URL.
- Use the hidden Debug tab to troubleshoot backend reachability, last request payload, last response, token usage, and errors.
- Test Health Connect permissions on the live Samsung phone with only some categories granted.
- Confirm Import Today fills steps, sleep, heart rate, resting heart rate, workouts, active calories, and weight when those permissions are granted.
- Improve the Health Connect card message so it lists which selected categories are granted and which still need permission.
- Make empty data states cleaner when Health Connect has permission but no data for today.

## Coach Work

- Treat Emily Coach as the main app experience after Health Connect import.
- Use local Emily Profile values when comparing workout peak HR to Troy's saved caution/goal values.
- Decide what data Emily Coach should send to ChatGPT.
- Keep the private backend as the only place that stores the OpenAI API key.
- Add a real coach response screen after the backend exists.
- Define the exact suggestion format Emily should display in the ChatGPT coach response card.
- Keep suggested coach questions available so Troy can tap common questions without remembering what to ask.
- Keep the coach language non-medical: summarize, explain patterns, ask questions, and suggest small wellness steps.

## App Polish

- Tighten mobile layouts so long labels cannot wrap awkwardly inside small boxes.
- Add better loading and error states during Import Today.
- Add a simple settings screen for app version, Health Connect status, and privacy notes.
- Add a manual refresh timestamp after data import.

## Data Upgrades

- Treat imported health data as review material for Emily Coach, not as manual form entry.
- Move saved check-ins from `SharedPreferences` to Room or DataStore.
- Store imported snapshots by date so trends are easier to review later.
- Add optional charts for steps, sleep, resting heart rate, and workouts.
- Add blood pressure, oxygen saturation, nutrition, or other Health Connect types only after the basic flow is stable.

## Release Prep

- Create a GitHub repository and push `master`.
- Add screenshots after the app UI settles.
- Add a privacy policy before any public release.
- Create a signed release build only when the app is ready to share outside local testing.
