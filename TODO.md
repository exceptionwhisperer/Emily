# Emily TODO

## One-Month Release Plan

Goal: finish a private Emily test build that Troy can use on the Samsung phone for Health Connect review, HRV/resting heart rate recovery review, and Emily Coach guidance through the private OpenAI backend.

## Week 1 - Connection Stable

- Keep the main product focus on Coach and Data Review.
- Test the real Coach backend call from the Samsung phone using the Profile backend URL.
- Confirm the Samsung phone can reach the Windows backend while Auto Blocker is off.
- Confirm Coach works in both modes:
  - Fake data test mode checked: no OpenAI cost.
  - Fake data test mode unchecked: real backend/OpenAI response.
- Improve the hidden Debug tab so it shows backend reachability, last request payload, last response preview, token usage, and errors.
- Test Health Connect permissions on the live Samsung phone with only some categories granted.
- Confirm Import Today fills steps, sleep, heart rate, resting heart rate, HRV, SpO2, workouts, active calories, and weight when those permissions are granted.
- Improve the Health Connect card message so it lists which selected categories are granted and which still need permission.
- Make empty data states cleaner when Health Connect has permission but no data for today.
- Compare Emily Review against Troy's phone health app for steps, sleep, resting HR, HRV, heart range, SpO2, and active zone minutes.

## Week 2 - Coach Quality

- Treat Emily Coach as the main app experience after Health Connect import.
- Make Emily Coach explain HRV plus resting heart rate recovery trends before other data.
- Use local Emily Profile values when comparing workout peak HR to Troy's saved caution/goal values.
- Refine the Profile tab after Troy tests AICD HR caution limit, workout HR goal, resting HR baseline, HRV baseline, and health notes.
- Define the exact suggestion format Emily should display in the ChatGPT coach response card.
- Keep suggested coach questions available so Troy can tap common questions without remembering what to ask.
- Keep the coach language non-medical: summarize, explain patterns, ask questions, and suggest small wellness steps.

## Week 3 - Data Review Polish

- Make the Review tab easier to scan, with recovery data grouped before lower-priority metrics.
- Tighten mobile layouts so long labels cannot wrap awkwardly inside small boxes.
- Make workout rows show workout type, minutes, and highest heart rate for each workout session.
- Add a manual refresh timestamp after data import.
- Consider showing sleep as hours/minutes, for example `7h 31m`, to match the health app display.
- Store imported snapshots by date so trends are easier to review later.
- Add optional charts for steps, sleep, resting heart rate, HRV, and workouts only after the list views are stable.

## Week 4 - Save, Test, Release

- Add a simple settings screen for app version, Health Connect status, and privacy notes.
- Add screenshots after the app UI settles.
- Add a privacy policy before any public release.
- Create a signed release build only when the app is ready to share outside local testing.
- Save the next stable checkpoint as Version 3.
- Push the final month checkpoint to GitHub with a version tag.

## Later Data Upgrades

- Treat imported health data as review material for Emily Coach, not as manual form entry.
- Move saved check-ins from `SharedPreferences` to Room or DataStore.
- Add blood pressure, respiratory rate, nutrition, or other Health Connect types only after the basic flow is stable.

## Always

- Keep the private backend as the only place that stores the OpenAI API key.
- Do not commit `.env`, API keys, private health exports, local keystores, or personal screenshots unless Troy explicitly chooses them.
- Keep fake data test mode available so UI testing does not spend OpenAI tokens.
