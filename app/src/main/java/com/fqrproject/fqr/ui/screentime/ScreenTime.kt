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
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.fqrproject.fqr.ui.index.PieChart
import com.fqrproject.fqr.ui.index.PieSlice
import com.fqrproject.fqr.ui.index.formatDuration
import kotlin.collections.emptyList

data class ScreenTime(
    val app: String,
    val time: Long,
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
class ScreenTimeViewModel(application: Application) : AndroidViewModel(application) {
    var hasPermission by mutableStateOf(false)
    var screenTimeData by mutableStateOf<List<ScreenTime>>(emptyList())

    var targetMinutes by mutableStateOf(120) // default 2 hours
        private set

    fun setTarget(minutes: Int) {
        targetMinutes = minutes
    }

    fun checkPermission(context: Context) {
        hasPermission = hasUsagePermission(context)
    }

    fun openPermissionSettings(context: Context) {
        context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
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

        val top5list = statsList.filter { x -> x.totalTimeInForeground > 0 }
            .sortedByDescending { x -> x.totalTimeInForeground }
            .take(5)
            .map { x -> ScreenTime(app = getAppName(context, x.packageName), time = x.totalTimeInForeground) }

        screenTimeData = top5list

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
    val targetMillis = screenModel.targetMinutes * 60000L
    val overTarget = totalTimeMili > targetMillis
    var sliderValue by remember { mutableStateOf(screenModel.targetMinutes.toFloat()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(8.dp))

        // Screen title
        Text(
            text = "Screen Time",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(Modifier.height(16.dp))

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

        Spacer(Modifier.height(16.dp))

        // Pie chart
        PieChart(
            slices = pieSlices,
            modifier = Modifier
                .size(180.dp)
                .padding(8.dp)
        )

        Spacer(Modifier.height(16.dp))

        // Daily limit section
        Text(
            text = "Daily Limit",
            style = MaterialTheme.typography.labelMedium,
            color = Color.Gray,
            modifier = Modifier.align(Alignment.Start)
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Slider(
                value = sliderValue,
                onValueChange = { sliderValue = it },
                valueRange = 30f..480f,
                steps = 14,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = formatDuration(sliderValue.toInt() * 60000L),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.width(56.dp)
            )
        }
        Button(
            onClick = { screenModel.setTarget(sliderValue.toInt()) },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Save")
        }

        Spacer(Modifier.height(16.dp))

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