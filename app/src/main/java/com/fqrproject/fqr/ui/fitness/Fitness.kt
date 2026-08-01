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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import java.time.LocalDateTime


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun FitnessScreen(navController: NavController, stepViewModel: StepViewModel) {
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

        // stats (step count, est calories and est distance)
        item {
            val context = LocalContext.current
            val stepCount by remember(context) {
                Step.getTodaySteps(context)
            }.collectAsState(initial = 0)

            val s1 = StatData("Steps", stepCount.toString(),
                Icons.Default.Snowshoeing, Color(0xFF50C878))

            val estCals = stepsToCalories(stepCount).toString()
            val s2 = StatData("Est. Calories", estCals,
                Icons.Default.LocalFireDepartment, Color(0xFFFF4500))

            val estDist = "%.2f".format(stepsToDistance(stepCount))
            val s3 = StatData("Est. Distance", estDist + " km",
                Icons.Default.Route, Color(0xFF89CFF0))

            StatsRow(listOf(s1, s2, s3))
        }

        // daily progress ( for calories and the steps daily)
        item {
            val context = LocalContext.current
            val stepCount by remember(context) {
                Step.getTodaySteps(context)
            }.collectAsState(initial = 0)

            ProgressSection(stepCount = stepCount)
        }

        // quick actions (for the summary, log new workout and sync steps)
        item {
            QuickActionsSection(navController, stepViewModel)
        }

        // last 3 workouts
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
        // back button if required (depends on which page user is on)
        if (showBackButton && onBackClick != null) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBackIosNew,
                    contentDescription = "Back"
                )
            }
        }

        // title of page
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
fun ProgressSection(stepCount: Int) { // for daily goal progress
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
                current = stepCount,
                goal = 10000,
                color = Color(0xFF4CAF50)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // daily calories progress
            ProgressItem(
                label = "Calories",
                current = stepsToCalories(stepCount),
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

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun QuickActionsSection(navController: NavController, stepViewModel: StepViewModel) {
    val context = LocalContext.current
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
                    onClick = { navController.navigate("fitness_summary") },
                    modifier = Modifier.weight(1f),
                )
                ActionButton(
                    text = "",
                    icon = Icons.Default.AddCircleOutline,
                    onClick = { navController.navigate("log_workout") }, // log new workout manually
                    modifier = Modifier.weight(1f)
                )
                ActionButton(
                    text = "",
                    icon = Icons.Default.Sync,
                    onClick = {
                        stepViewModel.syncSteps(context) }, // sync steps to database
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
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column (modifier = Modifier.padding(16.dp) ){
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Workouts",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                TextButton(onClick = { navController.navigate("all_workouts") }) {
                    Text("See All")
                }

            }

            val context = LocalContext.current
            val workouts by remember(context) { Workout.getLast3(context) }.collectAsState(initial = emptyList())

            workouts.forEach { workout ->
                WorkoutItem(
                    workout = workout,
                    onClick = { navController.navigate("workout_details/${workout.id}") })
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (workouts.isEmpty()) {
                Text(
                    text = "No workouts yet\nStart by clicking + icon under Quick Actions",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
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


fun stepsToDistance(steps: Int): Double {
    // avg stride 1.4 m
    val distanceInMeters = steps * 1.4
    val distanceInKm = distanceInMeters / 1000
    return distanceInKm
}

fun stepsToCalories(steps: Int): Int {
    // avg 0.045 cal per step
    return (steps * 0.045).toInt()
}