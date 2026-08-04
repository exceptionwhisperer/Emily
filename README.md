# Emily

Emily is a native Android health data review and coach app built with Kotlin and Jetpack Compose.

The main focus is **Coach** and **Data Review**. Emily imports selected Health Connect data, reviews the numbers for trend context, then prepares coach-style guidance with special attention on HRV and resting heart rate recovery changes.

Current version: `2.0.0 (2)`

See `CHANGELOG.md` for saved version notes and `TODO.md` for the current build list.

## How Emily Works

Emily has two kinds of data:

- **Imported Health Connect data:** sleep, steps, heart rate, HRV, resting heart rate, active calories, workout minutes, workout types, and weight.
- **Manual check-in data:** mood, symptoms, medications, and notes.

Use the app in this order:

1. Tap **Review > Data to use > Change** and choose which Health Connect data Emily should use.
2. Tap **Connect** only when setting up permissions for the first time or after changing selected data types.
3. Tap **Import Today** whenever you want to refresh today's Health Connect numbers.
4. Fill in manual check-in items like mood, symptoms, medications, and notes.
5. Use **Profile** to save personal context Emily Coach should remember locally.
6. Tap **Create Coach Summary** to generate Emily's coach-style summary.
7. Tap **Save Today's Check-In** when the day's snapshot looks right.

You do **not** need to tap **Connect** every time. Once Health Connect permissions are granted, **Import Today** is the normal refresh button. If you grant only some selected permissions, Emily imports only the granted data types instead of blocking the whole import.

## What It Tracks

- Daily wellness score
- Sleep
- Steps
- Mood
- Symptoms
- Medication notes
- Personal health notes
- Health Connect imports for steps, sleep, heart rate, HRV, resting heart rate, active calories, exercise minutes, workout types, and weight
- A data-selection card to choose exactly which Health Connect data Emily imports and uses for coaching
- 7-day trend summaries for coaching context, including HRV and resting heart rate recovery changes
- Emily Coach summaries that prepare the user's data for ChatGPT-style guidance

## How To Open It

1. Open Android Studio.
2. Choose **Open**.
3. Select this folder: `C:\Users\Dad\Documents\Emily 2`.
4. Let Gradle sync finish.
5. Press **Run**.

## Beginner Notes

- The main screen lives in `app/src/main/java/com/emily/healthtracker/MainActivity.kt`.
- App name text lives in `app/src/main/res/values/strings.xml`.
- The Android setup lives in `app/build.gradle.kts`.

Right now, check-ins are saved locally with Android `SharedPreferences`. A good later upgrade is Room or DataStore if the app grows.

## Using A Second Computer

After this project is pushed to GitHub:

1. Install Android Studio on the second computer.
2. Clone the GitHub repository.
3. Open the cloned folder in Android Studio.
4. Let Gradle sync finish.
5. Connect an Android phone with Health Connect.
6. Press **Run**.

The app source is safe to store in GitHub. Do not commit personal health exports, API keys, local keystores, or private backend secrets.

If this local checkout has no GitHub remote yet, create an empty GitHub repository first, then add its repository URL as the `origin` remote before pushing.

## Health Connect

Emily can request permission to read steps, sleep, heart rate, HRV, resting heart rate, active calories, exercise sessions, workout types, and weight from Android Health Connect.

For testing:

1. Run Emily on a device or emulator where Health Connect is available.
2. Make sure another app has written steps or sleep data into Health Connect.
3. Use **Review > Data to use** to check or uncheck the health data Emily should import.
4. Tap **Connect** inside Emily and grant the requested health permissions.
5. Tap **Import Today** to pull selected data and update the 7-day trend card.

Health Connect only gives Emily data after the user grants permission.

## Emily Coach

Emily now includes a coach card that summarizes the current health number, imported data, symptoms, medication notes, recent check-ins, and the HRV plus resting heart rate recovery trend.

The first private backend is in `backend/`. The Android app stores only the backend URL, while the OpenAI API key stays in `backend/.env` on the development computer. See `docs/AI_COACH_PLAN.md` and `backend/README.md`.

## Emily Profile

The Profile tab stores local context such as AICD high HR caution limit, workout HR goal, resting HR baseline, HRV baseline, and health notes. Emily Coach uses this local profile when explaining imported data. These profile values are entered by the user; they are not read from hidden Google Health or Fitbit settings.
