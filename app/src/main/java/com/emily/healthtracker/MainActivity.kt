package com.emily.healthtracker

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.TextButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ActiveCaloriesBurnedRecord
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.HeartRateVariabilityRmssdRecord
import androidx.health.connect.client.records.RestingHeartRateRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.WeightRecord
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import org.json.JSONArray
import org.json.JSONObject

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EmilyApp()
        }
    }
}

class PermissionsRationaleActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EmilyPermissionsRationaleScreen()
        }
    }
}

@Composable
fun EmilyApp() {
    MaterialTheme(
        colorScheme = MaterialTheme.colorScheme.copy(
            primary = Teal,
            secondary = Coral,
            background = Cream,
            surface = Color.White,
            onPrimary = Color.White,
            onSecondary = Charcoal,
            onBackground = Charcoal,
            onSurface = Charcoal
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Cream
        ) {
            HealthTrackerScreen()
        }
    }
}

@Composable
private fun EmilyPermissionsRationaleScreen() {
    MaterialTheme(
        colorScheme = MaterialTheme.colorScheme.copy(
            primary = Teal,
            background = Cream,
            surface = Color.White,
            onBackground = Charcoal,
            onSurface = Charcoal
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Cream
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Text(
                        text = "Emily Health Data Use",
                        color = Charcoal,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                item {
                    Text(
                        text = "Emily uses only the health data you choose so it can show your numbers, summarize trends, and prepare coach-style guidance for you.",
                        color = Charcoal.copy(alpha = 0.78f)
                    )
                }
                item {
                    MetricCard(title = "Data Emily can read", value = "User selected") {
                        Text("Steps, sleep, heart rate, HRV, resting heart rate, active calories, workout sessions, workout types, and weight may be used only after you grant permission.")
                    }
                }
                item {
                    MetricCard(title = "How Emily uses it", value = "For coaching") {
                        Text("Emily combines your selected health data with your check-ins to explain patterns and suggest small non-medical next steps.")
                    }
                }
                item {
                    MetricCard(title = "Your control", value = "Always") {
                        Text("You can uncheck data types inside Emily and revoke Health Connect permissions in Android settings at any time.")
                    }
                }
                item {
                    Text(
                        text = "Emily does not diagnose medical conditions, prescribe treatment, or replace a qualified clinician.",
                        color = Coral,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun HealthTrackerScreen() {
    var sleepHours by remember { mutableStateOf("7.5") }
    var steps by remember { mutableStateOf("4200") }
    var heartRate by remember { mutableStateOf("") }
    var restingHeartRate by remember { mutableStateOf("") }
    var latestHeartRate by remember { mutableStateOf("") }
    var minHeartRate by remember { mutableStateOf("") }
    var maxHeartRate by remember { mutableStateOf("") }
    var heartRateSamples by remember { mutableStateOf("") }
    var hrvToday by remember { mutableStateOf("") }
    var hrvWeekAverage by remember { mutableStateOf("") }
    var hrvChange by remember { mutableStateOf("") }
    var hrvSamples by remember { mutableStateOf("") }
    var activeCalories by remember { mutableStateOf("") }
    var exerciseMinutes by remember { mutableStateOf("") }
    var workoutTypes by remember { mutableStateOf("No workout types imported yet.") }
    var weightPounds by remember { mutableStateOf("") }
    var mood by remember { mutableStateOf(7f) }
    var symptoms by remember { mutableStateOf("") }
    var medications by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var trendSummary by remember { mutableStateOf("7-day trends will appear after importing Health Connect data.") }
    var coachInsight by remember { mutableStateOf("Import or enter today's numbers, then ask Emily Coach for a plain-language summary.") }
    var chatGptCoachResponse by remember { mutableStateOf("") }
    var chatGptSuggestions by remember { mutableStateOf(listOf<String>()) }
    var coachQuestion by remember { mutableStateOf("") }
    var lastCoachQuestion by remember { mutableStateOf("") }
    var fakeCoachMode by remember { mutableStateOf(true) }
    var coachRequestCount by remember { mutableStateOf(0) }
    var coachInputTokens by remember { mutableStateOf(0) }
    var coachOutputTokens by remember { mutableStateOf(0) }
    var includeSteps by remember { mutableStateOf(true) }
    var includeSleep by remember { mutableStateOf(true) }
    var includeHeartRate by remember { mutableStateOf(true) }
    var includeActiveCalories by remember { mutableStateOf(true) }
    var includeWorkouts by remember { mutableStateOf(true) }
    var includeWeight by remember { mutableStateOf(false) }
    var isDataSelectionExpanded by remember { mutableStateOf(false) }
    var selectedSection by remember { mutableStateOf(AppSection.Home) }
    val entries = remember { mutableStateListOf<HealthEntry>() }
    val context = androidx.compose.ui.platform.LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val preferences = remember {
        context.getSharedPreferences("emily_health_entries", Context.MODE_PRIVATE)
    }
    val healthConnectStatus = remember { HealthConnectClient.getSdkStatus(context) }
    val healthConnectClient = remember(healthConnectStatus) {
        if (healthConnectStatus == HealthConnectClient.SDK_AVAILABLE) {
            HealthConnectClient.getOrCreate(context)
        } else {
            null
        }
    }
    val selectedHealthData = SelectedHealthData(
        includeSteps = includeSteps,
        includeSleep = includeSleep,
        includeHeartRate = includeHeartRate,
        includeActiveCalories = includeActiveCalories,
        includeWorkouts = includeWorkouts,
        includeWeight = includeWeight
    )
    val healthConnectPermissions = selectedHealthData.permissions()
    val hasSelectedData = selectedHealthData.hasAnyHealthConnectImport()
    val hasWorkoutReviewData = exerciseMinutes.isNotBlank() ||
        activeCalories.isNotBlank() ||
        (
            workoutTypes.isNotBlank() &&
                workoutTypes != "No workout types imported yet." &&
                workoutTypes != "Workout data not selected"
            )
    val hasWeightReviewData = weightPounds.isNotBlank()
    var requestedPermissions by remember { mutableStateOf(emptySet<String>()) }
    var grantedHealthPermissions by remember { mutableStateOf(emptySet<String>()) }
    var hasHealthConnectPermission by remember { mutableStateOf(false) }
    fun updatePermissionState(grantedPermissions: Set<String>) {
        grantedHealthPermissions = grantedPermissions
        hasHealthConnectPermission = healthConnectPermissions.isNotEmpty() &&
            healthConnectPermissions.any { permission -> permission in grantedPermissions }
    }
    var healthConnectMessage by remember {
        mutableStateOf(
            when (healthConnectStatus) {
                HealthConnectClient.SDK_AVAILABLE -> "Health Connect is available."
                HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED ->
                    "Health Connect needs to be installed or updated on this device."
                else -> "Health Connect is not available on this device."
            }
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = PermissionController.createRequestPermissionResultContract()
    ) { grantedPermissions ->
        updatePermissionState(grantedPermissions)
        val grantedSelectedCount = requestedPermissions.count { permission -> permission in grantedPermissions }
        healthConnectMessage = if (grantedSelectedCount == requestedPermissions.size && requestedPermissions.isNotEmpty()) {
            "Health Connect is connected for the selected data."
        } else if (grantedSelectedCount > 0) {
            "Health Connect is connected for $grantedSelectedCount selected permission(s). Import Today will use the granted items."
        } else {
            "No selected Health Connect permissions were granted yet. Tap Connect or Manage to choose data."
        }
    }

    LaunchedEffect(Unit) {
        entries.addAll(loadHealthEntries(preferences))
    }

    LaunchedEffect(healthConnectClient, healthConnectPermissions) {
        healthConnectClient?.let { client ->
            val grantedPermissions = client.permissionController.getGrantedPermissions()
            updatePermissionState(grantedPermissions)
        }
    }

    val wellnessScore = calculateWellnessScore(
        sleepHours = sleepHours.toFloatOrNull() ?: 0f,
        steps = steps.toIntOrNull() ?: 0,
        mood = mood
    )
    fun currentCoachPayload(): String {
        return buildCoachInsight(
            wellnessScore = wellnessScore,
            sleepHours = sleepHours,
            steps = steps,
            mood = mood.roundToInt(),
            symptoms = symptoms,
            medications = medications,
            notes = notes,
            trendSummary = trendSummary,
            heartRate = heartRate,
            restingHeartRate = restingHeartRate,
            latestHeartRate = latestHeartRate,
            minHeartRate = minHeartRate,
            maxHeartRate = maxHeartRate,
            heartRateSamples = heartRateSamples,
            hrvToday = hrvToday,
            hrvWeekAverage = hrvWeekAverage,
            hrvChange = hrvChange,
            hrvSamples = hrvSamples,
            activeCalories = activeCalories,
            exerciseMinutes = exerciseMinutes,
            workoutTypes = workoutTypes,
            weightPounds = weightPounds,
            selectedHealthData = selectedHealthData,
            recentEntries = entries.take(5)
        )
    }
    fun askCoach(question: String) {
        val cleanQuestion = question.ifBlank { "What trends do you see in my health numbers today?" }
        val payload = currentCoachPayload()
        coachInsight = payload
        lastCoachQuestion = cleanQuestion
        coachRequestCount += 1

        if (fakeCoachMode) {
            val fakeResponse = buildFakeCoachResponse(
                question = cleanQuestion,
                wellnessScore = wellnessScore,
                sleepHours = sleepHours,
                steps = steps,
                heartRate = heartRate,
                restingHeartRate = restingHeartRate,
                latestHeartRate = latestHeartRate,
                minHeartRate = minHeartRate,
                maxHeartRate = maxHeartRate,
                heartRateSamples = heartRateSamples,
                hrvToday = hrvToday,
                hrvWeekAverage = hrvWeekAverage,
                hrvChange = hrvChange,
                hrvSamples = hrvSamples,
                exerciseMinutes = exerciseMinutes,
                workoutTypes = workoutTypes,
                mood = mood.roundToInt(),
                trendSummary = trendSummary
            )
            chatGptCoachResponse = fakeResponse.response
            chatGptSuggestions = fakeResponse.suggestions
            coachInputTokens += estimateTokens(payload)
            coachOutputTokens += estimateTokens(fakeResponse.response + fakeResponse.suggestions.joinToString())
        } else {
            chatGptCoachResponse = "Backend mode is selected, but the Android-to-backend connection is not wired yet. Switch Fake data test mode back on for free testing."
            chatGptSuggestions = listOf(
                "Start the backend after adding your API key.",
                "Wire this button to POST /api/coach.",
                "Save token usage from the backend response."
            )
        }
    }
    fun fillFakeHealthData() {
        sleepHours = "7.4"
        steps = "9420"
        heartRate = "78"
        restingHeartRate = "67"
        latestHeartRate = "74"
        minHeartRate = "58"
        maxHeartRate = "132"
        heartRateSamples = "96"
        hrvToday = "42.6"
        hrvWeekAverage = "38.2"
        hrvChange = "4.4 above 7-day average"
        hrvSamples = "5"
        activeCalories = "426"
        exerciseMinutes = "48"
        workoutTypes = "Walking x3, Strength training x1"
        weightPounds = "168.4"
        mood = 8f
        symptoms = "Mild shoulder tightness after workout."
        medications = "Morning vitamins logged."
        notes = "Good energy today. Testing Emily with fake sample data."
        trendSummary = "7-day trend: 8120 steps/day, 7.1 sleep hours/day, 38 exercise min/day, 390 active calories/day, 76 bpm average heart rate, 68 bpm resting heart rate, heart range 58-132 bpm, 96 heart samples, HRV 38.2 ms 7-day average. Today's resting heart rate is 67 bpm, 1 below your 7-day average. Latest HRV today is 42.6 ms, 4.4 above your 7-day average. Latest heart rate today is 74 bpm. Workout types this week: Walking x3, Strength training x1. Today: 9420 steps and 7.4 sleep hours."
        coachInsight = "Fake health data has been filled in. Go to Review, Coach, or Trend to test the cards."
        chatGptCoachResponse = "Fake Emily Coach response is ready. This did not call OpenAI and did not use tokens."
        chatGptSuggestions = listOf(
            "Check the Trend tab to see the fake 7-day summary.",
            "Use the Coach tab to ask a fake-mode question.",
            "Keep Fake data test mode checked to avoid OpenAI cost."
        )
        fakeCoachMode = true
        healthConnectMessage = "Fake test data filled. Health Connect was not used for this sample."
    }

    Scaffold(
        containerColor = Cream,
        bottomBar = {
            EmilyBottomNavigation(
                selectedSection = selectedSection,
                onSectionSelected = { selectedSection = it }
            )
        }
    ) { contentPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Cream)
                .padding(contentPadding)
                .padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Header(wellnessScore = wellnessScore)
        }

        if (selectedSection == AppSection.Home) {
            item {
                HealthConnectCard(
                    statusText = healthConnectMessage,
                    isAvailable = healthConnectClient != null,
                    hasPermission = hasHealthConnectPermission,
                    hasSelectedData = hasSelectedData,
                    onRequestPermission = {
                        try {
                            requestedPermissions = healthConnectPermissions
                            permissionLauncher.launch(healthConnectPermissions)
                        } catch (exception: Exception) {
                            healthConnectMessage =
                                "Health Connect could not open permissions: ${exception.message ?: "unknown issue"}"
                        }
                    },
                    onManageAccess = {
                        try {
                            context.startActivity(Intent(HealthConnectClient.ACTION_HEALTH_CONNECT_SETTINGS))
                        } catch (exception: Exception) {
                            healthConnectMessage =
                                "Emily could not open Health Connect settings: ${exception.message ?: "unknown issue"}"
                        }
                    },
                    onImportToday = {
                        val client = healthConnectClient ?: return@HealthConnectCard
                        coroutineScope.launch {
                            try {
                                val currentGrantedPermissions = client.permissionController.getGrantedPermissions()
                                updatePermissionState(currentGrantedPermissions)
                                val importableData = selectedHealthData.withGrantedPermissions(currentGrantedPermissions)
                                if (!importableData.hasAnyHealthConnectImport()) {
                                    healthConnectMessage = "No selected Health Connect permissions are granted yet. Tap Connect or Manage first."
                                    return@launch
                                }
                                val importedData = readHealthConnectData(client, importableData)
                                if (importableData.includeSteps) {
                                    steps = importedData.today.steps?.toString().orEmpty()
                                }
                                if (importableData.includeSleep) {
                                    sleepHours = importedData.today.sleepMinutes?.let { formatHours(it) }.orEmpty()
                                }
                                if (importableData.includeHeartRate) {
                                    heartRate = importedData.today.averageHeartRate?.toString().orEmpty()
                                    restingHeartRate = importedData.today.restingHeartRate?.toString().orEmpty()
                                    latestHeartRate = importedData.today.latestHeartRate?.toString().orEmpty()
                                    minHeartRate = importedData.today.minHeartRate?.toString().orEmpty()
                                    maxHeartRate = importedData.today.maxHeartRate?.toString().orEmpty()
                                    heartRateSamples = importedData.today.heartRateSampleCount
                                        ?.takeIf { it > 0 }
                                        ?.toString()
                                        .orEmpty()
                                    hrvToday = importedData.today.latestHrvMillis?.let { formatOneDecimal(it) }.orEmpty()
                                    hrvWeekAverage = importedData.weekAverageHrvMillis?.let { formatOneDecimal(it) }.orEmpty()
                                    hrvChange = hrvChangeText(
                                        todayHrvMillis = importedData.today.latestHrvMillis,
                                        weekAverageHrvMillis = importedData.weekAverageHrvMillis
                                    )
                                    hrvSamples = importedData.today.hrvSampleCount
                                        ?.takeIf { it > 0 }
                                        ?.toString()
                                        .orEmpty()
                                }
                                if (importableData.includeActiveCalories) {
                                    activeCalories = importedData.today.activeCalories
                                        ?.roundToInt()
                                        ?.takeIf { it > 0 }
                                        ?.toString()
                                        .orEmpty()
                                }
                                if (importableData.includeWorkouts) {
                                    exerciseMinutes = importedData.today.exerciseMinutes?.toString().orEmpty()
                                    workoutTypes = importedData.today.workoutTypesSummary
                                }
                                if (importableData.includeWeight) {
                                    weightPounds = importedData.latestWeightPounds?.let { formatOneDecimal(it) }.orEmpty()
                                }
                                trendSummary = importedData.trendSummary
                                healthConnectMessage = "Imported granted Health Connect data and updated 7-day trends."
                            } catch (exception: Exception) {
                                healthConnectMessage =
                                    "Emily could not import Health Connect data yet: ${exception.message ?: "unknown issue"}"
                            }
                        }
                    }
                )
            }
            item {
                FakeDataTestCard(
                    onFillFakeData = { fillFakeHealthData() }
                )
            }
        }

        if (selectedSection == AppSection.Data) {
            item {
            DataSelectionCard(
                includeSteps = includeSteps,
                onIncludeStepsChange = {
                    includeSteps = it
                    if (!it) steps = ""
                },
                includeSleep = includeSleep,
                onIncludeSleepChange = {
                    includeSleep = it
                    if (!it) sleepHours = ""
                },
                includeHeartRate = includeHeartRate,
                onIncludeHeartRateChange = {
                    includeHeartRate = it
                    if (!it) {
                        heartRate = ""
                        restingHeartRate = ""
                        latestHeartRate = ""
                        minHeartRate = ""
                        maxHeartRate = ""
                        heartRateSamples = ""
                        hrvToday = ""
                        hrvWeekAverage = ""
                        hrvChange = ""
                        hrvSamples = ""
                    }
                },
                includeActiveCalories = includeActiveCalories,
                onIncludeActiveCaloriesChange = {
                    includeActiveCalories = it
                    if (!it) activeCalories = ""
                },
                includeWorkouts = includeWorkouts,
                onIncludeWorkoutsChange = {
                    includeWorkouts = it
                    if (!it) {
                        exerciseMinutes = ""
                        workoutTypes = "Workout data not selected"
                    }
                },
                includeWeight = includeWeight,
                onIncludeWeightChange = {
                    includeWeight = it
                    if (!it) weightPounds = ""
                },
                isExpanded = isDataSelectionExpanded,
                onToggleExpanded = { isDataSelectionExpanded = !isDataSelectionExpanded }
            )
            }
        }

        if (selectedSection == AppSection.Data && includeHeartRate) {
            item {
                MetricCard(
                    title = "Recovery review",
                    value = recoveryReviewSummary(hrvToday, hrvWeekAverage, restingHeartRate)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ImportedDataRow(
                            label = "HRV today",
                            value = hrvToday.ifBlank { "No data" },
                            unit = "ms"
                        )
                        ImportedDataRow(
                            label = "HRV 7-day average",
                            value = hrvWeekAverage.ifBlank { "No data" },
                            unit = "ms"
                        )
                        ImportedDataRow(
                            label = "HRV change",
                            value = hrvChange.ifBlank { "No data" }
                        )
                        ImportedDataRow(
                            label = "Resting heart rate today",
                            value = restingHeartRate.ifBlank { "No data" },
                            unit = "bpm"
                        )
                        ImportedDataRow(
                            label = "Average heart rate",
                            value = heartRate.ifBlank { "No data" },
                            unit = "bpm"
                        )
                        ImportedDataRow(
                            label = "Latest heart rate",
                            value = latestHeartRate.ifBlank { "No data" },
                            unit = "bpm"
                        )
                        ImportedDataRow(
                            label = "Low heart rate today",
                            value = minHeartRate.ifBlank { "No data" },
                            unit = "bpm"
                        )
                        ImportedDataRow(
                            label = "High heart rate today",
                            value = maxHeartRate.ifBlank { "No data" },
                            unit = "bpm"
                        )
                        ImportedDataRow(
                            label = "Heart samples today",
                            value = heartRateSamples.ifBlank { "No data" }
                        )
                        ImportedDataRow(
                            label = "HRV samples today",
                            value = hrvSamples.ifBlank { "No data" }
                        )
                    }
                }
            }
        }

        if (selectedSection == AppSection.Data && includeSleep) {
            item {
                MetricCard(title = "Sleep", value = "${sleepHours.ifBlank { "0" }} hours") {
                    SourceNote("From Health Connect")
                }
            }
        }

        if (selectedSection == AppSection.Data && includeSteps) {
            item {
                MetricCard(title = "Movement", value = "${steps.ifBlank { "0" }} steps") {
                    SourceNote("From Health Connect")
                }
            }
        }

        if (selectedSection == AppSection.Data && (includeWorkouts || includeActiveCalories) && hasWorkoutReviewData) {
            item {
                MetricCard(
                    title = "Workout",
                    value = workoutSummary(includeWorkouts, exerciseMinutes, includeActiveCalories, activeCalories)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (includeWorkouts) {
                            ImportedDataRow(
                                label = "Exercise minutes",
                                value = exerciseMinutes.ifBlank { "No data" },
                                unit = "min"
                            )
                            Text(text = "Types: $workoutTypes", color = Charcoal.copy(alpha = 0.72f))
                        }
                        if (includeActiveCalories) {
                            ImportedDataRow(
                                label = "Active calories",
                                value = activeCalories.ifBlank { "No data" },
                                unit = "cal"
                            )
                        }
                    }
                }
            }
        }

        if (selectedSection == AppSection.Data && includeWeight && hasWeightReviewData) {
            item {
                MetricCard(title = "Weight", value = if (weightPounds.isBlank()) "No data" else "$weightPounds lb") {
                    ImportedDataRow(
                        label = "Latest weight from Health Connect",
                        value = weightPounds.ifBlank { "No data" },
                        unit = "lb"
                    )
                }
            }
        }

        if (selectedSection == AppSection.Data) {
            item {
            MetricCard(title = "Mood", value = "${mood.roundToInt()} / 10") {
                Slider(
                    value = mood,
                    onValueChange = { mood = it },
                    valueRange = 1f..10f,
                    steps = 8
                )
            }
            }
        }

        if (selectedSection == AppSection.Data) {
            item {
            NotesCard(
                symptoms = symptoms,
                onSymptomsChange = { symptoms = it },
                medications = medications,
                onMedicationsChange = { medications = it },
                notes = notes,
                onNotesChange = { notes = it }
            )
            }
        }

        if (selectedSection == AppSection.Trend) {
            item {
            TrendCard(trendSummary = trendSummary)
            }
        }

        if (selectedSection == AppSection.Coach) {
            item {
            EmilyCoachCard(
                insight = coachInsight,
                onGenerateInsight = {
                    coachInsight = currentCoachPayload()
                    chatGptCoachResponse = "Coach summary is ready for ChatGPT. Backend connection is the next step."
                    chatGptSuggestions = listOf(
                        "Use this card for ChatGPT's health-number explanation.",
                        "Show two small next steps here after the backend responds."
                    )
                }
            )
            }
        }

        if (selectedSection == AppSection.Coach) {
            item {
            CoachConversationCard(
                fakeCoachMode = fakeCoachMode,
                onFakeCoachModeChange = { fakeCoachMode = it },
                question = coachQuestion,
                onQuestionChange = { coachQuestion = it },
                lastQuestion = lastCoachQuestion,
                suggestedQuestions = suggestedCoachQuestions,
                onAskQuestion = { selectedQuestion ->
                    askCoach(selectedQuestion)
                    if (selectedQuestion == coachQuestion) {
                        coachQuestion = ""
                    }
                }
            )
            }
        }

        if (selectedSection == AppSection.Coach) {
            item {
            ChatGptCoachResponseCard(
                response = chatGptCoachResponse,
                suggestions = chatGptSuggestions
            )
            }
        }

        if (selectedSection == AppSection.Coach) {
            item {
            CoachUsageCard(
                fakeCoachMode = fakeCoachMode,
                requestCount = coachRequestCount,
                inputTokens = coachInputTokens,
                outputTokens = coachOutputTokens
            )
            }
        }

        if (selectedSection == AppSection.Data) {
            item {
            Button(
                onClick = {
                    entries.add(
                        0,
                        HealthEntry(
                            date = LocalDate.now().format(DateTimeFormatter.ofPattern("MMM d, yyyy")),
                            score = wellnessScore,
                            sleepHours = sleepHours,
                            waterCups = 0,
                            steps = steps,
                            mood = mood.roundToInt(),
                            symptoms = symptoms.ifBlank { "None logged" },
                            medications = medications.ifBlank { "None logged" },
                            notes = notes.ifBlank { "No extra notes" }
                        )
                    )
                    saveHealthEntries(preferences, entries)
                    symptoms = ""
                    medications = ""
                    notes = ""
                },
                colors = ButtonDefaults.buttonColors(containerColor = Teal),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text("Save Today's Check-In", fontWeight = FontWeight.Bold)
            }
            }
        }

        if (selectedSection == AppSection.Data) {
            item {
            Text(
                text = "Recent check-ins",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Charcoal,
                modifier = Modifier.padding(top = 4.dp)
            )
            }
        }

        if (selectedSection == AppSection.Data && entries.isEmpty()) {
            item {
                EmptyState()
            }
        } else if (selectedSection == AppSection.Data) {
            items(entries) { entry ->
                EntryCard(entry = entry)
            }
        }

        item {
            VersionFooter()
            Spacer(modifier = Modifier.height(18.dp))
        }
    }
    }
}

@Composable
private fun EmilyBottomNavigation(
    selectedSection: AppSection,
    onSectionSelected: (AppSection) -> Unit
) {
    NavigationBar(containerColor = Color.White) {
        AppSection.entries.forEach { section ->
            NavigationBarItem(
                selected = selectedSection == section,
                onClick = { onSectionSelected(section) },
                icon = {
                    Text(
                        text = section.icon,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                label = { Text(section.label) }
            )
        }
    }
}

@Composable
private fun VersionFooter() {
    Text(
        text = "Emily ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
        color = Charcoal.copy(alpha = 0.58f),
        fontSize = 12.sp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    )
}

@Composable
private fun DataSelectionCard(
    includeSteps: Boolean,
    onIncludeStepsChange: (Boolean) -> Unit,
    includeSleep: Boolean,
    onIncludeSleepChange: (Boolean) -> Unit,
    includeHeartRate: Boolean,
    onIncludeHeartRateChange: (Boolean) -> Unit,
    includeActiveCalories: Boolean,
    onIncludeActiveCaloriesChange: (Boolean) -> Unit,
    includeWorkouts: Boolean,
    onIncludeWorkoutsChange: (Boolean) -> Unit,
    includeWeight: Boolean,
    onIncludeWeightChange: (Boolean) -> Unit,
    isExpanded: Boolean,
    onToggleExpanded: () -> Unit
) {
    val selectedCount = listOf(
        includeSteps,
        includeSleep,
        includeHeartRate,
        includeActiveCalories,
        includeWorkouts,
        includeWeight
    ).count { it }

    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Data to use",
                        color = Charcoal,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$selectedCount of 6 selected",
                        color = Charcoal.copy(alpha = 0.72f)
                    )
                }
                TextButton(onClick = onToggleExpanded) {
                    Text(if (isExpanded) "Hide" else "Change")
                }
            }
            if (isExpanded) {
                Text(
                    text = "Choose what Emily imports and includes in coaching.",
                    color = Charcoal.copy(alpha = 0.72f)
                )
                DataCheckboxRow("Steps", includeSteps, onIncludeStepsChange)
                DataCheckboxRow("Sleep", includeSleep, onIncludeSleepChange)
                DataCheckboxRow("Heart rate, HRV, and resting HR", includeHeartRate, onIncludeHeartRateChange)
                DataCheckboxRow("Active calories", includeActiveCalories, onIncludeActiveCaloriesChange)
                DataCheckboxRow("Workout types and minutes", includeWorkouts, onIncludeWorkoutsChange)
                DataCheckboxRow("Weight", includeWeight, onIncludeWeightChange)
            }
        }
    }
}

