package com.fqrproject.fqr.ui.goals

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val goalText = intent.getStringExtra("goalText") ?: "Goal reminder"
        val goalId = intent.getStringExtra("goalId") ?: return

        val notification = NotificationCompat.Builder(context, "screen_time_channel")
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("Goal Reminder")
            .setContentText("Don't forget: $goalText")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        if (ActivityCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            NotificationManagerCompat.from(context).notify(goalId.hashCode(), notification)
        }
    }
}