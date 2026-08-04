# Emily Changelog

## 1.0.0 (1) - Local Android Test Build

Saved on July 29, 2026.

### Latest Milestone

- Added fake data test mode for Emily Coach so the coach flow can be tested without OpenAI billing.
- Added suggested coach questions and a custom question field.
- Added session usage tracking for coach requests, estimated input tokens, estimated output tokens, and cost display.
- Added fake coach responses that use the current health numbers and trend summary.
- Changed trend and coach response text into scannable bullet lists.
- Made the Trend heading larger and bold for easier reading.

### Added

- Native Android starter app built with Kotlin and Jetpack Compose.
- Health Connect import flow for selected health data.
- Data selection card for steps, sleep, heart rate, active calories, workouts, and weight.
- Imported data cards for selected categories only.
- Resting heart rate display and 7-day resting heart rate trend text.
- Workout minutes and workout type summaries from Health Connect exercise sessions.
- Local Emily Coach summary that prepares the user's health data for ChatGPT-style guidance.
- App version footer using Android `BuildConfig`.
- Custom Emily app icon.

### Changed

- Removed hydration from the app and Health Connect permission requests.
- Import Today now stays available when Health Connect is installed and data is selected.
- Import Today checks current Health Connect permissions when tapped and imports only granted selected data.
- Unchecked data categories are hidden from the screen.

### Notes

- App package: `com.emily.healthtracker`.
- Android version: `versionName = "1.0.0"`, `versionCode = 1`.
- Current branch: `master`.
- This is a local test build, not a Play Store release.
- Personal health data is stored locally on the phone using Android `SharedPreferences`.
