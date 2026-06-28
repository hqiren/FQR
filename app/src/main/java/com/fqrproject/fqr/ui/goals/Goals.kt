package com.fqrproject.fqr.ui.goals

import android.app.Application
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
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
    val isDone: Boolean = false
)

class GoalsViewModel(
    application: Application,
    private val repo: GoalsRepository
) : AndroidViewModel(application) {
    val goals: StateFlow<List<Goal>> = repo.goals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addGoal(text: String) {
        viewModelScope.launch {
            val newList = goals.value + Goal(goal = text)
            repo.saveGoals(newList)
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

    fun editGoal(id: String, newTask: String){
        viewModelScope.launch {
            val newList = goals.value.map { x ->
                if (x.id == id) {
                    val newGoal = x.copy(goal = newTask)
                    newGoal
                } else {
                    x
                }
            }
            repo.saveGoals(newList)
        }
    }

    fun deleteGoal(id: String) {
        viewModelScope.launch {
            val newList = goals.value.filter { screenTime -> screenTime.id != id }
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

    Column(modifier = Modifier.fillMaxWidth().safeDrawingPadding().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text("Edit Goal", style = MaterialTheme.typography.titleLarge)
        }
        Spacer(Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = editedText,
                onValueChange = { text ->
                    editedText = text
                },
                modifier = Modifier
                    .weight(1f)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Button(onClick = {
                if (editedText.isNotBlank()) {
                    goal?.let { goalsModel.editGoal(it.id, editedText) }
                    navController.popBackStack()
                }
            }) {
                Text(text = "Save")
            }
        }
    }
}

@Composable
fun GoalsScreen(navController: NavController, goalsModel: GoalsViewModel) {
    val goals by goalsModel.goals.collectAsState()
    var input by remember {
        mutableStateOf("")
    }

    Column(modifier = Modifier
        .fillMaxWidth()
        .safeDrawingPadding()
    ){
        Row(modifier = Modifier
            .padding(16.dp)
        ) {
            HeaderSection("Goals")
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = input,
                onValueChange = { text ->
                    input = text
                },
                modifier = Modifier
                    .weight(1f)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Button(onClick = {
                if (input.isNotBlank()) {
                    goalsModel.addGoal(input)
                    input = ""
                }
            }) {
                Text(text = "Add")
            }
        }

        LazyColumn {
            items(goals) { goal ->
                Row(verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable {
                            navController.navigate("goal_info/${goal.id}") }) {
                Text(
                    text = goal.goal,
                    textDecoration = if (goal.isDone) TextDecoration.LineThrough else TextDecoration.None,
                    color = if (goal.isDone) Color.Gray else Color.Unspecified,
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
                    IconButton(onClick = {
                        goalsModel.deleteGoal(goal.id)
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
    }
}