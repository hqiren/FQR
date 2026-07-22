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
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter



@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun LogWorkoutScreen(
    onBackClick: () -> Unit,
    workoutToEdit: Workout? = null // null means logging new workout
) {
    // rmb state of fields, init with workout details if editing
    var workoutName by remember { mutableStateOf(workoutToEdit?.name ?: "") }
    var workoutType by remember { mutableStateOf(workoutToEdit?.type ?: "cardio") }
    var workoutDuration by remember { mutableStateOf(workoutToEdit?.duration?.toString() ?: "") }
    var workoutCalories by remember { mutableStateOf(workoutToEdit?.calories?.toString() ?: "") }
    var workoutDist by remember { mutableStateOf(workoutToEdit?.distance?.toString() ?: "") }
    var workoutNotes by remember { mutableStateOf(workoutToEdit?.notes ?: "") }
    var workoutDate by remember { mutableStateOf(workoutToEdit?.date ?: LocalDateTime.now()) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

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
            title = if (workoutToEdit == null) {
                "Log Workout"
            } else {
                "Edit Workout"
            },
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
        DateTimeSelector(
            selectedDateTime = workoutDate,
            onDateTimeSelected = { newDateTime ->
                workoutDate = newDateTime
            }
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

        // save workout to db
        Button(
            onClick = {
                if (workoutName.isNotBlank() && workoutDuration.isNotBlank() && workoutCalories.isNotBlank()) {
                    val workout = Workout(
                        id = workoutToEdit?.id ?: 0, // use current id if is editing
                        name = workoutName,
                        type = workoutType,
                        duration = workoutDuration.toIntOrNull() ?: 0,
                        calories = workoutCalories.toIntOrNull() ?: 0,
                        distance = workoutDist.toIntOrNull() ?: 0,
                        date = workoutDate,
                        notes = workoutNotes
                    )

                    scope.launch {
                        if (workoutToEdit == null) {
                            Workout.insert(context, workout)
                        } else {
                            Workout.update(context, workout)
                        }
                        onBackClick()
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
        ) {
            Text(
                text = if (workoutToEdit == null) "Save Workout" else "Update Workout",
                style = MaterialTheme.typography.titleMedium
            )
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


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DateTimeSelector(
    selectedDateTime: LocalDateTime,
    onDateTimeSelected: (LocalDateTime) -> Unit
) {
    val context = LocalContext.current
    var currentDateTime by remember { mutableStateOf(selectedDateTime) }

    val showDatePicker = { // date picker
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                currentDateTime = LocalDateTime.of(
                    year,
                    month + 1,
                    dayOfMonth,
                    currentDateTime.hour,
                    currentDateTime.minute
                )
                onDateTimeSelected(currentDateTime)
            },
            currentDateTime.year,
            currentDateTime.monthValue - 1, // starts from 0
            currentDateTime.dayOfMonth
        ).show()
    }

    val showTimePicker = { // time picker
        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                currentDateTime = currentDateTime.withHour(hourOfDay).withMinute(minute)
                onDateTimeSelected(currentDateTime)
            },
            currentDateTime.hour,
            currentDateTime.minute,
            false
        ).show()
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Start Date & Time",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface( // date card
                modifier = Modifier
                    .weight(1f)
                    .clickable { showDatePicker() }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Date",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = currentDateTime.format(
                                DateTimeFormatter.ofPattern("MMM dd, yyyy")
                            ),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Surface( // time card
                modifier = Modifier
                    .weight(1f)
                    .clickable { showTimePicker() }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Time",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = currentDateTime.format(
                                DateTimeFormatter.ofPattern("hh:mm a") // e.g. 07:44 AM
                            ),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