@Composable
private fun DataCheckboxRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
        Text(text = label, color = Charcoal)
    }
}

@Composable
private fun SourceNote(text: String) {
    Text(
        text = text,
        color = Charcoal.copy(alpha = 0.62f),
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
private fun ImportedDataRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    unit: String = ""
) {
    val isMissing = value == "No data"
    val displayValue = if (unit.isBlank() || isMissing) value else "$value $unit"
    val needsStackedLayout = displayValue.length > 16 || label.length + displayValue.length > 28
    val rowModifier = modifier
        .fillMaxWidth()
        .background(SoftMint.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
        .padding(horizontal = 12.dp, vertical = 10.dp)

    if (needsStackedLayout) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = rowModifier
        ) {
            Text(
                text = label,
                color = Charcoal.copy(alpha = 0.72f),
                fontSize = 13.sp
            )
            Text(
                text = displayValue,
                color = if (isMissing) Charcoal.copy(alpha = 0.58f) else Teal,
                fontSize = if (isMissing) 14.sp else 17.sp,
                fontWeight = if (isMissing) FontWeight.SemiBold else FontWeight.Bold,
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth()
            )
        }
        return
    }

    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = rowModifier
    ) {
        Text(
            text = label,
            color = Charcoal.copy(alpha = 0.72f),
            fontSize = 13.sp,
            modifier = Modifier
                .weight(1f)
                .padding(end = 10.dp)
        )
        Text(
            text = displayValue,
            color = if (isMissing) Charcoal.copy(alpha = 0.58f) else Teal,
            fontSize = if (isMissing) 14.sp else 17.sp,
            fontWeight = if (isMissing) FontWeight.SemiBold else FontWeight.Bold,
            textAlign = TextAlign.End,
            modifier = Modifier.widthIn(min = 76.dp)
        )
    }
}

