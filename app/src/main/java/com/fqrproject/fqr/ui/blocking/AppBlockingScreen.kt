package com.fqrproject.fqr.ui.blocking

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.fqrproject.fqr.ui.index.formatDuration
import com.fqrproject.fqr.ui.screentime.ScreenTimeViewModel

fun isAccessibilityServiceEnabled(context: Context): Boolean {
    val enabled = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
    ) ?: return false
    // matches both the fully-qualified and the shorthand form
    return enabled.contains(AppBlockerService::class.java.name) ||
            enabled.contains("${context.packageName}/.ui.blocking.AppBlockerService")
}

@Composable
fun AppBlockingScreen(
    navController: NavController,
    blockingModel: BlockingViewModel,
    screenModel: ScreenTimeViewModel
) {
    val context = LocalContext.current
    val blockedApps by blockingModel.blockedApps.collectAsState()
    val blockingEnabled by blockingModel.blockingEnabled.collectAsState()
    val appsWithState = blockingModel.getAppsWithBlockedState()
    var hasOverlayPermission by remember { mutableStateOf(Settings.canDrawOverlays(context)) }
    var hasAccessibilityPermission by remember { mutableStateOf(isAccessibilityServiceEnabled(context)) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasOverlayPermission = Settings.canDrawOverlays(context)
                hasAccessibilityPermission = isAccessibilityServiceEnabled(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(blockedApps, blockingEnabled) {
        blockingModel.syncToSharedPrefs(context)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(horizontal = 20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text("App Blocking", style = MaterialTheme.typography.titleLarge)
        }

        Spacer(Modifier.height(16.dp))

        if (!hasOverlayPermission) {
            Text(
                text = "Overlay permission required",
                color = Color(0xFFFFB74D),
                style = MaterialTheme.typography.bodySmall
            )
            Button(onClick = {
                context.startActivity(
                    Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:${context.packageName}")
                    )
                )
            }) {
                Text("Grant Overlay Permission")
            }
            Spacer(Modifier.height(8.dp))
        }

        if (!hasAccessibilityPermission) {
            Text(
                text = "Accessibility permission required",
                color = Color(0xFFFFB74D),
                style = MaterialTheme.typography.bodySmall
            )
            Button(onClick = {
                context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            }) {
                Text("Grant Accessibility Permission")
            }
            Spacer(Modifier.height(8.dp))
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Enable App Blocking", style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = "Blocks selected apps when screen time target is exceeded",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            Switch(
                checked = blockingEnabled,
                onCheckedChange = { blockingModel.setBlockingEnabled(it) },
                enabled = hasOverlayPermission && hasAccessibilityPermission
            )
        }

        Spacer(Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(Modifier.height(8.dp))

        Text(
            text = "Select Apps to Block",
            style = MaterialTheme.typography.labelMedium,
            color = Color.Gray
        )

        Spacer(Modifier.height(8.dp))

        // Recommendations based on top used apps
        val topApps = screenModel.screenTimeData.take(3)

        if (topApps.isNotEmpty()) {
            Text(
                text = "Recommended to Block",
                style = MaterialTheme.typography.labelMedium,
                color = Color.Gray
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Your most-used apps today",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
            Spacer(Modifier.height(8.dp))

            topApps.forEach { app ->
                val alreadyBlocked = blockedApps.contains(app.packageName)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(app.app, style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = formatDuration(app.time),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                    }
                    if (alreadyBlocked) {
                        Text(
                            text = "Blocked",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF81C784)
                        )
                    } else {
                        Button(
                            onClick = { blockingModel.toggleApp(app.packageName) },
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                                horizontal = 16.dp, vertical = 4.dp
                            )
                        ) {
                            Text("Block", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(Modifier.height(8.dp))
        }

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(appsWithState) { app ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { blockingModel.toggleApp(app.packageName) }
                        .padding(vertical = 8.dp)
                ) {
                    Text(
                        text = app.appName,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    Checkbox(
                        checked = app.isBlocked,
                        onCheckedChange = { blockingModel.toggleApp(app.packageName) }
                    )
                }
                HorizontalDivider()
            }
        }
    }
}