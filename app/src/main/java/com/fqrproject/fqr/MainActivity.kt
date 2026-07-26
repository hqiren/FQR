package com.fqrproject.fqr

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fqrproject.fqr.ui.fitness.StepViewModel
import com.fqrproject.fqr.ui.screentime.ScreenTimeRepository
import com.fqrproject.fqr.ui.theme.FQRTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createNotificationChannel(this)

        lifecycleScope.launch {
            val repo = ScreenTimeRepository(this@MainActivity)
            repo.targetMinutes.collect { minutes ->
                getSharedPreferences("blocking_state", MODE_PRIVATE)
                    .edit()
                    .putInt("target_minutes", minutes)
                    .apply()
            }
        }

        setContent {
            FQRTheme {
                val context = LocalContext.current
                val stepViewModel: StepViewModel = viewModel()

                val launcher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    if (isGranted) {
                        stepViewModel.startListening()
                    }
                }

                val notifLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { /* granted or not, nothing special to do */ }

                LaunchedEffect(Unit) {
                    val hasPermission = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACTIVITY_RECOGNITION
                    ) == PackageManager.PERMISSION_GRANTED

                    if (hasPermission) {
                        stepViewModel.startListening()
                    } else {
                        launcher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        val hasNotifPermission = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) == PackageManager.PERMISSION_GRANTED

                        if (!hasNotifPermission) {
                            notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
                        if (!alarmManager.canScheduleExactAlarms()) {
                            context.startActivity(
                                Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                            )
                        }
                    }
                }

                DisposableEffect(Unit) {
                    onDispose {
                        stepViewModel.stopListening()
                    }
                }
                AppNavHost()
            }
        }
    }
}

fun createNotificationChannel(context: Context) {
    val channel = NotificationChannel(
        "screen_time_channel",
        "Screen Time Alerts",
        NotificationManager.IMPORTANCE_DEFAULT
    )
    val manager = context.getSystemService(NotificationManager::class.java)
    manager.createNotificationChannel(channel)
}