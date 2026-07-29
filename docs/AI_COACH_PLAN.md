# Emily AI Coach Plan

Emily should use the user's own Health Connect data to create plain-language health summaries, similar in spirit to a premium health coach feature, without copying another app's exact product, branding, or private logic.

## What Emily Can Analyze

- Steps
- Sleep duration
- Heart rate
- Resting heart rate
- Active calories
- Exercise minutes
- Workout types
- Weight
- Mood
- Symptoms
- Medication notes
- Saved check-in trends

Later additions can include resting heart rate, blood pressure, oxygen saturation, menstrual cycle data, and nutrition if the user grants those Health Connect permissions.

## Safe Product Boundary

Emily can:

- Explain patterns in the user's own data.
- Compare different workout types with sleep, heart rate, mood, and recovery signals.
- Show when today's resting heart rate is above or below the recent baseline.
- Respect the user's selected data types when building coach summaries.
- Point out changes from normal.
- Ask useful follow-up questions.
- Suggest small wellness habits.
- Encourage the user to contact a clinician for concerning symptoms.

Emily should not:

- Diagnose medical conditions.
- Tell the user to start, stop, or change medication.
- Claim emergency certainty.
- Pretend to be Google, Fitbit, or a licensed clinician.

## Recommended ChatGPT Architecture

Do not put an OpenAI API key directly in the Android app. Mobile apps can be inspected, and a key inside the app can be stolen.

Use this flow instead:

1. Emily reads approved data from Android Health Connect.
2. Emily creates a compact health summary payload.
3. Emily sends that payload to a private backend owned by the user.
4. The backend calls the OpenAI Responses API.
5. The backend sends the coach response back to Emily.

## Draft AI Prompt

```text
You are Emily Coach, a supportive health-tracking assistant.

Use only the user's supplied health data. Explain patterns in plain language. Keep the tone calm, practical, and non-judgmental.

Do not diagnose, prescribe, or replace a medical professional. If symptoms sound urgent or unusual, recommend contacting a qualified clinician or emergency services.

Return:
1. Health number summary
2. Notable patterns
3. One possible reason to watch
4. Two small next steps
5. One follow-up question

User health data:
{health_summary_payload}
```

## First Implementation Step

The current Android app includes Health Connect imports, a 7-day trends card, and an Emily Coach card that creates a local ChatGPT-ready summary from the user's current check-in data.

The first backend now lives in `backend/`. It exposes:

- `GET /health` to confirm the backend is running.
- `POST /api/coach` to send a compact health summary to the OpenAI Responses API.

The next implementation step is wiring the Android app's **Create Coach Summary** button to call this backend instead of only creating a local prompt.

The Android app now has a dedicated **ChatGPT coach response** card. That card should display the backend's plain-language response plus the small next-step suggestions that Emily Coach returns.
