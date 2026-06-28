package com.fqrproject.fqr.ui.fitness

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

@Entity(tableName = "steps")
@TypeConverters(Converters::class)
data class Step(
    @PrimaryKey
    val date: LocalDate,
    val stepCount: Int,
    val lastUpdated: Long = System.currentTimeMillis()
) {
    companion object {
        private fun getDao(context: Context): StepDao {
            return WorkoutDatabase.getDatabase(context).stepDao() // get step section only
        }

        @RequiresApi(Build.VERSION_CODES.O)
        fun getTodaySteps(context: Context): Flow<Int> {
            val today = LocalDate.now()
            return getDao(context).getStepsByDate(today).map { it?.stepCount ?: 0 }
        }

        @RequiresApi(Build.VERSION_CODES.O)
        suspend fun updateTodaySteps(context: Context, steps: Int) {
            val today = LocalDate.now()
            val stepEntry = Step(date = today, stepCount = steps)
            getDao(context).insertOrUpdateStep(stepEntry)
        }

        @RequiresApi(Build.VERSION_CODES.O)
        suspend fun getStepsByDate(context: Context, date: LocalDate): Int? {
            return getDao(context).getStepsByDateOnce(date)?.stepCount
        }

        suspend fun getAverageSteps(context: Context): Int {
            val stepsWithData = getDao(context).getStepsWithData()
            if (stepsWithData.isEmpty()) return 0

            return stepsWithData.map { it.stepCount }.average().toInt()
        }
    }
}
