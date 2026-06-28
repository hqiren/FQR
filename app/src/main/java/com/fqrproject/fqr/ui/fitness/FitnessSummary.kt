package com.fqrproject.fqr.ui.fitness

import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Snowshoeing
import androidx.compose.material.icons.filled.Sports
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun FitnessSummary(onBackClick: () -> Unit) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // top part
        item {
            HeaderSection("Fitness Summary", true,
                onBackClick = onBackClick)
        }

        // stats
        item {

            val context = LocalContext.current

            // fave workout type
            var woType: String? by remember { mutableStateOf("NIL") }
            LaunchedEffect(Unit) {
                woType = Workout.getFavouriteType(context)
            }

            // avg time per workout
            var avgDuration by remember { mutableStateOf(0.0) }

            LaunchedEffect(Unit) {
                avgDuration = Workout.getAverageDuration(context)
            }

            // avg steps per day
            var avgStepCount by remember { mutableIntStateOf(0) }

            LaunchedEffect(Unit) {
                avgStepCount = Step.getAverageSteps(context)
            }

            val s1 = StatData("Your Favourite Workout Type", woType!!,
                Icons.Default.Sports, Color(0xFF4CAF50)
            )
            val s2 = StatData("Avg time per workout", avgDuration.toString() + " min",
                Icons.Default.Timer, Color(0xFFFF5722)
            )
            val s3 = StatData("Avg Distance per day",
                "%.2f".format(stepsToDistance(avgStepCount)) + " km",
                Icons.Default.Route, Color(0xFF2196F3)
            )
            StatsRow(listOf(s1, s2, s3))
        }

        // recommendations
        item {
            RecommendationSection()
        }
    }
}


@Composable
fun RecommendationSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Personalised Recommendation",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(20.dp)) // formatting

            Text(
                text = "\n" + "More Strength Training" + "\n\n", // formatting
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}