@Composable
private fun BulletTextList(
    text: String,
    fontSize: Int = 14
) {
    val bulletItems = remember(text) { text.toBulletItems() }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        bulletItems.forEach { item ->
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "-",
                    color = Teal,
                    fontSize = fontSize.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = item,
                    color = Charcoal.copy(alpha = 0.78f),
                    fontSize = fontSize.sp,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun TrendCard(trendSummary: String) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Trend",
                color = Charcoal,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            BulletTextList(
                text = trendSummary,
                fontSize = 16
            )
        }
    }
}

@Composable
private fun EmilyCoachCard(
    insight: String,
    onGenerateInsight: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Emily Coach",
                color = Charcoal,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            BulletTextList(text = insight)
            Button(
                onClick = onGenerateInsight,
                colors = ButtonDefaults.buttonColors(containerColor = Teal),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Create Coach Summary")
            }
        }
    }
}

@Composable
private fun CoachConversationCard(
    fakeCoachMode: Boolean,
    onFakeCoachModeChange: (Boolean) -> Unit,
    question: String,
    onQuestionChange: (String) -> Unit,
    lastQuestion: String,
    suggestedQuestions: List<String>,
    onAskQuestion: (String) -> Unit
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Ask Emily Coach",
                        color = Charcoal,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (fakeCoachMode) "Fake data test mode: no OpenAI cost" else "Backend mode: may use your OpenAI account",
                        color = if (fakeCoachMode) Teal else Coral,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Checkbox(
                    checked = fakeCoachMode,
                    onCheckedChange = onFakeCoachModeChange
                )
            }
            Text(
                text = "Try a suggested question or type your own. Later this same card will call the backend.",
                color = Charcoal.copy(alpha = 0.72f)
            )
            suggestedQuestions.forEach { suggestedQuestion ->
                Button(
                    onClick = { onAskQuestion(suggestedQuestion) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SoftMint,
                        contentColor = Charcoal
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(suggestedQuestion)
                }
            }
            OutlinedTextField(
                value = question,
                onValueChange = onQuestionChange,
                label = { Text("Ask your own coach question") },
                minLines = 2,
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = { onAskQuestion(question) },
                enabled = question.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Teal),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Ask ChatGPT Coach")
            }
            if (lastQuestion.isNotBlank()) {
                Text(
                    text = "Last question: $lastQuestion",
                    color = Charcoal.copy(alpha = 0.68f),
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun ChatGptCoachResponseCard(
    response: String,
    suggestions: List<String>
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = SoftMint),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "ChatGPT coach response",
                    color = Charcoal,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (response.isBlank()) "waiting" else "ready",
                    color = Teal,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            BulletTextList(
                text = response.ifBlank {
                    "After the backend is connected, ChatGPT's explanation of your health number will appear here."
                }
            )
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Suggestions",
                    color = Charcoal,
                    fontWeight = FontWeight.SemiBold
                )
                if (suggestions.isEmpty()) {
                    Text(
                        text = "No suggestions yet.",
                        color = Charcoal.copy(alpha = 0.68f)
                    )
                } else {
                    suggestions.forEach { suggestion ->
                        Text(
                            text = "- $suggestion",
                            color = Charcoal.copy(alpha = 0.78f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CoachUsageCard(
    fakeCoachMode: Boolean,
    requestCount: Int,
    inputTokens: Int,
    outputTokens: Int
) {
    val estimatedCost = if (fakeCoachMode) 0.0 else estimateCoachCostDollars(inputTokens, outputTokens)
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Coach usage",
                    color = Charcoal,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (fakeCoachMode) "\$0.00" else "\$${String.format(Locale.US, "%.4f", estimatedCost)}",
                    color = if (fakeCoachMode) Teal else Coral,
                    fontWeight = FontWeight.Bold
                )
            }
            ImportedDataRow(label = "Coach requests this session", value = requestCount.toString())
            ImportedDataRow(label = "Estimated input tokens", value = inputTokens.toString())
            ImportedDataRow(label = "Estimated output tokens", value = outputTokens.toString())
            Text(
                text = if (fakeCoachMode) {
                    "Fake data test mode does not call OpenAI. These token numbers are only practice estimates."
                } else {
                    "Backend mode will use real usage values returned by OpenAI after the connection is wired."
                },
                color = Charcoal.copy(alpha = 0.68f),
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun HealthConnectCard(
    statusText: String,
    isAvailable: Boolean,
    hasPermission: Boolean,
    hasSelectedData: Boolean,
    onRequestPermission: () -> Unit,
    onManageAccess: () -> Unit,
    onImportToday: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Google Health Connect",
                color = Charcoal,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(text = statusText, color = Charcoal.copy(alpha = 0.72f))
            Text(
                text = "Import Today fills the Health Connect boxes below. Mood, symptoms, medications, and notes stay manual.",
                color = Charcoal.copy(alpha = 0.72f),
                fontSize = 13.sp
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = onRequestPermission,
                    enabled = isAvailable && hasSelectedData,
                    colors = ButtonDefaults.buttonColors(containerColor = Coral),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Text("Connect")
                }
                Button(
                    onClick = onManageAccess,
                    enabled = isAvailable,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SoftMint,
                        contentColor = Charcoal,
                        disabledContainerColor = SoftMint.copy(alpha = 0.5f),
                        disabledContentColor = Charcoal.copy(alpha = 0.45f)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Text("Manage")
                }
            }
            Button(
                onClick = onImportToday,
                enabled = isAvailable && hasSelectedData,
                colors = ButtonDefaults.buttonColors(containerColor = Teal),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Import Today")
            }
        }
    }
}

@Composable
private fun FakeDataTestCard(
    onFillFakeData: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Testing",
                color = Charcoal,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Fill Emily with fake sample data. This does not use Health Connect, OpenAI, tokens, or money.",
                color = Charcoal.copy(alpha = 0.72f)
            )
            Button(
                onClick = onFillFakeData,
                colors = ButtonDefaults.buttonColors(containerColor = Coral),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Fill Fake Data")
            }
        }
    }
}

