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
npm run start:local
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

## Railway Deployment Notes

Railway should deploy only this backend folder, not the whole Android project.

1. In Railway, create a new project from GitHub.
2. Choose the GitHub repo `exceptionwhisperer/Emily`.
3. Set the service Root Directory to `/backend`.
4. Add these service variables:

```text
OPENAI_API_KEY=your_private_openai_api_key
OPENAI_MODEL=gpt-5
```

Railway provides `PORT` automatically. The backend uses `process.env.PORT`, so do not hard-code the port in Railway.

After Railway deploys, open:

```text
https://your-railway-domain/health
```

Then put the Railway service URL into Emily's Profile tab as the Coach backend URL.

## Safety Boundary

Emily Coach should summarize and explain the user's own data. It should not diagnose medical conditions, prescribe treatment, or replace a qualified clinician.
