package com.fqrproject.fqr.ui.goals

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.fqrproject.fqr.ui.fitness.HeaderSection
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Goal(
    val id: String = UUID.randomUUID().toString(),
    val goal: String,
    val isDone: Boolean = false,
    val dueDate: Long? = null,        // epoch millis, null = no deadline
    val recurrence: String = "None"   // "None", "Daily", "Weekly"
)

class GoalsViewModel(
    application: Application,
    private val repo: GoalsRepository
) : AndroidViewModel(application) {
    val goals: StateFlow<List<Goal>> = repo.goals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addGoal(text: String, dueDate: Long? = null, recurrence: String = "None") {
        viewModelScope.launch {
            val newGoal = Goal(goal = text, dueDate = dueDate, recurrence = recurrence)
            val newList = goals.value + newGoal
            repo.saveGoals(newList)
            if (dueDate != null) {
                ReminderScheduler.schedule(getApplication(), newGoal)
            }
        }
    }

    fun toggleDone(id: String){
        viewModelScope.launch {
            val newList = goals.value.map { x ->
                if (x.id == id) {
                    val newGoal = x.copy(isDone = !x.isDone)
                    newGoal
            } else {
                x
                }
            }
            repo.saveGoals(newList)
        }
    }
    fun editGoal(id: String, newTask: String, dueDate: Long? = null, recurrence: String = "None") {
        viewModelScope.launch {
            val newList = goals.value.map { x ->
                if (x.id == id) x.copy(goal = newTask, dueDate = dueDate, recurrence = recurrence) else x
            }
            repo.saveGoals(newList)
            val updated = newList.find { it.id == id }
            if (updated != null && dueDate != null) {
                ReminderScheduler.schedule(getApplication(), updated)
            }
        }
    }

    fun deleteGoal(id: String) {
        viewModelScope.launch {
            val goalToDelete = goals.value.find { it.id == id }
            if (goalToDelete != null) {
                ReminderScheduler.cancel(getApplication(), goalToDelete)
            }
            val newList = goals.value.filter { it.id != id }
            repo.saveGoals(newList)
        }
    }
}

class GoalsViewModelFactory(
    private val application: Application,
    private val repo: GoalsRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return GoalsViewModel(application, repo) as T
    }
}

