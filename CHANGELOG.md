# Emily Changelog

## 2.0.0 (2) - Emily Coach Backend Test Build

Saved on August 4, 2026.

### August 18, 2026 Progress Save

- Added a Review refresh card with loading state so Health Connect imports show when Emily is retrieving data.
- Moved fake-data testing controls into the hidden Debug tab so Home stays focused on Connect, Manage, and Import Today.
- Added missing Health Connect permission declarations for HRV and blood oxygen.
- Added Blood oxygen / SpO2 import, Review display, fake test data, and Coach payload support.
- Changed sleep import to count actual sleep stages when available instead of full session time.
- Fixed step imports so steps stay tied to today's calendar day instead of using the recent fallback window.
- Kept the recent fallback only for sleep/recovery-style data that may sync across midnight.
- Updated the Android Gradle plugin and Gradle wrapper versions used by the current Android Studio setup.

### Latest Milestone

- Wired the Android Coach screen to call the private Emily backend when fake data test mode is unchecked.
- Added an editable backend URL field in Profile so the phone can point to the Windows backend.
- Added Android internet permission for the Coach backend call.
- Kept the OpenAI API key out of the Android app and GitHub by using `backend/.env`.
- Updated the backend to return OpenAI token usage for Coach cost tracking.
- Updated Debug to show the backend URL, backend status, Coach mode, and token counters.
- Confirmed the backend can call OpenAI successfully with `gpt-5`.
- Marked the app as Android version `2.0.0 (2)`.

### Notes

- Phone test backend URL on Troy's current network: `http://192.168.68.66:8787`.
- Fake data test mode remains available for free UI testing.
- This is still a local/private test build, not a Play Store release.

## 1.0.0 (1) - Local Android Test Build

Saved on July 29, 2026.

### Latest Milestone

- Added fake data test mode for Emily Coach so the coach flow can be tested without OpenAI billing.
- Added suggested coach questions and a custom question field.
- Added session usage tracking for coach requests, estimated input tokens, estimated output tokens, and cost display.
- Added fake coach responses that use the current health numbers and trend summary.
- Changed trend and coach response text into scannable bullet lists.
- Made the Trend heading larger and bold for easier reading.
- Added bottom navigation sections for Home, Data, Coach, and Trend.
- Reduced the Home page to the app header plus Health Connect connect/manage/import controls.
- Added a Home testing card that fills Emily with fake sample data without Health Connect or OpenAI.
- Expanded heart data usage to import latest, low, high, and sample count alongside average and resting heart rate.
- Added HRV recovery tracking from Health Connect with today's latest HRV, 7-day average, sample count, and change text.
- Updated Emily Coach to explain the HRV plus resting heart rate recovery trend first.
- Refocused the app direction around Coach and Data Review, including renaming the Data tab to Review.
- Reworked the Review tab to show recovery first and hide empty Workout/Weight cards until data exists.
- Tightened Review row spacing so `No data` values are smaller, calmer, and better aligned.
- Fixed long Review values so rows stack instead of crushing labels on narrow phone screens.
- Shortened the HRV recovery row label to prevent vertical wrapping on phone screens.
- Removed duplicate Sleep and Movement detail rows so those cards show the imported value once.
- Moved long card header summaries under the title so Recovery review spacing stays readable.
- Left-aligned stacked Review values so HRV change reads like a clear statement.
- Right-aligned stacked numeric Review values while keeping long text Review values left-aligned.
- Added workout session review rows with workout type, minutes, and highest heart rate during each session.
- Changed suggested Emily Coach questions to load into the question box before sending.
- Added a local Emily Profile tab for AICD HR caution limit, workout HR goal, resting HR baseline, HRV baseline, and health context notes.
- Added a hidden Debug tab unlocked by tapping the version footer six times for future OpenAI pipeline debugging.

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
