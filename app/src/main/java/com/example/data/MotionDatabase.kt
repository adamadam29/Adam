package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        ProjectEntity::class,
        LayerEntity::class,
        KeyframeEntity::class,
        ExportEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class MotionDatabase : RoomDatabase() {
    abstract fun motionDao(): MotionDao

    companion object {
        @Volatile
        private var INSTANCE: MotionDatabase? = null

        fun getDatabase(context: Context): MotionDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MotionDatabase::class.java,
                    "motion_editor_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