@Composable
private fun Header(wellnessScore: Int) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color.White, SoftMint, SoftCoral)
                    )
                )
                .padding(18.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Image(
                    painter = painterResource(id = R.drawable.emily_mark),
                    contentDescription = "Emily app mark",
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Emily",
                        color = Charcoal,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Daily health tracker",
                        color = Charcoal.copy(alpha = 0.72f),
                        fontSize = 15.sp
                    )
                }
                ScoreCircle(score = wellnessScore)
            }
        }
    }
}

@Composable
private fun ScoreCircle(score: Int) {
    var displayedScoreTarget by remember { mutableStateOf(0) }
    var rotationTarget by remember { mutableStateOf(0f) }
    val displayedScore by animateIntAsState(
        targetValue = displayedScoreTarget,
        animationSpec = tween(durationMillis = 900),
        label = "scoreCountUp"
    )
    val rotationDegrees by animateFloatAsState(
        targetValue = rotationTarget,
        animationSpec = tween(durationMillis = 900),
        label = "scoreSpin"
    )

    LaunchedEffect(score) {
        displayedScoreTarget = score
        rotationTarget += 360f
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(76.dp)
            .graphicsLayer(rotationZ = rotationDegrees)
            .clip(CircleShape)
            .background(Teal)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.graphicsLayer(rotationZ = -rotationDegrees)
        ) {
            Text(
                text = displayedScore.toString(),
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )
            Text(text = "score", color = Color.White.copy(alpha = 0.88f), fontSize = 11.sp)
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    content: @Composable () -> Unit
) {
    val stackHeader = title.length + value.length > 26
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            if (stackHeader) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = title, color = Charcoal, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text(text = value, color = Teal, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            } else {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = title, color = Charcoal, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text(text = value, color = Teal, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }
            content()
        }
    }
}

