package com.fqrproject.fqr.ui.screentime

import android.Manifest
import android.annotation.SuppressLint
import android.app.AppOpsManager
import android.app.Application
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.fqrproject.fqr.ui.fitness.HeaderSection
import com.fqrproject.fqr.ui.index.PieChart
import com.fqrproject.fqr.ui.index.PieSlice
import com.fqrproject.fqr.ui.index.formatDuration
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import kotlin.collections.emptyList


data class ScreenTime(
    val app: String,
    val time: Long,
    val packageName: String
)

fun getAppName(context: Context, packageName: String): String {
    return try {
        val packageManager = context.packageManager
        val appInfo = packageManager.getApplicationInfo(packageName, 0)
        packageManager.getApplicationLabel(appInfo).toString()
    } catch (e: PackageManager.NameNotFoundException) {
        packageName
    }
}

fun hasUsagePermission(context: Context): Boolean {
    val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
    val mode = appOps.checkOpNoThrow(
        AppOpsManager.OPSTR_GET_USAGE_STATS,
        android.os.Process.myUid(),
        context.packageName
    )
    return mode == AppOpsManager.MODE_ALLOWED
}
class ScreenTimeViewModel(
    application: Application,
    private val repo: ScreenTimeRepository
) : AndroidViewModel(application) {
    var hasPermission by mutableStateOf(false)
    var screenTimeData by mutableStateOf<List<ScreenTime>>(emptyList())

    val targetMinutesFlow: StateFlow<Int> = repo.targetMinutes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 120)


    private var lastNotifiedDate by mutableStateOf("")

    fun setTarget(minutes: Int) {
        viewModelScope.launch {
            repo.setTargetMinutes(minutes)
        }
    }

    fun checkPermission(context: Context) {
        hasPermission = hasUsagePermission(context)
    }

    fun openPermissionSettings(context: Context) {
        context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
    }

    fun checkAndNotify(context: Context) {
        val today = LocalDate.now().toString()

        if (lastNotifiedDate == today) return

        val totalMillis = screenTimeData.sumOf { it.time }
        val targetMinutes = targetMinutesFlow.value
        val targetMillis = targetMinutes * 60000L

        if (totalMillis > targetMillis) {
            val notification = NotificationCompat.Builder(context, "screen_time_channel")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Screen Time Alert")
                .setContentText("You've exceeded your daily screen time limit of ${formatDuration(targetMillis)}")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build()

            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED) {
                NotificationManagerCompat.from(context).notify(1, notification)
                lastNotifiedDate = today
            }
        }
    }

    @RequiresPermission(Manifest.permission.PACKAGE_USAGE_STATS)
    fun getTopAppsUsage() {
        val context = getApplication<Application>()
        val usageStatsManager =
            context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val endTime = System.currentTimeMillis()
        val startTime = endTime - (24 * 60 * 60 * 1000) // last 24 hours

        val statsList = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startTime,
            endTime
        )

        val top5list = statsList.filter { x ->
            x.totalTimeInForeground > 60000 &&
                    x.packageName != context.packageName &&
                    !x.packageName.contains("launcher") &&
                    !x.packageName.contains("systemui")
        }
            .sortedByDescending { x -> x.totalTimeInForeground }
            .take(5)
            .map { x ->
                ScreenTime(
                    app = getAppName(context, x.packageName),
                    time = x.totalTimeInForeground,
                    packageName = x.packageName
                )
            }

        screenTimeData = top5list

    }
}

class ScreenTimeViewModelFactory(
    private val application: Application,
    private val repo: ScreenTimeRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return ScreenTimeViewModel(application, repo) as T
    }
}

