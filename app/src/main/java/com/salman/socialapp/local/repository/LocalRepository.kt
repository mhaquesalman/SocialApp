package com.salman.socialapp.local.repository

import android.app.Application
import com.salman.socialapp.local.db.SocialAppLocalDatabase
import com.salman.socialapp.model.Post
import com.salman.socialapp.network.ApiService
import com.salman.socialapp.repositories.Repository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LocalRepository(val app: Application) {

    private val socialDao = SocialAppLocalDatabase.getSocialAppLocalDatabase(app).socialAppDao()

/*    init {
        CoroutineScope(Dispatchers.IO).launch {
            val data = socialDao.getPostListFromRoom()
        }
    }*/

    suspend fun savePostListToLocalDb(posts: List<Post>) {
        socialDao.savePostListToRoom(posts)
    }

    fun getPostListFromLocalDb() = socialDao.getPostListFromRoom()

    suspend fun deletePostListFromLocalDb() {
        socialDao.deletePostListFromRoom()
    }
}