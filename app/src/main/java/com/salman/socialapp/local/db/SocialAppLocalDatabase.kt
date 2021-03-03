package com.salman.socialapp.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.salman.socialapp.model.Post

const val DATABASE_VERSION = 1
const val DATABASE_NAME = "SocialAppRoom"
@Database(
    entities = [Post::class],
    version = DATABASE_VERSION,
    exportSchema = false
)
abstract class SocialAppLocalDatabase : RoomDatabase() {

    abstract fun socialAppDao(): SocialAppDao

    companion object {
        @Volatile
        private var INSTANCE: SocialAppLocalDatabase? = null
        fun getSocialAppLocalDatabase(context: Context): SocialAppLocalDatabase {
            if (INSTANCE == null) {
                synchronized(this) {
                    INSTANCE = Room.databaseBuilder(
                        context.applicationContext,
                        SocialAppLocalDatabase::class.java,
                        DATABASE_NAME
                    ).build()
                }
            }
            return INSTANCE!!
        }
    }

}