@Composable
private fun NotesCard(
    symptoms: String,
    onSymptomsChange: (String) -> Unit,
    medications: String,
    onMedicationsChange: (String) -> Unit,
    notes: String,
    onNotesChange: (String) -> Unit
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = "Health notes", color = Charcoal, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = symptoms,
                onValueChange = onSymptomsChange,
                label = { Text("Symptoms") },
                minLines = 2,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = medications,
                onValueChange = onMedicationsChange,
                label = { Text("Medications") },
                minLines = 2,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = notes,
                onValueChange = onNotesChange,
                label = { Text("Anything else") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun EmptyState() {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "No check-ins yet. Save one when Emily is ready for today's snapshot.",
            color = Charcoal.copy(alpha = 0.72f),
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
private fun EntryCard(entry: HealthEntry) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(entry.date, color = Charcoal, fontWeight = FontWeight.Bold)
                Text("Score ${entry.score}", color = Coral, fontWeight = FontWeight.Bold)
            }
            Text("Sleep: ${entry.sleepHours}h  |  Steps: ${entry.steps}")
            Text("Mood: ${entry.mood}/10")
            Text("Symptoms: ${entry.symptoms}")
            Text("Medications: ${entry.medications}")
            Text("Notes: ${entry.notes}")
        }
    }
}

private fun calculateWellnessScore(
    sleepHours: Float,
    steps: Int,
    mood: Float
): Int {
    val sleepScore = ((sleepHours.coerceIn(0f, 8f) / 8f) * 35f)
    val stepScore = ((steps.coerceIn(0, 8000) / 8000f) * 35f)
    val moodScore = ((mood.coerceIn(1f, 10f) / 10f) * 30f)
    return (sleepScore + stepScore + moodScore).roundToInt()
}

private fun buildCoachInsight(
    wellnessScore: Int,
    sleepHours: String,
    steps: String,
    mood: Int,
    symptoms: String,
    medications: String,
    notes: String,
    trendSummary: String,
    heartRate: String,
    restingHeartRate: String,
    latestHeartRate: String,
    minHeartRate: String,
    maxHeartRate: String,
    heartRateSamples: String,
    hrvToday: String,
    hrvWeekAverage: String,
    hrvChange: String,
    hrvSamples: String,
    activeCalories: String,
    exerciseMinutes: String,
    workoutTypes: String,
    weightPounds: String,
    selectedHealthData: SelectedHealthData,
    recentEntries: List<HealthEntry>
): String {
    val stepCount = steps.toIntOrNull() ?: 0
    val sleep = sleepHours.toFloatOrNull() ?: 0f
    val focusArea = when {
        sleep < 6f -> "sleep recovery"
        stepCount < 3500 -> "gentle movement"
        mood <= 4 -> "mood support"
        else -> "consistency"
    }
    val trend = if (recentEntries.isEmpty()) {
        "This is the first saved snapshot, so Emily will start spotting trends after a few check-ins."
    } else {
        val averageScore = recentEntries.map { it.score }.average().roundToInt()
        "Your recent saved average is $averageScore, compared with today's $wellnessScore."
    }
    val symptomLine = symptoms.ifBlank { "No symptoms were entered for this check-in." }
    val medicationLine = medications.ifBlank { "No medication notes were entered for this check-in." }
    val noteLine = notes.ifBlank { "No extra notes were entered for this check-in." }
    val recoveryTrend = recoveryTrendText(
        restingHeartRate = restingHeartRate,
        hrvToday = hrvToday,
        hrvWeekAverage = hrvWeekAverage
    )

    val selectedDataSummary = selectedHealthData.summaryLabel()

    return """
        Today's health number is $wellnessScore. The main coaching focus is $focusArea.
        
        $trend
        
        $trendSummary
        
        User-selected data types: $selectedDataSummary.
        Data used: $sleepHours hours sleep, $steps steps, mood $mood/10.
        Heart/activity: ${heartRate.ifBlank { "no heart rate data" }} bpm avg, ${latestHeartRate.ifBlank { "no latest heart rate data" }} bpm latest, ${restingHeartRate.ifBlank { "no resting heart rate data" }} bpm resting, ${minHeartRate.ifBlank { "no low heart rate data" }} bpm low, ${maxHeartRate.ifBlank { "no high heart rate data" }} bpm high, ${heartRateSamples.ifBlank { "0" }} heart samples, HRV ${hrvToday.ifBlank { "no HRV data" }} ms today, HRV ${hrvWeekAverage.ifBlank { "no HRV baseline" }} ms 7-day average, HRV change ${hrvChange.ifBlank { "not available" }}, ${hrvSamples.ifBlank { "0" }} HRV samples, ${exerciseMinutes.ifBlank { "0" }} exercise minutes, ${activeCalories.ifBlank { "0" }} active calories.
        Recovery trend to explain first: $recoveryTrend
        Workout types: $workoutTypes
        Weight: ${weightPounds.ifBlank { "no weight data" }} lb.
        Symptoms: $symptomLine
        Medications: $medicationLine
        Notes: $noteLine
        
        ChatGPT should explain the HRV and resting heart rate recovery trend first, then explain other patterns, ask one helpful follow-up question, and suggest small non-medical next steps. It should not diagnose, prescribe, or replace a clinician.
    """.trimIndent()
}