@Composable
fun GoalDetailScreen(
    goalId: String,
    goalsModel: GoalsViewModel,
    navController: NavController
) {
    val goals by goalsModel.goals.collectAsState()
    val goal = goals.find { it.id == goalId }

    var editedText by remember { mutableStateOf(goal?.goal ?: "") }
    var dueDate by remember { mutableStateOf(goal?.dueDate) }
    var recurrence by remember { mutableStateOf(goal?.recurrence ?: "None") }
    var showDatePicker by remember { mutableStateOf(false) }

    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize().safeDrawingPadding().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text("Edit Goal", style = MaterialTheme.typography.titleLarge)
        }

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = editedText,
            onValueChange = { editedText = it },
            label = { Text("Goal") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        // Due date
        Text("Due Date", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = dueDate?.let {
                    java.time.Instant.ofEpochMilli(it)
                        .atZone(java.time.ZoneId.systemDefault())
                        .toLocalDate()
                        .format(java.time.format.DateTimeFormatter.ofPattern("MMM d, yyyy"))
                } ?: "No deadline set",
                modifier = Modifier.weight(1f)
            )
            Button(onClick = { showDatePicker = true }) {
                Text(if (dueDate == null) "Set Date" else "Change")
            }
        }

        Spacer(Modifier.height(16.dp))

        // Recurrence
        Text("Repeat", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
        Spacer(Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("None", "Daily", "Weekly").forEach { option ->
                val selected = recurrence == option
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (selected) Color(0xFF4FC3F7) else Color(0xFF2A3A40))
                        .clickable { recurrence = option }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        option,
                        color = if (selected) Color.Black else Color.Gray,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                if (editedText.isNotBlank()) {
                    goal?.let {
                        goalsModel.editGoal(it.id, editedText, dueDate, recurrence)
                    }
                    navController.popBackStack()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save")
        }
    }

    // Date picker dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dueDate = datePickerState.selectedDateMillis
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
@Composable
fun GoalsScreen(navController: NavController, goalsModel: GoalsViewModel) {
    val goals by goalsModel.goals.collectAsState()
    var goalToDelete by remember {
        mutableStateOf<Goal?>(null)
    }
    var input by remember {
        mutableStateOf("")
    }
    var showGoalDetails by remember {
        mutableStateOf(false)
    }
    var dueDate by remember { mutableStateOf<Long?>(null) }
    var recurrence by remember { mutableStateOf("None") }
    var showDatePicker by remember { mutableStateOf(false) }

    Column(modifier = Modifier
        .safeDrawingPadding()
        .fillMaxSize()
    ){
        Row(modifier = Modifier
            .padding(16.dp)
        ) {
            HeaderSection("Goals")
        }

        // Add goal button with input
        Button(
            onClick = { showGoalDetails = true },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        ) {
            Text("+ Add Goal")
        }

        // Shows number of goals + how many are completed
        Text(
            text = "${goals.size} goals · ${goals.count { it.isDone }} completed",
            style = MaterialTheme.typography.labelMedium,
            color = Color.Gray,
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp)
        )

        // List showing the goals to complete, with due date (sorted)
        val sortedGoals = goals.sortedWith(
            compareBy({ it.isDone }, { it.dueDate == null }, { it.dueDate })
        )

        LazyColumn {
            items(sortedGoals) { goal ->
                Row(verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable {
                            navController.navigate("goal_info/${goal.id}") }) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = goal.goal,
                            textDecoration = if (goal.isDone) TextDecoration.LineThrough else TextDecoration.None,
                            color = if (goal.isDone) Color.Gray else Color.Unspecified,
                            modifier = Modifier.padding(16.dp)
                        )
                        goal.dueDate?.let { due ->
                            Text(
                                text = "Due: " + java.time.Instant.ofEpochMilli(due)
                                    .atZone(java.time.ZoneId.systemDefault())
                                    .toLocalDate()
                                    .format(java.time.format.DateTimeFormatter.ofPattern("MMM d")),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFFFB74D),
                                modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                            )
                        }
                    }
                Checkbox(
                    checked = goal.isDone,
                    onCheckedChange = {
                        goalsModel.toggleDone(goal.id)
                    }
                )
                    // Delete goal button
                    IconButton(onClick = {
                        goalToDelete = goal
                    }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete goal"
                        )
                    }
                }
                HorizontalDivider()
            }
        }

        // Confirmation dialog for deletion of goals
        if (goalToDelete != null) {
            AlertDialog(
                onDismissRequest = { goalToDelete = null },
                title = { Text("Delete goal") },
                text = { Text("Would you like to delete this goal?") },
                confirmButton = { TextButton(onClick = {
                    goalToDelete?.let { goal ->
                        goalsModel.deleteGoal(goal.id)
                    } // Safeguards that goalToDelete is not null
                    goalToDelete = null
                }) { Text("Delete") } },
                dismissButton = { TextButton(onClick = {
                    goalToDelete = null
                }) { Text("Cancel") } }
            )
        }

        // Add goals details log for adding of goals
        if (showGoalDetails) {
            AlertDialog(
                onDismissRequest = { showGoalDetails = false },
                title = { Text("Add goal details") },
                text = {
                    // Column for date picker and recurrence
                    Column(modifier = Modifier.fillMaxSize().safeDrawingPadding().padding(16.dp)) {
                        // Text field to add goal name
                        OutlinedTextField(
                            value = input,
                            onValueChange = { text -> input = text },
                            placeholder = { Text("Add a new goal...") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(16.dp))

                        // Due date
                        Text("Due Date", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                        Spacer(Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = dueDate?.let {
                                    java.time.Instant.ofEpochMilli(it)
                                        .atZone(java.time.ZoneId.systemDefault())
                                        .toLocalDate()
                                        .format(java.time.format.DateTimeFormatter.ofPattern("MMM d, yyyy"))
                                } ?: "No deadline set",
                                modifier = Modifier.weight(1f)
                            )
                            Button(onClick = { showDatePicker = true }) {
                                Text(if (dueDate == null) "Set Date" else "Change")
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // Recurrence
                        Text("Repeat", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                        Spacer(Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("None", "Daily", "Weekly").forEach { option ->
                                val selected = recurrence == option
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(if (selected) Color(0xFF4FC3F7) else Color(0xFF2A3A40))
                                        .clickable { recurrence = option }
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        option,
                                        color = if (selected) Color.Black else Color.Gray,
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = { TextButton(onClick = {
                    if (input != "") {
                        goalsModel.addGoal(text=input, dueDate=dueDate, recurrence=recurrence)
                        input = ""
                        dueDate = null
                        recurrence = ""
                        showGoalDetails = false
                    }
                }) { Text("Add Goal") } },
                dismissButton = { TextButton(onClick = {
                    showGoalDetails = false
                }) { Text("Cancel") } }
            )
        }

        // Show date picker
        if (showDatePicker) {
            val datePickerState = rememberDatePickerState()
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        dueDate = datePickerState.selectedDateMillis
                        showDatePicker = false
                    }) { Text("OK") }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }
    }
}