package com.fqrproject.fqr.ui.fitness

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Sports
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime


//workout class/data structure
@Entity(tableName = "workouts")
@TypeConverters(Converters::class)
data class Workout(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0, // auto generate
    val name: String,
    val type: String,  // cardio, strength others
    val duration: Int, // duration minutes
    val calories: Int,
    val distance: Int = 0,  // distance meters
    val date: LocalDateTime?,
    val notes: String = ""
) {
    val icon: ImageVector // take icon based on workout type
        get() = when (type.lowercase()) {
            "cardio" -> Icons.AutoMirrored.Filled.DirectionsRun
            "strength" -> Icons.Default.FitnessCenter
            else -> Icons.Default.Sports
        }

    // functions for getting/updating data to db
    companion object {
        private fun getDao(context: Context): WorkoutDao {
            return WorkoutDatabase.getDatabase(context).workoutDao()
        }

        // get last 3 workouts (for the recent workouts view)
        fun getLast3(context: Context): Flow<List<Workout>> {
            return getDao(context).getAllWorkouts().map { it.take(3) }
        }

        // get all workouts
        fun getAll(context: Context): Flow<List<Workout>> {
            return getDao(context).getAllWorkouts()
        }


        // insert workout
        suspend fun insert(context: Context, workout: Workout) {
            getDao(context).insertWorkout(workout)
        }

        // update workout
        suspend fun update(context: Context, workout: Workout) {
            getDao(context).updateWorkout(workout)
        }

        // get by id
        suspend fun getById(context: Context, id: Int): Workout? {
            return getDao(context).getWorkoutById(id)
        }

        // get fave workout type (i.e. cardio, strength, others)
        suspend fun getFavouriteType(context: Context): String? {
            val allWorkouts = getDao(context).getAllWorkouts().first()
            if (allWorkouts.isEmpty()) return "NIL"

            val typeCount = allWorkouts.groupBy { it.type }
                .mapValues { it.value.size }
            return typeCount.maxByOrNull { it.value }?.key
        }

        // get avg duration per workout
        suspend fun getAverageDuration(context: Context): Double {
            val allWorkouts = getDao(context).getAllWorkouts().first()
            if (allWorkouts.isEmpty()) return 0.0

            return allWorkouts.map { it.duration }.average()
        }

        suspend fun delete(context: Context, workout: Workout) {
            getDao(context).deleteWorkout(workout)
        }
    }
}