private fun buildFakeCoachResponse(
    question: String,
    wellnessScore: Int,
    sleepHours: String,
    steps: String,
    heartRate: String,
    restingHeartRate: String,
    latestHeartRate: String,
    minHeartRate: String,
    maxHeartRate: String,
    heartRateSamples: String,
    hrvToday: String,
    hrvWeekAverage: String,
    hrvChange: String,
    hrvSamples: String,
    exerciseMinutes: String,
    workoutTypes: String,
    mood: Int,
    trendSummary: String
): FakeCoachResponse {
    val stepCount = steps.toIntOrNull() ?: 0
    val sleep = sleepHours.toFloatOrNull() ?: 0f
    val movementText = when {
        stepCount >= 8000 -> "Your movement looks strong today at $steps steps."
        stepCount >= 4000 -> "Your movement is moderate today at $steps steps."
        else -> "Your movement looks light today at ${steps.ifBlank { "0" }} steps."
    }
    val sleepText = when {
        sleep >= 7f -> "Sleep is in a solid range at ${sleepHours.ifBlank { "0" }} hours."
        sleep >= 6f -> "Sleep is close, but a little more recovery time may help."
        else -> "Sleep looks like the first area to protect."
    }
    val heartText = if (heartRate.isBlank() && restingHeartRate.isBlank() && latestHeartRate.isBlank()) {
        "Heart and HRV data were not found for today."
    } else {
        "Heart today shows avg ${heartRate.ifBlank { "no avg" }}, latest ${latestHeartRate.ifBlank { "no latest" }}, resting ${restingHeartRate.ifBlank { "no resting" }}, low ${minHeartRate.ifBlank { "no low" }}, high ${maxHeartRate.ifBlank { "no high" }}, from ${heartRateSamples.ifBlank { "0" }} samples. HRV is ${hrvToday.ifBlank { "no HRV today" }} ms today versus ${hrvWeekAverage.ifBlank { "no baseline" }} ms baseline, change ${hrvChange.ifBlank { "not available" }}, from ${hrvSamples.ifBlank { "0" }} HRV samples."
    }
    val recoveryTrend = recoveryTrendText(
        restingHeartRate = restingHeartRate,
        hrvToday = hrvToday,
        hrvWeekAverage = hrvWeekAverage
    )
    val workoutText = if (exerciseMinutes.isBlank()) {
        "No workout minutes were imported yet."
    } else {
        "Workout time is $exerciseMinutes minutes, with types listed as $workoutTypes."
    }

    return FakeCoachResponse(
        response = """
            Fake Emily Coach response
            
            Question: $question
            
            Recovery trend: $recoveryTrend
            
            Health number: $wellnessScore. $sleepText $movementText $heartText $workoutText Mood is $mood/10.
            
            Trend view: $trendSummary
            
            This is test-mode guidance only. The paid OpenAI connection is not being used yet.
        """.trimIndent(),
        suggestions = listOf(
            if (sleep < 7f) "Protect an earlier bedtime or a calmer wind-down tonight." else "Keep sleep timing steady tonight.",
            if (stepCount < 6000) "Try a short easy walk if you feel up to it." else "Keep movement steady without overdoing it.",
            if (restingHeartRate.isBlank()) "Import resting heart rate again after Health Connect has today's data." else "Watch whether resting heart rate stays near your normal baseline.",
            if (minHeartRate.isBlank() || maxHeartRate.isBlank()) "Import heart rate samples to see today's low and high range." else "Use the low-to-high heart range as context for workout intensity and recovery.",
            if (hrvToday.isBlank() || hrvWeekAverage.isBlank()) "Import HRV after your watch or health app has synced recovery data." else "Compare HRV and resting heart rate together before judging recovery."
        )
    )
}

private fun estimateTokens(text: String): Int {
    return (text.length / 4).coerceAtLeast(1)
}

private fun estimateCoachCostDollars(inputTokens: Int, outputTokens: Int): Double {
    val inputCost = inputTokens / 1_000_000.0 * 2.50
    val outputCost = outputTokens / 1_000_000.0 * 15.00
    return inputCost + outputCost
}

private suspend fun readHealthConnectData(
    healthConnectClient: HealthConnectClient,
    selectedHealthData: SelectedHealthData
): ImportedHealthDataSet {
    val zoneId = ZoneId.systemDefault()
    val todayStart = LocalDate.now().atStartOfDay(zoneId).toInstant()
    val tomorrowStart = LocalDate.now().plusDays(1).atStartOfDay(zoneId).toInstant()
    val weekStart = LocalDate.now().minusDays(6).atStartOfDay(zoneId).toInstant()
    val weightStart = LocalDate.now().minusDays(30).atStartOfDay(zoneId).toInstant()
    val todayRange = TimeRangeFilter.between(todayStart, tomorrowStart)
    val weekRange = TimeRangeFilter.between(weekStart, tomorrowStart)

    val today = readHealthMetrics(healthConnectClient, todayRange, selectedHealthData)
    val week = readHealthMetrics(healthConnectClient, weekRange, selectedHealthData)
    val weekAverageHrvMillis = week.hrvAverageMillis
    val latestWeight = if (selectedHealthData.includeWeight) {
        readLatestWeightPounds(
            healthConnectClient = healthConnectClient,
            timeRangeFilter = TimeRangeFilter.between(weightStart, tomorrowStart)
        )
    } else {
        null
    }

    return ImportedHealthDataSet(
        today = today,
        weekAverageHrvMillis = weekAverageHrvMillis,
        latestWeightPounds = latestWeight,
        trendSummary = buildTrendSummary(today, week)
    )
}

private suspend fun readHealthMetrics(
    healthConnectClient: HealthConnectClient,
    timeRangeFilter: TimeRangeFilter,
    selectedHealthData: SelectedHealthData
): ImportedHealthMetrics {
    val requestedMetrics = buildSet {
        if (selectedHealthData.includeSteps) add(StepsRecord.COUNT_TOTAL)
        if (selectedHealthData.includeActiveCalories) add(ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL)
        if (selectedHealthData.includeWorkouts) add(ExerciseSessionRecord.EXERCISE_DURATION_TOTAL)
        if (selectedHealthData.includeHeartRate) {
            add(HeartRateRecord.BPM_AVG)
            add(RestingHeartRateRecord.BPM_AVG)
        }
    }
    val aggregateResponse = if (requestedMetrics.isEmpty()) {
        null
    } else {
        healthConnectClient.aggregate(
            AggregateRequest(
                metrics = requestedMetrics,
                timeRangeFilter = timeRangeFilter
            )
        )
    }
    val sleepResponse = if (selectedHealthData.includeSleep) {
        healthConnectClient.readRecords(
            ReadRecordsRequest(
                recordType = SleepSessionRecord::class,
                timeRangeFilter = timeRangeFilter
            )
        )
    } else {
        null
    }
    val exerciseResponse = if (selectedHealthData.includeWorkouts) {
        healthConnectClient.readRecords(
            ReadRecordsRequest(
                recordType = ExerciseSessionRecord::class,
                timeRangeFilter = timeRangeFilter
            )
        )
    } else {
        null
    }
    val heartRateResponse = if (selectedHealthData.includeHeartRate) {
        healthConnectClient.readRecords(
            ReadRecordsRequest(
                recordType = HeartRateRecord::class,
                timeRangeFilter = timeRangeFilter
            )
        )
    } else {
        null
    }
    val hrvResponse = if (selectedHealthData.includeHeartRate) {
        healthConnectClient.readRecords(
            ReadRecordsRequest(
                recordType = HeartRateVariabilityRmssdRecord::class,
                timeRangeFilter = timeRangeFilter
            )
        )
    } else {
        null
    }

    val sleepMinutes = sleepResponse?.records?.sumOf { sleepRecord ->
        Duration.between(sleepRecord.startTime, sleepRecord.endTime).toMinutes()
    }
    val workoutTypesSummary = exerciseResponse?.records?.let { summarizeWorkoutTypes(it) }
        ?: "Workout data not selected"
    val heartRateSamples = heartRateResponse?.records
        ?.flatMap { heartRateRecord -> heartRateRecord.samples }
        .orEmpty()
    val hrvRecords = hrvResponse?.records.orEmpty()
    val hrvValues = hrvRecords.map { record -> record.heartRateVariabilityMillis }

    return ImportedHealthMetrics(
        steps = aggregateResponse?.get(StepsRecord.COUNT_TOTAL),
        sleepMinutes = sleepMinutes,
        activeCalories = aggregateResponse?.get(ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL)?.inKilocalories,
        exerciseMinutes = aggregateResponse?.get(ExerciseSessionRecord.EXERCISE_DURATION_TOTAL)?.toMinutes(),
        averageHeartRate = aggregateResponse?.get(HeartRateRecord.BPM_AVG),
        restingHeartRate = aggregateResponse?.get(RestingHeartRateRecord.BPM_AVG),
        latestHeartRate = heartRateSamples.maxByOrNull { sample -> sample.time }?.beatsPerMinute,
        minHeartRate = heartRateSamples.minOfOrNull { sample -> sample.beatsPerMinute },
        maxHeartRate = heartRateSamples.maxOfOrNull { sample -> sample.beatsPerMinute },
        heartRateSampleCount = heartRateSamples.size.takeIf { count -> count > 0 },
        latestHrvMillis = hrvRecords.maxByOrNull { record -> record.time }?.heartRateVariabilityMillis,
        hrvAverageMillis = hrvValues.averageOrNull(),
        hrvSampleCount = hrvRecords.size.takeIf { count -> count > 0 },
        workoutTypesSummary = workoutTypesSummary
    )
}

