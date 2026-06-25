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
import androidx.compose.ui.text.font.FontFamily
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

val randomColor = Color(
    red = Random.nextFloat(),
    green = Random.nextFloat(),
    blue = Random.nextFloat(),
    alpha = 1f
)

@Composable
fun PieChart(
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
                color = Color.Black.toArgb()
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

            drawContext.canvas.nativeCanvas.drawText(slice.label, xcoord, ycoord, paint)

            startAngle += sweep
        }
    }
}

@Composable
fun PieChartLegend(slices: List<PieSlice>) {
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
                    Text(slice.label + ": " + formatDuration(slice.value))
                }
            }
        }
    }
}

fun formatDuration(milliseconds: Long): String {
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
        Color(0xFF4FC3F7), // sky blue
        Color(0xFFFFB74D), // amber
        Color(0xFF81C784), // soft green
        Color(0xFFFF8A65), // coral
        Color(0xFFCE93D8)  // lavender
    )
    val goals by goalsModel.goals.collectAsState()
    val screenTimeData = screenTimeViewModel.screenTimeData
    val slices = screenTimeData.mapIndexed { index, screenTime ->
        PieSlice(label = screenTime.app,
            value = screenTime.time,
            color = sliceColors[index]) }
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
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Column(modifier = Modifier
        .safeDrawingPadding()
        .fillMaxWidth()) {
        Text(text = formattedDate,
            modifier = Modifier
                .align(Alignment.CenterHorizontally),
            textAlign = TextAlign.Center,
            fontFamily = FontFamily.Serif)
        Text(text = "Today's Screentime",
            modifier = Modifier
                .align(Alignment.CenterHorizontally),
            textAlign = TextAlign.Center,
            fontFamily = FontFamily.Serif)
        if (slices.isEmpty()) {
            Text(
                text = "Tap to grant screen time permission",
                color = Color.Gray,
                textAlign = TextAlign.Center,
                fontFamily = FontFamily.Serif,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clickable { navController.navigate("screentime") {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }}
            )
        } else {
            PieChart(
                slices = slices,
                modifier = Modifier
                    .size(200.dp)
                    .align(Alignment.CenterHorizontally)
                    .padding(16.dp)
                    .clickable {
                        navController.navigate("screentime") {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        } }
            )
            PieChartLegend(slices = slices)
        }
        Row(verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)){
            Text(text = "Your Goals")
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
        if (firstThreeGoals.isEmpty()) {
            Text(text = "No goals yet — add one!",
                color = Color.Gray,
                textAlign = TextAlign.Center,
                fontFamily = FontFamily.Serif,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .fillMaxWidth()
                    .padding(40.dp))
        } else {
            LazyColumn {
                items(firstThreeGoals) { goal ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = goal.goal,
                            modifier = Modifier
                                .weight(1f)
                                .padding(16.dp)
                        )
                        Checkbox(
                            checked = goal.isDone,
                            onCheckedChange = {
                                goalsModel.toggleDone(goal.id)
                            }
                        )
                    }
                    HorizontalDivider()
                }
            }
        }
    }
}