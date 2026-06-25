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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.fqrproject.fqr.ui.index.PieChart
import com.fqrproject.fqr.ui.index.PieSlice
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
            Color(0xFF4FC3F7), // sky blue
            Color(0xFFFFB74D), // amber
            Color(0xFF81C784), // soft green
            Color(0xFFFF8A65), // coral
            Color(0xFFCE93D8)  // lavender
        )
        val pieSlices = screenModel.screenTimeData.mapIndexed { index, screenTime ->
            PieSlice(label = screenTime.app,
                value = screenTime.time,
                color = sliceColors[index]) }
        PieChart(pieSlices)
    }
}