private fun formatHours(minutes: Long): String {
    val hours = minutes / 60f
    return formatOneDecimal(hours)
}

private suspend fun readLatestWeightPounds(
    healthConnectClient: HealthConnectClient,
    timeRangeFilter: TimeRangeFilter
): Double? {
    val weightResponse = healthConnectClient.readRecords(
        ReadRecordsRequest(
            recordType = WeightRecord::class,
            timeRangeFilter = timeRangeFilter
        )
    )
    return weightResponse.records.maxByOrNull { it.time }?.weight?.inPounds
}

private fun buildTrendSummary(
    today: ImportedHealthMetrics,
    week: ImportedHealthMetrics
): String {
    val dailyAverageSteps = week.steps?.div(7)
    val dailyAverageSleep = week.sleepMinutes?.div(7)
    val dailyAverageCalories = week.activeCalories?.div(7.0)
    val dailyAverageExercise = week.exerciseMinutes?.div(7)
    val heartRateText = week.averageHeartRate?.let { "$it bpm average heart rate" } ?: "no heart rate average"
    val restingHeartRateText = week.restingHeartRate?.let { "$it bpm resting heart rate" }
        ?: "no resting heart rate average"
    val heartRangeText = if (week.minHeartRate != null && week.maxHeartRate != null) {
        "heart range ${week.minHeartRate}-${week.maxHeartRate} bpm"
    } else {
        "no heart rate range"
    }
    val heartSamplesText = week.heartRateSampleCount?.let { "$it heart samples" } ?: "no heart samples"
    val latestHeartRateText = today.latestHeartRate?.let { "Latest heart rate today is $it bpm." }
        ?: "Latest heart rate today was not found."
    val hrvAverageText = week.hrvAverageMillis?.let { "HRV ${formatOneDecimal(it)} ms 7-day average" }
        ?: "no HRV average"
    val hrvChangeText = hrvChangeText(today.latestHrvMillis, week.hrvAverageMillis)
    val restingHeartRateChangeText = restingHeartRateChangeText(today.restingHeartRate, week.restingHeartRate)
    val calorieText = dailyAverageCalories?.let { "${it.roundToInt()} active calories/day" }
        ?: "no active calorie average"
    val stepsText = dailyAverageSteps?.let { "$it steps/day" } ?: "steps not selected"
    val sleepText = dailyAverageSleep?.let { "${formatHours(it)} sleep hours/day" } ?: "sleep not selected"
    val exerciseText = dailyAverageExercise?.let { "$it exercise min/day" } ?: "workouts not selected"
    val todayStepsText = today.steps?.let { "$it steps" } ?: "steps not selected"
    val todaySleepText = today.sleepMinutes?.let { "${formatHours(it)} sleep hours" } ?: "sleep not selected"

    return "7-day trend: $stepsText, $sleepText, " +
        "$exerciseText, $calorieText, $heartRateText, $restingHeartRateText, $heartRangeText, $heartSamplesText, $hrvAverageText. " +
        "$restingHeartRateChangeText " +
        "$hrvChangeText " +
        "$latestHeartRateText " +
        "Workout types this week: ${week.workoutTypesSummary}. " +
        "Today: $todayStepsText and $todaySleepText."
}

private fun hrvChangeText(
    todayHrvMillis: Double?,
    weekAverageHrvMillis: Double?
): String {
    if (todayHrvMillis == null) {
        return "Latest HRV today was not found."
    }
    if (weekAverageHrvMillis == null) {
        return "Latest HRV today is ${formatOneDecimal(todayHrvMillis)} ms, with no 7-day baseline yet."
    }

    val difference = todayHrvMillis - weekAverageHrvMillis
    val direction = when {
        difference > 0.05 -> "${formatOneDecimal(kotlin.math.abs(difference))} above"
        difference < -0.05 -> "${formatOneDecimal(kotlin.math.abs(difference))} below"
        else -> "the same as"
    }

    return "Latest HRV today is ${formatOneDecimal(todayHrvMillis)} ms, $direction your 7-day average."
}

private fun recoveryTrendText(
    restingHeartRate: String,
    hrvToday: String,
    hrvWeekAverage: String
): String {
    val resting = restingHeartRate.toLongOrNull()
    val todayHrv = hrvToday.toDoubleOrNull()
    val baselineHrv = hrvWeekAverage.toDoubleOrNull()

    if (resting == null && (todayHrv == null || baselineHrv == null)) {
        return "Recovery trend needs HRV and resting heart rate data before Emily can compare recovery."
    }
    if (todayHrv == null || baselineHrv == null) {
        return "Resting heart rate is ${resting ?: "not available"} bpm, but HRV needs a today value and 7-day baseline before Emily can judge the recovery trend."
    }

    val hrvDifference = todayHrv - baselineHrv
    val hrvSignal = when {
        hrvDifference >= 3.0 -> "HRV is meaningfully above baseline"
        hrvDifference <= -3.0 -> "HRV is meaningfully below baseline"
        else -> "HRV is close to baseline"
    }
    val restingSignal = resting?.let { "resting heart rate today is $it bpm" }
        ?: "resting heart rate is not available"
    val recoveryRead = when {
        hrvDifference >= 3.0 -> "That can point toward better recovery, especially if resting heart rate is near your normal range."
        hrvDifference <= -3.0 -> "That can point toward more strain or less recovery, especially if resting heart rate is higher than normal."
        else -> "That looks more like a steady recovery signal unless resting heart rate is unusual for you."
    }

    return "$hrvSignal at ${formatOneDecimal(todayHrv)} ms versus ${formatOneDecimal(baselineHrv)} ms baseline, and $restingSignal. $recoveryRead"
}

private fun restingHeartRateChangeText(
    todayRestingHeartRate: Long?,
    weekRestingHeartRate: Long?
): String {
    if (todayRestingHeartRate == null) {
        return "Today's resting heart rate was not found."
    }
    if (weekRestingHeartRate == null) {
        return "Today's resting heart rate is $todayRestingHeartRate bpm, with no 7-day baseline yet."
    }

    val difference = todayRestingHeartRate - weekRestingHeartRate
    val direction = when {
        difference > 0 -> "${kotlin.math.abs(difference)} above"
        difference < 0 -> "${kotlin.math.abs(difference)} below"
        else -> "the same as"
    }

    return "Today's resting heart rate is $todayRestingHeartRate bpm, $direction your 7-day average."
}

private fun summarizeWorkoutTypes(
    exerciseSessions: List<ExerciseSessionRecord>
): String {
    if (exerciseSessions.isEmpty()) {
        return "No workout sessions found"
    }

    return exerciseSessions
        .groupBy { workoutTypeName(it.exerciseType) }
        .entries
        .sortedByDescending { (_, sessions) -> sessions.size }
        .joinToString(separator = ", ") { (type, sessions) ->
            "$type x${sessions.size}"
        }
}

