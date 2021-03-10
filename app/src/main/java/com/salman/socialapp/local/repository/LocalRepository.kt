package com.salman.socialapp.local.repository

import android.app.Application
import com.salman.socialapp.local.db.SocialAppDao
import com.salman.socialapp.local.db.SocialAppLocalDatabase
import com.salman.socialapp.model.Post
import com.salman.socialapp.network.ApiService
import com.salman.socialapp.repositories.Repository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class LocalRepository
@Inject
constructor (val socialAppDao: SocialAppDao) {

//    private val socialDao = SocialAppLocalDatabase.getSocialAppLocalDatabase(app).socialAppDao()

/*    init {
        CoroutineScope(Dispatchers.IO).launch {
            val data = socialDao.getPostListFromRoom()
        }
    }*/

    suspend fun savePostListToLocalDb(posts: List<Post>) {
        socialAppDao.savePostListToRoom(posts)
    }

    fun getPostListFromLocalDb() = socialAppDao.getPostListFromRoom()

    suspend fun deletePostListFromLocalDb() {
        socialAppDao.deletePostListFromRoom()
    }
}