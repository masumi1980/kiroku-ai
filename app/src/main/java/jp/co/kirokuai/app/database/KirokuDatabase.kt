package jp.co.kirokuai.app.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [MeetingEntity::class],
    version = 2,
    exportSchema = true,
)
abstract class KirokuDatabase : RoomDatabase() {
    abstract fun meetingDao(): MeetingDao

    companion object {
        private const val DATABASE_NAME = "kiroku.db"

        fun create(context: Context): KirokuDatabase =
            Room.databaseBuilder(
                context.applicationContext,
                KirokuDatabase::class.java,
                DATABASE_NAME,
            )
                .addMigrations(MIGRATION_1_2)
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
    }
}
