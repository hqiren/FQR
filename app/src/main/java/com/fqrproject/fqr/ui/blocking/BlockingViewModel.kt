package com.fqrproject.fqr.ui.blocking

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString

data class App(
    val packageName: String,
    val appName: String,
    val isBlocked: Boolean = false
)

class BlockingViewModel(
    application: Application,
    private val repo: BlockingRepository
) : AndroidViewModel(application) {

    val blockedApps: StateFlow<List<String>> = repo.apps
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val blockingEnabled: StateFlow<Boolean> = repo.blockingEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    var installedApps by mutableStateOf<List<App>>(emptyList())
        private set

    init {
        loadInstalledApps()
    }

    private fun loadInstalledApps() {
        viewModelScope.launch {
            val context = getApplication<Application>()
            val pm = context.packageManager

            val launcherIntent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }

            val installed = pm.queryIntentActivities(launcherIntent, 0)
                .map { resolveInfo -> resolveInfo.activityInfo.applicationInfo }
                .distinctBy { it.packageName }
                .filter { it.packageName != context.packageName }   // don't block ourselves
                .map { appInfo ->
                    App(
                        packageName = appInfo.packageName,
                        appName = pm.getApplicationLabel(appInfo).toString()
                    )
                }
                .sortedBy { it.appName.lowercase() }

            installedApps = installed
        }
    }

    fun toggleApp(packageName: String) {
        viewModelScope.launch {
            val current = blockedApps.value
            val updated = if (current.contains(packageName)) {
                current.filter { it != packageName }
            } else {
                current + packageName
            }
            repo.saveBlockedApps(updated)
        }
    }

    fun setBlockingEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repo.setBlockingEnabled(enabled)
        }
    }

    // Call this after blockedApps updates to sync isBlocked state
    fun getAppsWithBlockedState(): List<App> {
        return installedApps.map { app ->
            app.copy(isBlocked = blockedApps.value.contains(app.packageName))
        }
    }

    fun syncToSharedPrefs(context: Context) {
        val prefs = context.getSharedPreferences("blocking_state", Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean("blocking_enabled", blockingEnabled.value)
            .putString("blocked_apps",
                kotlinx.serialization.json.Json.encodeToString(blockedApps.value))
            .apply()
    }
}

class BlockingViewModelFactory(
    private val application: Application,
    private val repo: BlockingRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return BlockingViewModel(application, repo) as T
    }
}