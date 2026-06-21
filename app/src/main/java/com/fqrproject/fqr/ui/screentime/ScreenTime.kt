package com.fqrproject.fqr.ui.screentime

import android.Manifest
import android.app.Application
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import androidx.annotation.RequiresPermission
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import java.sql.Time

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
class ScreenTimeViewModel(application: Application) : AndroidViewModel(application) {
    @RequiresPermission(Manifest.permission.PACKAGE_USAGE_STATS)
    fun getTopAppsUsage(): List<ScreenTime> {
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

        return top5list
    }
}