package com.salman.socialapp.local.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.salman.socialapp.local.repository.LocalRepository
import com.salman.socialapp.model.Post

class LocalViewModel(app: Application) : AndroidViewModel(app) {
    private val localRepository = LocalRepository(app)

    suspend fun savePostListToLocalDb(posts: List<Post>) {
        localRepository.savePostListToLocalDb(posts)
    }

    fun getPostListFromLocalDb() = localRepository.getPostListFromLocalDb()

    suspend fun deletePostListFromLocalDb() {
        localRepository.deletePostListFromLocalDb()
    }
}