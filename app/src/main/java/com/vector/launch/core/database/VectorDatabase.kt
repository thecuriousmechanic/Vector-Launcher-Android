package com.vector.launch.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.withTransaction
import androidx.sqlite.db.SupportSQLiteDatabase

import androidx.room.migration.Migration

@Database(
    entities = [
        TaskEntity::class,
        ProjectEntity::class,
        NoteEntity::class,
        ResourceEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class VectorDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun projectDao(): ProjectDao
    abstract fun noteDao(): NoteDao
    abstract fun resourceDao(): ResourceDao

    companion object {
        @Volatile
        private var INSTANCE: VectorDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE tasks ADD COLUMN completedAt INTEGER DEFAULT NULL")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE INDEX IF NOT EXISTS index_tasks_projectId ON tasks(projectId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_notes_projectId ON notes(projectId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_resources_projectId ON resources(projectId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_resources_taskId ON resources(taskId)")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE tasks ADD COLUMN isToday INTEGER NOT NULL DEFAULT 0")
                db.execSQL("UPDATE tasks SET isToday = 1 WHERE projectId IS NULL")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_tasks_isToday ON tasks(isToday)")
            }
        }

        fun getInstance(context: Context): VectorDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    VectorDatabase::class.java,
                    "vector_launcher_db"
                )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                .build()
                INSTANCE = instance
                instance
            }
        }

        suspend fun seedInitialData(db: VectorDatabase) = db.withTransaction {
            val project1 = ProjectEntity(
                id = "proj-1",
                name = "Vector Launcher Rebuild",
                description = "Rebuild launcher into lightweight productivity engine",
                colorHex = "#6366F1",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            val project2 = ProjectEntity(
                id = "proj-2",
                name = "Personal System",
                description = "Daily routines and capture",
                colorHex = "#10B981",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            db.projectDao().insertProjects(listOf(project1, project2))

            val task1 = TaskEntity(
                id = "task-1",
                title = "Complete Dashboard Customization",
                description = "Allow users to reorder, show/hide widgets",
                completed = false,
                priority = "HIGH",
                dueDate = null,
                projectId = "proj-1",
                isToday = false,
                completedAt = null,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            val task2 = TaskEntity(
                id = "task-2",
                title = "Refine App Launcher Search & Preferences",
                description = "Ensure fast app launching with persistent DataStore preferences",
                completed = false,
                priority = "MEDIUM",
                dueDate = null,
                projectId = "proj-1",
                isToday = false,
                completedAt = null,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            val task3 = TaskEntity(
                id = "task-3",
                title = "Morning reflection & quick capture",
                description = "",
                completed = true,
                priority = "LOW",
                dueDate = null,
                projectId = "proj-2",
                isToday = false,
                completedAt = System.currentTimeMillis(),
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            db.taskDao().insertTasks(listOf(task1, task2, task3))

            val note1 = NoteEntity(
                id = "note-1",
                title = "Vector Product Architecture",
                content = "VECTOR LAUNCHER ITSELF IS THE FOCUS ENVIRONMENT.\n\nCore Hierarchy:\nTime -> Apps -> Tasks -> Projects -> Notes.",
                projectId = "proj-1",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            val note2 = NoteEntity(
                id = "note-2",
                title = "Minimal Visual Language",
                content = "Quiet, modern, functional design system with zero heavy liquid glass rendering lag.",
                projectId = "proj-1",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            db.noteDao().insertNotes(listOf(note1, note2))

            val resource1 = ResourceEntity(
                id = "res-1",
                title = "Android Performance Checklist",
                uri = "https://developer.android.com/topic/performance",
                type = "LINK",
                projectId = "proj-1",
                taskId = "task-2",
                createdAt = System.currentTimeMillis()
            )

            db.resourceDao().insertResource(resource1)
        }
    }
}
