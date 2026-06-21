package com.fqrproject.fqr.ui.fitness

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun LogWorkoutScreen(onBackClick: () -> Unit, onSave: (Workout) -> Unit) {
    // rmb state of fields
    var workoutName by remember { mutableStateOf("") }
    var workoutType by remember { mutableStateOf("cardio") }
    var workoutDuration by remember { mutableStateOf("") }
    var workoutCalories by remember { mutableStateOf("") }
    var workoutDist by remember { mutableStateOf("") }
    var workoutNotes by remember { mutableStateOf("") }
    //var workoutDate by remember { mutableStateOf(LocalDateTime.now()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // title
        HeaderSection(
            title = "Log Workout",
            showBackButton = true,
            onBackClick = onBackClick
        )

        //  name of workout
        OutlinedTextField(
            value = workoutName,
            onValueChange = { workoutName = it },
            label = { Text("Workout Name") },
            placeholder = { Text("e.g. Gym") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = {
                Icon(Icons.Default.FitnessCenter, contentDescription = null)
            }
        )

        // workout type
        WorkoutTypeDropdown(
            selectedType = workoutType,
            onTypeSelected = { workoutType = it }
        )

        // duration in minutes
        OutlinedTextField(
            value = workoutDuration,
            onValueChange = { workoutDuration = it },
            label = { Text("Duration (minutes)") },
            placeholder = { Text("e.g. 30") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            leadingIcon = {
                Icon(Icons.Default.Timer, contentDescription = null)
            }
        )

        // calories
        OutlinedTextField(
            value = workoutCalories,
            onValueChange = { workoutCalories = it},
            label = { Text("Calories Burned") },
            placeholder = { Text("e.g. 270") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            leadingIcon = {
                Icon(Icons.Default.LocalFireDepartment, contentDescription = null)
            }
        )

        // distance in meters (optional)
        OutlinedTextField(
            value = workoutDist,
            onValueChange = { workoutDist = it},
            label = { Text("Distance (meters) - Optional") },
            placeholder = { Text("e.g. 5000") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            leadingIcon = {
                Icon(Icons.Default.Route, contentDescription = null)
            }
        )

        // workout date and time
        //DateTimeSelector()

        // workout notes (optional)
        OutlinedTextField(
            value = workoutNotes,
            onValueChange = { workoutNotes = it },
            label = { Text("Notes (Optional)") },
            placeholder = { Text("Anything worth noting?") },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            leadingIcon = {
                Icon(Icons.Default.Pending, contentDescription = null)
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // save workout
        Button(
            onClick = {}, // save to database
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
        ) {
            Text("Save Workout", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
fun WorkoutTypeDropdown(
    selectedType: String,
    onTypeSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    //map to list of pairs
    val workoutTypes = listOf(
        "cardio" to Icons.AutoMirrored.Filled.DirectionsRun,
        "strength" to Icons.Default.FitnessCenter,
        "others" to Icons.Default.Sports
    )

    Column {
        Text(
            text = "Workout Type",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
        )

        OutlinedCard(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val selectedIcon = when (selectedType) {
                        "cardio" -> Icons.AutoMirrored.Filled.DirectionsRun
                        "strength" -> Icons.Default.FitnessCenter
                        else -> Icons.Default.Sports
                    }

                    Icon(
                        imageVector = selectedIcon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = selectedType,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = "Expand"
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            workoutTypes.forEach { (type, icon) ->
                DropdownMenuItem(
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = if (type == selectedType)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = type,
                                fontWeight = if (type == selectedType)
                                    androidx.compose.ui.text.font.FontWeight.Bold
                                else
                                    androidx.compose.ui.text.font.FontWeight.Normal
                            )
                        }
                    },
                    onClick = {
                        onTypeSelected(type)
                        expanded = false
                    }
                )
            }
        }
    }
}

