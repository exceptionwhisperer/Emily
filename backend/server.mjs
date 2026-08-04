import http from "node:http";

const PORT = Number(process.env.PORT || 8787);
const OPENAI_API_KEY = process.env.OPENAI_API_KEY;
const OPENAI_MODEL = process.env.OPENAI_MODEL || "gpt-5.6-terra";

const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Methods": "GET, POST, OPTIONS",
  "Access-Control-Allow-Headers": "Content-Type, Authorization"
};

const coachInstructions = `
You are Emily Coach, a supportive health-tracking assistant.

Use only the user's supplied health data. Explain patterns in plain language.
Keep the tone calm, practical, and non-judgmental.

Do not diagnose, prescribe, or replace a medical professional.
If symptoms sound urgent, unusual, or potentially serious, recommend contacting
a qualified clinician or emergency services.

Return this exact structure:
1. Health number summary
2. Notable patterns
3. One possible reason to watch
4. Two small next steps
5. One follow-up question
`.trim();

const server = http.createServer(async (request, response) => {
  try {
    if (request.method === "OPTIONS") {
      sendJson(response, 204, {});
      return;
    }

    if (request.method === "GET" && request.url === "/health") {
      sendJson(response, 200, {
        ok: true,
        service: "emily-coach-backend",
        model: OPENAI_MODEL,
        hasOpenAiKey: Boolean(OPENAI_API_KEY)
      });
      return;
    }

    if (request.method === "POST" && request.url === "/api/coach") {
      if (!OPENAI_API_KEY) {
        sendJson(response, 500, {
          error: "OPENAI_API_KEY is not set on the backend."
        });
        return;
      }

      const body = await readJsonBody(request);
      const healthSummary = normalizeHealthSummary(body);

      if (!healthSummary) {
        sendJson(response, 400, {
          error: "Send a JSON body with healthSummary text or a healthSummaryPayload object."
        });
        return;
      }

      const openAiResponse = await fetch("https://api.openai.com/v1/responses", {
        method: "POST",
        headers: {
          "Authorization": `Bearer ${OPENAI_API_KEY}`,
          "Content-Type": "application/json"
        },
        body: JSON.stringify({
          model: OPENAI_MODEL,
          reasoning: {
            effort: "low"
          },
          max_output_tokens: 900,
          input: [
            {
              role: "developer",
              content: [
                {
                  type: "input_text",
                  text: coachInstructions
                }
              ]
            },
            {
              role: "user",
              content: [
                {
                  type: "input_text",
                  text: `User health data:\n${healthSummary}`
                }
              ]
            }
          ]
        })
      });

      const openAiJson = await openAiResponse.json();

      if (!openAiResponse.ok) {
        sendJson(response, openAiResponse.status, {
          error: "OpenAI request failed.",
          detail: openAiJson.error?.message || "Unknown OpenAI API error."
        });
        return;
      }

      sendJson(response, 200, {
        coachText: extractOutputText(openAiJson),
        model: OPENAI_MODEL,
        responseId: openAiJson.id || null,
        usage: normalizeUsage(openAiJson.usage)
      });
      return;
    }

    sendJson(response, 404, {
      error: "Route not found.",
      routes: ["GET /health", "POST /api/coach"]
    });
  } catch (error) {
    sendJson(response, 500, {
      error: "Emily Coach backend failed.",
      detail: error instanceof Error ? error.message : "Unknown error."
    });
  }
});

server.listen(PORT, () => {
  console.log(`Emily Coach backend running at http://localhost:${PORT}`);
});

function sendJson(response, statusCode, payload) {
  response.writeHead(statusCode, {
    "Content-Type": "application/json",
    ...corsHeaders
  });

  if (statusCode === 204) {
    response.end();
    return;
  }

  response.end(JSON.stringify(payload, null, 2));
}

function readJsonBody(request) {
  return new Promise((resolve, reject) => {
    let rawBody = "";

    request.on("data", (chunk) => {
      rawBody += chunk;
      if (rawBody.length > 100_000) {
        request.destroy();
        reject(new Error("Request body is too large."));
      }
    });

    request.on("end", () => {
      if (!rawBody.trim()) {
        resolve({});
        return;
      }

      try {
        resolve(JSON.parse(rawBody));
      } catch {
        reject(new Error("Request body must be valid JSON."));
      }
    });

    request.on("error", reject);
  });
}

function normalizeHealthSummary(body) {
  if (typeof body.healthSummary === "string" && body.healthSummary.trim()) {
    return body.healthSummary.trim();
  }

  if (body.healthSummaryPayload && typeof body.healthSummaryPayload === "object") {
    return JSON.stringify(body.healthSummaryPayload, null, 2);
  }

  return "";
}

function extractOutputText(openAiJson) {
  if (typeof openAiJson.output_text === "string") {
    return openAiJson.output_text;
  }

  const textParts = [];
  for (const item of openAiJson.output || []) {
    for (const content of item.content || []) {
      if (content.type === "output_text" && typeof content.text === "string") {
        textParts.push(content.text);
      }
    }
  }

  return textParts.join("\n").trim();
}

function normalizeUsage(usage) {
  if (!usage || typeof usage !== "object") {
    return {
      inputTokens: null,
      outputTokens: null,
      totalTokens: null
    };
  }

  return {
    inputTokens: usage.input_tokens ?? null,
    outputTokens: usage.output_tokens ?? null,
    totalTokens: usage.total_tokens ?? null
  };
}
