package com.fqrproject.fqr.ui.goals

import android.R.attr.name
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.fqrproject.fqr.ui.fitness.HeaderSection
import java.util.UUID

data class Goal(
    val id: String = UUID.randomUUID().toString(),
    val goal: String,
    val isDone: Boolean = false
)

class GoalsViewModel : ViewModel() {
    var goals by mutableStateOf<List<Goal>>(emptyList())
        private set

    fun addGoal(text: String) {
        val newGoal = Goal(goal = text)
        goals = goals + newGoal
    }

    fun toggleDone(id: String){
        goals = goals.map{ x ->
            if (x.id == id) {
                val newGoal = x.copy(isDone = !x.isDone)
                newGoal
            } else {
                x
            }
        }
    }
}

@Composable
fun GoalsScreen(navController: NavController, goalsModel: GoalsViewModel) {
    val goals = goalsModel.goals
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