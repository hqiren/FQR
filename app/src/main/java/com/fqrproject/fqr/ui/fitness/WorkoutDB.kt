package com.fqrproject.fqr.ui.fitness

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalDateTime

@Dao
interface WorkoutDao {

    // get all workout, sort by last first
    @Query("SELECT * FROM workouts ORDER BY date DESC")
    fun getAllWorkouts(): Flow<List<Workout>>

    // get workout by id
    @Query("SELECT * FROM workouts WHERE id = :workoutId")
    suspend fun getWorkoutById(workoutId: Int): Workout?

    // insert new workout
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkout(workout: Workout): Long
}


@Dao
interface StepDao {
    @Query("SELECT * FROM steps WHERE date = :date")
    fun getStepsByDate(date: LocalDate): Flow<Step?>

    @Query("SELECT * FROM steps WHERE date = :date")
    suspend fun getStepsByDateOnce(date: LocalDate): Step?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateStep(step: Step)

    // get steps where there is steps
    @Query("SELECT * FROM steps WHERE stepCount > 0")
    suspend fun getStepsWithData(): List<Step>
}

// database
@Database(
    entities = [Workout::class, Step::class],
    version = 2,
    exportSchema = false
)

@TypeConverters(Converters::class)
abstract class WorkoutDatabase : RoomDatabase() {

    abstract fun workoutDao(): WorkoutDao
    abstract fun stepDao(): StepDao

    companion object {
        @Volatile
        private var INSTANCE: WorkoutDatabase? = null

        fun getDatabase(context: Context): WorkoutDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WorkoutDatabase::class.java,
                    "workout_database"
                )
                    .fallbackToDestructiveMigration(false)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

// data parser between db and usage
class Converters {
    @RequiresApi(Build.VERSION_CODES.O)
    @TypeConverter
    fun fromTimestamp(value: String?): LocalDateTime? = value?.let { LocalDateTime.parse(it) }

    @TypeConverter
    fun dateToTimestamp(date: LocalDateTime?): String? = date?.toString()

    @RequiresApi(Build.VERSION_CODES.O)
    @TypeConverter
    fun fromDateString(value: String?): LocalDate? = value?.let { LocalDate.parse(it) }

    @TypeConverter
    fun localDateToString(date: LocalDate?): String? = date?.toString()
}