package com.fqrproject.fqr.ui.fitness

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun AllWorkoutsScreen(navController: NavController, onBackClick: () -> Unit) {
    // new screen with all workouts
    val context = LocalContext.current
    val workouts by remember(context) { Workout.getAll(context) }.collectAsState(initial = emptyList())

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // header w the back button
        item {
            HeaderSection(
                title = "All Workouts",
                showBackButton = true,
                onBackClick = onBackClick
            )
        }

        if (workouts.isEmpty()) { // in case no workouts recorded yet
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "No workouts yet\nStart by clicking + icon under Quick Actions",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            // use back the prev formatting
            items(workouts) { workout ->
                WorkoutItem(
                    workout = workout,
                    onClick = { navController.navigate("workout_details/${workout.id}") }
                )
            }
        }
    }
}