@SuppressLint("MissingPermission")
@Composable
fun ScreenTimeScreen(
    navController: NavController,
    screenModel: ScreenTimeViewModel
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                screenModel.checkPermission(context)
                if (screenModel.hasPermission) {
                    screenModel.getTopAppsUsage()
                    screenModel.checkAndNotify(context)
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    if (!screenModel.hasPermission) {
        Column(modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .safeDrawingPadding(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "Please grant permissions to display your screen time!")
            Button(onClick = {screenModel.openPermissionSettings(context)}) {
                Text("Click this to enable permissions")
            }
        }
    } else {
    val sliceColors = listOf(
        Color(0xFF4FC3F7),
        Color(0xFFFFB74D),
        Color(0xFF81C784),
        Color(0xFFFF8A65),
        Color(0xFFCE93D8)
    )
    val pieSlices = screenModel.screenTimeData.mapIndexed { index, screenTime ->
        PieSlice(label = screenTime.app, value = screenTime.time, color = sliceColors[index])
    }
    val totalTimeMili = screenModel.screenTimeData.sumOf { it.time }
    val totalTime = formatDuration(totalTimeMili)
    val targetMinutes by screenModel.targetMinutesFlow.collectAsState()
    val targetMillis = targetMinutes * 60000L
    val overTarget = totalTimeMili > targetMillis

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(8.dp))

        // Screen title
        Row(modifier = Modifier.fillMaxWidth()) {
            HeaderSection("Screen Time")
        }

        Spacer(Modifier.height(20.dp))

        // Today's usage section
        Text(
            text = "Today's Usage",
            style = MaterialTheme.typography.labelMedium,
            color = Color.Gray,
            modifier = Modifier.align(Alignment.Start)
        )
        Text(
            text = totalTime,
            style = MaterialTheme.typography.displayMedium
        )
        Text(
            text = if (overTarget)
                "⚠ Over target by ${formatDuration(totalTimeMili - targetMillis)}"
            else
                "✓ ${formatDuration(targetMillis - totalTimeMili)} remaining",
            color = if (overTarget) Color(0xFFE57373) else Color(0xFF81C784),
            style = MaterialTheme.typography.bodyMedium
        )

        // App Blocking button
        OutlinedButton(
            onClick = { navController.navigate("app_blocking") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                Icons.Default.Block,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text("Manage App Blocking")
        }

        Spacer(Modifier.height(20.dp))

        // Pie chart
        PieChart(
            slices = pieSlices,
            modifier = Modifier
                .size(150.dp)
                .padding(8.dp)
        )

        Spacer(Modifier.height(20.dp))

        // Daily limit section
        Text(
            text = "Daily Limit",
            style = MaterialTheme.typography.labelMedium,
            color = Color.Gray,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(Modifier.height(8.dp))

        val presets = listOf(30, 60, 90, 120, 180, 240)
        var showCustomInput by remember { mutableStateOf(false) }
        var customInput by remember { mutableStateOf("") }
        val isCustomSelected = targetMinutes !in presets

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            presets.forEach { minutes ->
                val isSelected = targetMinutes == minutes && !showCustomInput
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) Color(0xFF4FC3F7) else Color(0xFF2A3A40))
                        .clickable {
                            showCustomInput = false
                            screenModel.setTarget(minutes)
                        }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = formatDuration(minutes * 60000L),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) Color.Black else Color.Gray
                    )
                }
            }

            // Custom button but still under daily limit
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        if (showCustomInput || isCustomSelected) Color(0xFF4FC3F7)
                        else Color(0xFF2A3A40)
                    )
                    .clickable { showCustomInput = !showCustomInput }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isCustomSelected && !showCustomInput)
                        formatDuration(targetMinutes * 60000L)
                    else "Custom",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (showCustomInput || isCustomSelected) Color.Black else Color.Gray
                )
            }
        }

        if (showCustomInput) {
            Spacer(Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = customInput,
                    onValueChange = { customInput = it.filter { c -> c.isDigit() } },
                    label = { Text("Minutes") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                Spacer(Modifier.width(8.dp))
                Button(onClick = {
                    val minutes = customInput.toIntOrNull()
                    if (minutes != null && minutes > 0) {
                        screenModel.setTarget(minutes)
                        showCustomInput = false
                        customInput = ""
                    }
                }) {
                    Text("Set")
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Top apps section
        Text(
            text = "Top Apps",
            style = MaterialTheme.typography.labelMedium,
            color = Color.Gray,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(Modifier.height(8.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            itemsIndexed(screenModel.screenTimeData) { index, screenTime ->
                val progress = screenTime.time.toFloat() / totalTimeMili.toFloat()
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = screenTime.app,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = formatDuration(screenTime.time),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 8.dp),
                        color = sliceColors[index]
                    )
                }
                HorizontalDivider()
            }
        }
    }
}
}