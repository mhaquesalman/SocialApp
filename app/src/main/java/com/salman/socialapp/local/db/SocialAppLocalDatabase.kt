package com.salman.socialapp.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.salman.socialapp.local.ProfilePost
import com.salman.socialapp.model.Post
import com.salman.socialapp.model.Profile

const val DATABASE_VERSION = 4
const val DATABASE_NAME = "SocialAppRoom"
@Database(
    entities = [Post::class, Profile::class, ProfilePost::class],
    version = DATABASE_VERSION,
    exportSchema = false
)
abstract class SocialAppLocalDatabase : RoomDatabase() {

    abstract fun socialAppDao(): SocialAppDao

/*    companion object {
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
    }*/

}