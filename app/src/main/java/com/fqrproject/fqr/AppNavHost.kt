package com.fqrproject.fqr

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.fqrproject.fqr.ui.fitness.FitnessScreen
import com.fqrproject.fqr.ui.fitness.FitnessSummary
import com.fqrproject.fqr.ui.fitness.LogWorkoutScreen
import com.fqrproject.fqr.ui.fitness.WorkoutDetailsScreen
import com.fqrproject.fqr.ui.goals.GoalsRepository
import com.fqrproject.fqr.ui.goals.GoalsScreen
import com.fqrproject.fqr.ui.goals.GoalsViewModel
import com.fqrproject.fqr.ui.goals.GoalsViewModelFactory
import com.fqrproject.fqr.ui.index.IndexScreen
import com.fqrproject.fqr.ui.screentime.ScreenTimeScreen
import com.fqrproject.fqr.ui.screentime.ScreenTimeViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val app =  LocalContext.current.applicationContext
    val repo = GoalsRepository(LocalContext.current)
    val viewModel: GoalsViewModel = viewModel(factory = GoalsViewModelFactory(app as Application, repo))
    val screenModel: ScreenTimeViewModel = viewModel()

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = currentRoute == "index",
                    onClick = {
                        navController.navigate("index") {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") }
                )
                NavigationBarItem(
                    selected = currentRoute == "screentime",
                    onClick = {
                        navController.navigate("screentime") {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(Icons.Default.AccessTime, contentDescription = "Screen Time") },
                    label = { Text("Screen Time") }
                )
                NavigationBarItem(
                    selected = currentRoute == "goals",
                    onClick = {
                        navController.navigate("goals") {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(Icons.Default.DateRange, contentDescription = "Goals") },
                    label = { Text("Goals") }
                )
                NavigationBarItem(
                    selected = currentRoute == "fitness",
                    onClick = {
                        navController.navigate("fitness") {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(Icons.Default.FitnessCenter, contentDescription = "Fitness") },
                    label = { Text("Fitness") }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "index",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("index") { IndexScreen(navController, viewModel, screenModel) }
            composable("screentime") { ScreenTimeScreen(navController, screenModel) }
            composable("goals") { GoalsScreen(navController, viewModel) }
            composable ("fitness") { FitnessScreen(navController) }

            // within fitness page
            composable("FitnessSummary") {
                FitnessSummary(
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable("LogWorkoutScreen") {
                LogWorkoutScreen(
                    onBackClick = { navController.popBackStack() },
                    onSave = {} // save to database
                )
            }

            composable("workout_details/{workoutId}") { backStackEntry ->
                val workoutId = backStackEntry.arguments?.getInt("workoutId") ?: 1
                WorkoutDetailsScreen(
                    workoutId = workoutId,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}