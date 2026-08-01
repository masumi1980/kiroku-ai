package jp.co.kirokuai.app.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [MeetingEntity::class],
    version = 1,
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
            ).build()
    }
}