private fun workoutTypeName(exerciseType: Int): String {
    val rawName = ExerciseSessionRecord.EXERCISE_TYPE_INT_TO_STRING_MAP[exerciseType]
        ?: "Other workout"
    return rawName
        .replace("_", " ")
        .lowercase(Locale.US)
        .replaceFirstChar { firstCharacter ->
            if (firstCharacter.isLowerCase()) firstCharacter.titlecase(Locale.US) else firstCharacter.toString()
        }
}

private fun formatOneDecimal(value: Float): String {
    return String.format(Locale.US, "%.1f", value)
}

private fun formatOneDecimal(value: Double): String {
    return String.format(Locale.US, "%.1f", value)
}

private fun List<Double>.averageOrNull(): Double? {
    return if (isEmpty()) null else average()
}

private fun recoveryReviewSummary(
    hrvToday: String,
    hrvWeekAverage: String,
    restingHeartRate: String
): String {
    val hrvText = if (hrvToday.isBlank()) "HRV --" else "HRV $hrvToday"
    val hrvBaselineText = if (hrvWeekAverage.isBlank()) "base --" else "base $hrvWeekAverage"
    val restingText = if (restingHeartRate.isBlank()) "rest --" else "rest $restingHeartRate"
    return "$hrvText | $hrvBaselineText | $restingText"
}

private fun workoutSummary(
    includeWorkouts: Boolean,
    exerciseMinutes: String,
    includeActiveCalories: Boolean,
    activeCalories: String
): String {
    val parts = buildList {
        if (includeWorkouts) add("${exerciseMinutes.ifBlank { "0" }} min")
        if (includeActiveCalories) add("${activeCalories.ifBlank { "No data" }} cal")
    }
    return parts.joinToString(" | ").ifBlank { "No data" }
}

private fun String.toBulletItems(): List<String> {
    return trim()
        .replace("\r", "")
        .split("\n", ". ")
        .map { line ->
            line.trim()
                .removePrefix("-")
                .removePrefix("•")
                .trim()
        }
        .filter { line -> line.isNotBlank() }
        .map { line ->
            if (line.endsWith(".") || line.endsWith(":") || line.endsWith("?")) {
                line
            } else {
                "$line."
            }
        }
}

private data class HealthEntry(
    val date: String,
    val score: Int,
    val sleepHours: String,
    val waterCups: Int,
    val steps: String,
    val mood: Int,
    val symptoms: String,
    val medications: String,
    val notes: String
)

private data class ImportedHealthDataSet(
    val today: ImportedHealthMetrics,
    val weekAverageHrvMillis: Double?,
    val latestWeightPounds: Double?,
    val trendSummary: String
)

private data class ImportedHealthMetrics(
    val steps: Long?,
    val sleepMinutes: Long?,
    val activeCalories: Double?,
    val exerciseMinutes: Long?,
    val averageHeartRate: Long?,
    val restingHeartRate: Long?,
    val latestHeartRate: Long?,
    val minHeartRate: Long?,
    val maxHeartRate: Long?,
    val heartRateSampleCount: Int?,
    val latestHrvMillis: Double?,
    val hrvAverageMillis: Double?,
    val hrvSampleCount: Int?,
    val workoutTypesSummary: String
)

private data class FakeCoachResponse(
    val response: String,
    val suggestions: List<String>
)

private data class SelectedHealthData(
    val includeSteps: Boolean,
    val includeSleep: Boolean,
    val includeHeartRate: Boolean,
    val includeActiveCalories: Boolean,
    val includeWorkouts: Boolean,
    val includeWeight: Boolean
) {
    fun hasAnyHealthConnectImport(): Boolean {
        return includeSteps ||
            includeSleep ||
            includeHeartRate ||
            includeActiveCalories ||
            includeWorkouts ||
            includeWeight
    }

    fun permissions(): Set<String> {
        return buildSet {
            if (includeSteps) add(HealthPermission.getReadPermission(StepsRecord::class))
            if (includeSleep) add(HealthPermission.getReadPermission(SleepSessionRecord::class))
            if (includeHeartRate) {
                add(HealthPermission.getReadPermission(HeartRateRecord::class))
                add(HealthPermission.getReadPermission(HeartRateVariabilityRmssdRecord::class))
                add(HealthPermission.getReadPermission(RestingHeartRateRecord::class))
            }
            if (includeActiveCalories) add(HealthPermission.getReadPermission(ActiveCaloriesBurnedRecord::class))
            if (includeWorkouts) add(HealthPermission.getReadPermission(ExerciseSessionRecord::class))
            if (includeWeight) add(HealthPermission.getReadPermission(WeightRecord::class))
        }
    }

    fun withGrantedPermissions(grantedPermissions: Set<String>): SelectedHealthData {
        val heartRatePermissions = setOf(
            HealthPermission.getReadPermission(HeartRateRecord::class),
            HealthPermission.getReadPermission(HeartRateVariabilityRmssdRecord::class),
            HealthPermission.getReadPermission(RestingHeartRateRecord::class)
        )
        return copy(
            includeSteps = includeSteps &&
                HealthPermission.getReadPermission(StepsRecord::class) in grantedPermissions,
            includeSleep = includeSleep &&
                HealthPermission.getReadPermission(SleepSessionRecord::class) in grantedPermissions,
            includeHeartRate = includeHeartRate &&
                heartRatePermissions.all { permission -> permission in grantedPermissions },
            includeActiveCalories = includeActiveCalories &&
                HealthPermission.getReadPermission(ActiveCaloriesBurnedRecord::class) in grantedPermissions,
            includeWorkouts = includeWorkouts &&
                HealthPermission.getReadPermission(ExerciseSessionRecord::class) in grantedPermissions,
            includeWeight = includeWeight &&
                HealthPermission.getReadPermission(WeightRecord::class) in grantedPermissions
        )
    }

    fun summaryLabel(): String {
        val labels = buildList {
            if (includeSteps) add("steps")
            if (includeSleep) add("sleep")
            if (includeHeartRate) add("heart rate, HRV, and resting heart rate")
            if (includeActiveCalories) add("active calories")
            if (includeWorkouts) add("workouts")
            if (includeWeight) add("weight")
        }
        return labels.ifEmpty { listOf("manual entries only") }.joinToString()
    }
}

private enum class AppSection(
    val label: String,
    val icon: String
) {
    Home(label = "Home", icon = "⌂"),
    Data(label = "Review", icon = "▦"),
    Coach(label = "Coach", icon = "?"),
    Trend(label = "Trend", icon = "↗")
}

private fun loadHealthEntries(preferences: SharedPreferences): List<HealthEntry> {
    val savedJson = preferences.getString("entries", null) ?: return emptyList()
    val savedEntries = JSONArray(savedJson)
    return List(savedEntries.length()) { index ->
        val item = savedEntries.getJSONObject(index)
        HealthEntry(
            date = item.getString("date"),
            score = item.getInt("score"),
            sleepHours = item.getString("sleepHours"),
            waterCups = item.optInt("waterCups", 0),
            steps = item.getString("steps"),
            mood = item.getInt("mood"),
            symptoms = item.getString("symptoms"),
            medications = item.getString("medications"),
            notes = item.getString("notes")
        )
    }
}

private fun saveHealthEntries(
    preferences: SharedPreferences,
    entries: List<HealthEntry>
) {
    val savedEntries = JSONArray()
    entries.forEach { entry ->
        savedEntries.put(
            JSONObject()
                .put("date", entry.date)
                .put("score", entry.score)
                .put("sleepHours", entry.sleepHours)
                .put("waterCups", entry.waterCups)
                .put("steps", entry.steps)
                .put("mood", entry.mood)
                .put("symptoms", entry.symptoms)
                .put("medications", entry.medications)
                .put("notes", entry.notes)
        )
    }
    preferences.edit().putString("entries", savedEntries.toString()).apply()
}

private val Cream = Color(0xFFFFF8F0)
private val SoftMint = Color(0xFFE6F5F0)
private val SoftCoral = Color(0xFFFFE8DF)
private val Teal = Color(0xFF247C76)
private val Coral = Color(0xFFE96F5F)
private val Charcoal = Color(0xFF24302F)

private val suggestedCoachQuestions = listOf(
    "Is my HRV and resting heart rate showing recovery?",
    "Review my data and tell me what changed.",
    "What trends do you see today?",
    "How did my workout affect recovery?",
    "What should I focus on tomorrow?"
)

@Preview(showBackground = true)
@Composable
private fun EmilyAppPreview() {
    EmilyApp()
}
