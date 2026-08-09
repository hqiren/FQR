package com.fqrproject.fqr.ui.fitness

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Sports
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.first
import java.time.LocalDateTime

@RequiresApi(Build.VERSION_CODES.O)
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
            val s2 = StatData("Avg time per workout",
                "%.2f".format(avgDuration) + " min",
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


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun RecommendationSection() {
    // based on healthhub recommendation of 2 days strength and 150-300 min cardio per week
    val context = LocalContext.current
    var recommendationTxt by remember { mutableStateOf("Analyzing your activity...") }

    LaunchedEffect(Unit) {
        val allWorkouts = Workout.getAll(context).first()
        val now = LocalDateTime.now()
        val sevenDaysAgo = now.minusDays(7)

        // get last 7 days workouts
        val lastWeekWorkouts = allWorkouts.filter { 
            it.date != null && it.date.isAfter(sevenDaysAgo) 
        }

        // get cardio minutes from last 7 days
        val cardioMinutes = lastWeekWorkouts
            .filter { it.type == "cardio" }
            .sumOf { it.duration }

        // get strength sessions from last 7 days
        val strengthSessions = lastWeekWorkouts
            .count { it.type == "strength" }

        val cardioRemaining = 200 - cardioMinutes // remaining to check or suggest to user
        val strengthRemaining = 2 - strengthSessions
        
        // use 7 day window
        // if the user meets requirement in last 7 days, will print good job message
        // else will prompt recommendation to user
        var recs = ""
        if (cardioRemaining > 0) {
            recs += "Aim for $cardioRemaining minutes more cardio\n"
        }
        if (strengthRemaining > 0) {
            val unit = if (strengthRemaining == 1) "session" else "sessions"
            recs += "Aim for $strengthRemaining more strength training $unit\n"
        }

        if (recs == "") { // goal completed
            recommendationTxt = "Good job! Your workouts are aligned with HealthHub's recommendations.\n"
        } else {
            recommendationTxt = recs
        }
    }

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
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = recommendationTxt,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}