package com.emily.healthtracker

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ActiveCaloriesBurnedRecord
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.HeartRateRecord
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
                        Text("Steps, sleep, average heart rate, resting heart rate, active calories, workout sessions, workout types, and weight may be used only after you grant permission.")
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
    var includeSteps by remember { mutableStateOf(true) }
    var includeSleep by remember { mutableStateOf(true) }
    var includeHeartRate by remember { mutableStateOf(true) }
    var includeActiveCalories by remember { mutableStateOf(true) }
    var includeWorkouts by remember { mutableStateOf(true) }
    var includeWeight by remember { mutableStateOf(true) }
    var isDataSelectionExpanded by remember { mutableStateOf(false) }
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

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream)
            .padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Header(wellnessScore = wellnessScore)
        }

        if (includeSleep) {
            item {
                MetricCard(title = "Sleep", value = "${sleepHours.ifBlank { "0" }} hours") {
                    ImportedDataRow(
                        label = "Sleep from Health Connect",
                        value = sleepHours.ifBlank { "No data" },
                        unit = "hours"
                    )
                }
            }
        }

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
                            val importableData = selectedHealthData.withGrantedPermissions(grantedHealthPermissions)
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

        if (includeSteps) {
            item {
                MetricCard(title = "Movement", value = "${steps.ifBlank { "0" }} steps") {
                    ImportedDataRow(label = "Steps from Health Connect", value = steps.ifBlank { "No data" })
                }
            }
        }

        if (includeHeartRate) {
            item {
                MetricCard(
                    title = "Heart",
                    value = heartSummary(heartRate, restingHeartRate)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ImportedDataRow(
                            label = "Average heart rate",
                            value = heartRate.ifBlank { "No data" },
                            unit = "bpm"
                        )
                        ImportedDataRow(
                            label = "Resting heart rate today",
                            value = restingHeartRate.ifBlank { "No data" },
                            unit = "bpm"
                        )
                    }
                }
            }
        }

        if (includeWorkouts || includeActiveCalories) {
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

        if (includeWeight) {
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

        item {
            TrendCard(trendSummary = trendSummary)
        }

        item {
            EmilyCoachCard(
                insight = coachInsight,
                onGenerateInsight = {
                    coachInsight = buildCoachInsight(
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
                        activeCalories = activeCalories,
                        exerciseMinutes = exerciseMinutes,
                        workoutTypes = workoutTypes,
                        weightPounds = weightPounds,
                        selectedHealthData = selectedHealthData,
                        recentEntries = entries.take(5)
                    )
                }
            )
        }

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

        item {
            Text(
                text = "Recent check-ins",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Charcoal,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        if (entries.isEmpty()) {
            item {
                EmptyState()
            }
        } else {
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
                DataCheckboxRow("Heart rate", includeHeartRate, onIncludeHeartRateChange)
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
private fun ImportedDataRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    unit: String = ""
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .background(SoftMint.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Text(
            text = label,
            color = Charcoal.copy(alpha = 0.72f),
            fontSize = 13.sp,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = if (unit.isBlank() || value == "No data") value else "$value $unit",
            color = Teal,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
        )
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
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "7-day trends",
                color = Charcoal,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(text = trendSummary, color = Charcoal.copy(alpha = 0.78f))
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
            Text(text = insight, color = Charcoal.copy(alpha = 0.78f))
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
                enabled = isAvailable && hasPermission && hasSelectedData,
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
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(76.dp)
            .clip(CircleShape)
            .background(Teal)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = score.toString(),
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
                Text(text = title, color = Charcoal, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(text = value, color = Teal, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
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

    val selectedDataSummary = selectedHealthData.summaryLabel()

    return """
        Today's health number is $wellnessScore. The main coaching focus is $focusArea.
        
        $trend
        
        $trendSummary
        
        User-selected data types: $selectedDataSummary.
        Data used: $sleepHours hours sleep, $steps steps, mood $mood/10.
        Heart/activity: ${heartRate.ifBlank { "no heart rate data" }} bpm avg, ${restingHeartRate.ifBlank { "no resting heart rate data" }} bpm resting, ${exerciseMinutes.ifBlank { "0" }} exercise minutes, ${activeCalories.ifBlank { "0" }} active calories.
        Workout types: $workoutTypes
        Weight: ${weightPounds.ifBlank { "no weight data" }} lb.
        Symptoms: $symptomLine
        Medications: $medicationLine
        Notes: $noteLine
        
        ChatGPT should explain patterns, ask one helpful follow-up question, and suggest small non-medical next steps. It should not diagnose, prescribe, or replace a clinician.
    """.trimIndent()
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

    val sleepMinutes = sleepResponse?.records?.sumOf { sleepRecord ->
        Duration.between(sleepRecord.startTime, sleepRecord.endTime).toMinutes()
    }
    val workoutTypesSummary = exerciseResponse?.records?.let { summarizeWorkoutTypes(it) }
        ?: "Workout data not selected"

    return ImportedHealthMetrics(
        steps = aggregateResponse?.get(StepsRecord.COUNT_TOTAL),
        sleepMinutes = sleepMinutes,
        activeCalories = aggregateResponse?.get(ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL)?.inKilocalories,
        exerciseMinutes = aggregateResponse?.get(ExerciseSessionRecord.EXERCISE_DURATION_TOTAL)?.toMinutes(),
        averageHeartRate = aggregateResponse?.get(HeartRateRecord.BPM_AVG),
        restingHeartRate = aggregateResponse?.get(RestingHeartRateRecord.BPM_AVG),
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
    val restingHeartRateChangeText = restingHeartRateChangeText(today.restingHeartRate, week.restingHeartRate)
    val calorieText = dailyAverageCalories?.let { "${it.roundToInt()} active calories/day" }
        ?: "no active calorie average"
    val stepsText = dailyAverageSteps?.let { "$it steps/day" } ?: "steps not selected"
    val sleepText = dailyAverageSleep?.let { "${formatHours(it)} sleep hours/day" } ?: "sleep not selected"
    val exerciseText = dailyAverageExercise?.let { "$it exercise min/day" } ?: "workouts not selected"
    val todayStepsText = today.steps?.let { "$it steps" } ?: "steps not selected"
    val todaySleepText = today.sleepMinutes?.let { "${formatHours(it)} sleep hours" } ?: "sleep not selected"

    return "7-day trend: $stepsText, $sleepText, " +
        "$exerciseText, $calorieText, $heartRateText, $restingHeartRateText. " +
        "$restingHeartRateChangeText " +
        "Workout types this week: ${week.workoutTypesSummary}. " +
        "Today: $todayStepsText and $todaySleepText."
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

private fun heartSummary(
    averageHeartRate: String,
    restingHeartRate: String
): String {
    val averageText = if (averageHeartRate.isBlank()) "avg --" else "avg $averageHeartRate"
    val restingText = if (restingHeartRate.isBlank()) "rest today --" else "rest today $restingHeartRate"
    return "$averageText | $restingText"
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
    val workoutTypesSummary: String
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
            if (includeHeartRate) add("heart rate")
            if (includeActiveCalories) add("active calories")
            if (includeWorkouts) add("workouts")
            if (includeWeight) add("weight")
        }
        return labels.ifEmpty { listOf("manual entries only") }.joinToString()
    }
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

@Preview(showBackground = true)
@Composable
private fun EmilyAppPreview() {
    EmilyApp()
}
