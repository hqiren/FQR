package com.fqrproject.fqr.ui.fitness

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Sports
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun WorkoutDetailsScreen(workoutId: Int, onBackClick: () -> Unit) {
    var workout by remember { mutableStateOf<Workout?>(null) }
    val context = LocalContext.current

    LaunchedEffect(workoutId) {
        workout = Workout.getById(context, workoutId)
    }

    if (workout == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                HeaderSection(
                    // definitely got workout, so can use !! to force
                    title = workout!!.name,
                    showBackButton = true,
                    onBackClick = onBackClick
                )
            }

            // workout type (cardio, strength etc)
            item {
                WorkoutTypeBadge(workout!!)
            }

            // stats
            item {
                val s1 = StatData(
                    "Duration", workout!!.duration.toString() + " min",
                    Icons.Default.Timer, Color(0xFF4CAF50)
                )
                val s2 = StatData(
                    "Calories", workout!!.calories.toString(),
                    Icons.Default.LocalFireDepartment, Color(0xFFFF5722)
                )
                val s3 = StatData(
                    "Distance", workout!!.distance.toString() + " m",
                    Icons.Default.Route, Color(0xFF2196F3)
                )
                StatsRow(listOf(s1, s2, s3))
            }

            // workout details
            item {
                WorkoutDetailsCard(workout!!)
            }

            //  workout notes
            if (workout!!.notes.isNotEmpty()) {
                item {
                    NotesSection(workout!!.notes)
                }
            }

                // edit/delete Buttons
                // skip for now
                /*item {
                WorkoutActions()
            }*/
        }
    }

}

@Composable
fun WorkoutTypeBadge(workout: Workout) {
    Button(onClick = { }, // show breakdown on number of types of workout
        shape = CircleShape,
        modifier = Modifier.padding(4.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) { Icon(
        imageVector = workout.icon,
        contentDescription = null,
        modifier = Modifier.size(16.dp)
    )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = workout.type,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
    )}

}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun WorkoutDetailsCard(workout: Workout) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Workout Details",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")
            DetailRow("Date", workout.date?.format(dateFormatter) ?: "NIL")
            Spacer(modifier = Modifier.height(8.dp))
            DetailRow("Type", workout.type)
            Spacer(modifier = Modifier.height(8.dp))
            DetailRow("Duration", workout.duration.toString() + " min")
            Spacer(modifier = Modifier.height(8.dp))
            DetailRow("Calories Burned", workout.calories.toString())

            if (workout.distance != 0) {
                Spacer(modifier = Modifier.height(8.dp))
                DetailRow("Distance", workout.distance.toString() + " m")
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun NotesSection(notes: String) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Notes",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = notes,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun WorkoutActions() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = { }, // edit workout
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text("Edit Workout")
        }

        OutlinedButton(
            onClick = { }, // delete workout
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Delete Workout")
        }
    }
}
