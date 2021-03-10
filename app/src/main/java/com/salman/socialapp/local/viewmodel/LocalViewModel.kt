package com.salman.socialapp.local.viewmodel

import android.app.Application
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import com.salman.socialapp.local.repository.LocalRepository
import com.salman.socialapp.model.Post

class LocalViewModel
@ViewModelInject
constructor (val localRepository: LocalRepository) : ViewModel() {

//    private val localRepository = LocalRepository(app)

    suspend fun savePostListToLocalDb(posts: List<Post>) {
        localRepository.savePostListToLocalDb(posts)
    }

    fun getPostListFromLocalDb() = localRepository.getPostListFromLocalDb()

    suspend fun deletePostListFromLocalDb() {
        localRepository.deletePostListFromLocalDb()
    }
}