package com.fqrproject.fqr.ui.index

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.fqrproject.fqr.ui.goals.GoalsViewModel
import com.fqrproject.fqr.ui.screentime.ScreenTimeViewModel
import java.time.format.DateTimeFormatter
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

data class PieSlice(
    val label: String,
    val value: Long,
    val color: Color
)

@Composable
fun PieChart( // Screen Time Pie Chart
    slices: List<PieSlice>,
    modifier: Modifier = Modifier
) {
    val total = slices.sumOf { it.value.toDouble() }.toFloat()

    Canvas(modifier = modifier) {
        var startAngle = -90f // start at 12 o'clock instead of 3 o'clock

        slices.forEach { slice ->
            val sweep = (slice.value / total) * 360f
            val midAngle = startAngle + (sweep / 2f)
            val midAngleRad = Math.toRadians(midAngle.toDouble())
            val xcenter = size.width / 2f
            val ycenter = size.height / 2f
            val radius = (minOf(size.width, size.height) / 2f) * 0.65f
            val xcoord = xcenter + radius * cos(midAngleRad.toFloat())
            val ycoord = ycenter + radius * sin(midAngleRad.toFloat())
            val paint = Paint().apply {
                color = Color.White.toArgb()
                textSize = 12.dp.toPx()
                textAlign = Paint.Align.CENTER
            }


            drawArc(
                color = slice.color,
                startAngle = startAngle,
                sweepAngle = sweep,
                useCenter = true,
                size = Size(size.width, size.height)
            )

            if (sweep > 15f) {
                drawContext.canvas.nativeCanvas.drawText(formatDuration(slice.value), xcoord, ycoord, paint)
            }

            startAngle += sweep
        }
    }
}

@Composable
fun PieChartLegend(slices: List<PieSlice>) { // Legend of ScreenTime including Names and Time
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.width(IntrinsicSize.Max),
            horizontalAlignment = Alignment.Start
        ) {
            slices.forEach { slice ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(slice.color)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(slice.label)
                }
            }
        }
    }
}

fun formatDuration(milliseconds: Long): String { // Simple function to format duration from milliseconds to readable format
    val totalMinutes = milliseconds / 60000
    if (totalMinutes >= 60) {
        val hours = totalMinutes / 60
        val remainingMinutes = totalMinutes - hours * 60
        return "${hours}h ${remainingMinutes}m"
    } else {
        return "${totalMinutes}m"
    }
}

@Composable
fun IndexScreen(navController: NavController, goalsModel: GoalsViewModel, screenTimeViewModel: ScreenTimeViewModel) {
    val sliceColors = listOf(
        Color(0xFF4FC3F7),
        Color(0xFFFFB74D),
        Color(0xFF81C784),
        Color(0xFFFF8A65),
        Color(0xFFCE93D8)
    )
    val goals by goalsModel.goals.collectAsState()
    val screenTimeData = screenTimeViewModel.screenTimeData
    val slices = screenTimeData.mapIndexed { index, screenTime ->
        PieSlice(label = screenTime.app, value = screenTime.time, color = sliceColors[index])
    }
    val firstThreeGoals = goals.filter { goal -> !goal.isDone }.take(3)
    val today = java.time.LocalDate.now()
    val formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy")
    val formattedDate = today.format(formatter)
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                screenTimeViewModel.checkPermission(context)
                if (screenTimeViewModel.hasPermission) {
                    screenTimeViewModel.getTopAppsUsage()
                    screenTimeViewModel.checkAndNotify(context)
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Column(
        modifier = Modifier
            .safeDrawingPadding()
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(8.dp))

        // Date header
        Text(
            text = formattedDate,
            style = MaterialTheme.typography.labelMedium,
            color = Color.Gray
        )

        Spacer(Modifier.height(4.dp))

        // Screen time section header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Today's Screen Time",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f)
            )
            if (slices.isNotEmpty()) {
                Text(
                    text = formatDuration(screenTimeData.sumOf { it.time }),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF4FC3F7)
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // Pie chart or permission prompt
        if (slices.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
                    .clickable {
                        navController.navigate("screentime") {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Tap to grant screen time permission",
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                PieChart(
                    slices = slices,
                    modifier = Modifier
                        .size(160.dp)
                        .clickable {
                            navController.navigate("screentime") {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                )
                Spacer(Modifier.width(16.dp))

                Column {
                    slices.forEach { slice ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(slice.color)
                            )
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = slice.label,
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    text = formatDuration(slice.value),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(Modifier.height(12.dp))

        // Goals section
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Your Goals",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = {
                navController.navigate("goals") {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add goal"
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        if (firstThreeGoals.isEmpty()) {
            Text(
                text = "No goals yet — tap + to add one",
                color = Color.Gray,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = 16.dp)
            )
        } else {
            LazyColumn {
                items(firstThreeGoals) { goal ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                navController.navigate("goal_info/${goal.id}")
                            }
                            .padding(vertical = 4.dp)
                    ) {
                        Checkbox(
                            checked = goal.isDone,
                            onCheckedChange = { goalsModel.toggleDone(goal.id) }
                        )
                        Text(
                            text = goal.goal,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    HorizontalDivider()
                }
            }
        }
    }
}