package com.salman.socialapp.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.salman.socialapp.local.db.DATABASE_NAME
import com.salman.socialapp.local.db.SocialAppDao
import com.salman.socialapp.local.db.SocialAppLocalDatabase
import com.salman.socialapp.local.repository.LocalRepository
import com.salman.socialapp.network.ApiClient
import com.salman.socialapp.network.ApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideSocialAppLocalDatabase(@ApplicationContext context: Context): SocialAppLocalDatabase =
        Room.databaseBuilder(
        context,
        SocialAppLocalDatabase::class.java,
        DATABASE_NAME
    ).build()

    @Singleton
    @Provides
    fun provideSocialAppDao(socialAppLocalDatabase: SocialAppLocalDatabase): SocialAppDao =
        socialAppLocalDatabase.socialAppDao()
    
}