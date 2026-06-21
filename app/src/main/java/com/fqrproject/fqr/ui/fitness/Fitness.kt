package com.fqrproject.fqr.ui.fitness

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import java.time.LocalDateTime


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun FitnessScreen(navController: NavController) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // header
        item {
            HeaderSection("Fitness")
        }

        // stats
        item {
            val s1 = StatData("Steps", "6,667",
                Icons.Default.Snowshoeing, Color(0xFF4CAF50))
            val s2 = StatData("Calories", "599",
                Icons.Default.LocalFireDepartment, Color(0xFFFF5722))
            val s3 = StatData("Distance", "6.2 km",
                Icons.Default.Route, Color(0xFF2196F3))

            StatsRow(listOf(s1, s2, s3))

        }

        // daily progress
        item {
            ProgressSection()
        }

        // quick actions
        item {
            QuickActionsSection(navController)
        }

        // latest workouts
        item {
            RecentWorkoutSection(navController)
        }
    }
}

@Composable
fun HeaderSection(
    title: String,
    showBackButton: Boolean = false,
    onBackClick: (() -> Unit)? = null
) {
    Column {
        // Back button if required
        if (showBackButton && onBackClick != null) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBackIosNew,
                    contentDescription = "Back"
                )
            }
        }

        // Title
        Text(
            text = title,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun StatsRow(stats: List<StatData>) { // for steps, calories and distance
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        stats.forEach { stat ->
            StatCard(
                title = stat.title,
                value = stat.value,
                icon = stat.icon,
                color = stat.color,
                modifier = stat.modifier.weight(1f)
            )
        }
    }
}

@Composable
fun StatCard( // design for the stats
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ProgressSection() { // for daily goal progress
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Daily Goal Progress",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // daily steps progress
            ProgressItem(
                label = "Steps",
                current = 6667, // can take from workout once sort out database
                goal = 10000,
                color = Color(0xFF4CAF50)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // daily calories progress
            ProgressItem(
                label = "Calories",
                current = 599, // can take from workout once sort out database
                goal = 800,
                color = Color(0xFFFF5722)
            )
        }
    }
}

@Composable
fun ProgressItem(
    label: String,
    current: Int,
    goal: Int,
    color: Color
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = current.toString() + " / " + goal.toString(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        LinearProgressIndicator(
        progress = { (current.toFloat() / goal.toFloat()).coerceIn(0f, 1f) },
        modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
        color = color,
        trackColor = color.copy(alpha = 0.2f),
        strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
        )
    }
}

@Composable
fun QuickActionsSection(navController: NavController) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column (modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Quick Actions",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ActionButton(
                    text = "",
                    icon = Icons.Default.Info,
                    onClick = { navController.navigate("FitnessSummary") },
                    modifier = Modifier.weight(1f),
                )
                ActionButton(
                    text = "",
                    icon = Icons.Default.AddCircleOutline,
                    onClick = { navController.navigate("LogWorkoutScreen") }, // log new workout manually
                    modifier = Modifier.weight(1f)
                )
                ActionButton(
                    text = "",
                    icon = Icons.Default.Sync,
                    onClick = {  }, // sync workout
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun ActionButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text)
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun RecentWorkoutSection(navController: NavController) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column (modifier = Modifier.padding(16.dp) ){
            Text(
                text = "Recent Workouts",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            val workout = getWorkout(1)
            val workouts = listOf(workout)

            workouts.forEach { workout ->
                WorkoutItem(
                    workout = workout,
                    onClick = { navController.navigate("workout_details/${workout.name}") })
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun WorkoutItem(workout: Workout, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = workout.icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = workout.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = workout.duration.toString() + " min",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "View details",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// data class for the stat classes
data class StatData (
    val title: String,
    val value: String,
    val icon: ImageVector,
    val color: Color,
    val modifier: Modifier = Modifier
)

// Data class for activities/workout
data class Workout(
    val id: Int, // 1,2,3,4,5
    val name: String,
    val type: String,  // cardio, strength others
    val duration: Int, // duration in minutes
    val calories: Int, // e.g., 320
    val distance: Int = 0,  // distance in meters
    val date: LocalDateTime?,
    val notes: String = "",
) {
    val icon: ImageVector // take icon based on workout type
        get() = when (type.lowercase()) {
            "cardio" -> Icons.AutoMirrored.Filled.DirectionsRun
            "strength" -> Icons.Default.FitnessCenter
            else -> Icons.Default.Sports  // "others"
        }
}

@RequiresApi(Build.VERSION_CODES.O)
fun getWorkout(workoutId: Int): Workout {
    val egdate = LocalDateTime.of(2026, 8, 9, 12, 0)
    return Workout(
        id = workoutId,
        name = "Running", type = "Cardio", duration = 43,
        calories = 320, distance = 4000,date = egdate, notes = "oi")
}