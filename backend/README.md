# Emily Coach Backend

This is Emily's private AI assistant backend. The Android app should send the user's compact health summary here, and this backend calls the OpenAI Responses API.

The API key belongs on this backend, not inside the Android app.

## Requirements

- Node.js 20 or newer
- An OpenAI API key

## Setup

1. Copy `.env.example` to `.env`.
2. Put your OpenAI API key in `.env`.
3. Start the server:

```powershell
cd "C:\Users\Dad\Documents\Emily 2\backend"
npm start
```

The backend runs at:

```text
http://localhost:8787
```

## Test The Backend

Health check:

```powershell
Invoke-RestMethod -Uri "http://localhost:8787/health"
```

Coach test:

```powershell
Invoke-RestMethod `
  -Uri "http://localhost:8787/api/coach" `
  -Method POST `
  -ContentType "application/json" `
  -Body '{"healthSummary":"Score 82. Sleep 7.1 hours. Steps 10020. Average heart rate 81 bpm. Resting heart rate 69 bpm. Workout: Walking x5. Mood 8/10."}'
```

## Android Notes

- Android emulator can usually reach this backend at `http://10.0.2.2:8787`.
- A physical phone needs the computer's local network IP address, such as `http://192.168.1.25:8787`.
- Windows Firewall may ask for permission the first time the backend is run.

## Safety Boundary

Emily Coach should summarize and explain the user's own data. It should not diagnose medical conditions, prescribe treatment, or replace a qualified clinician.
