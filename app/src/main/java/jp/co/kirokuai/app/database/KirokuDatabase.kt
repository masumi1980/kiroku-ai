package jp.co.kirokuai.app.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [MeetingEntity::class, MeetingSummaryEntity::class],
    version = 3,
    exportSchema = true,
)
abstract class KirokuDatabase : RoomDatabase() {
    abstract fun meetingDao(): MeetingDao

    abstract fun meetingSummaryDao(): MeetingSummaryDao

    companion object {
        private const val DATABASE_NAME = "kiroku.db"

        fun create(context: Context): KirokuDatabase =
            Room.databaseBuilder(
                context.applicationContext,
                KirokuDatabase::class.java,
                DATABASE_NAME,
            )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                .build()

        val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE meetings ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0",
                )
                db.execSQL(
                    "ALTER TABLE meetings ADD COLUMN transcript TEXT NOT NULL DEFAULT ''",
                )
                db.execSQL(
                    "ALTER TABLE meetings ADD COLUMN summary TEXT DEFAULT NULL",
                )
                db.execSQL(
                    "UPDATE meetings SET updatedAt = createdAt WHERE updatedAt = 0",
                )
            }
        }

        val MIGRATION_2_3: Migration = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS meeting_summaries (
                        meetingId INTEGER NOT NULL,
                        summary TEXT NOT NULL,
                        decisionsJson TEXT NOT NULL,
                        discussionJson TEXT NOT NULL,
                        nextActionsJson TEXT NOT NULL,
                        risksJson TEXT NOT NULL,
                        createdAt INTEGER NOT NULL,
                        PRIMARY KEY(meetingId)
                    )
                    """.trimIndent(),
                )
            }
        }
    }
}
