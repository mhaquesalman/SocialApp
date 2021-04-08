package com.salman.socialapp.local.viewmodel

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import com.salman.socialapp.local.ProfilePost
import com.salman.socialapp.local.repository.LocalRepository
import com.salman.socialapp.model.Post
import com.salman.socialapp.model.Profile

class LocalViewModel
@ViewModelInject
constructor (val localRepository: LocalRepository) : ViewModel() {

//    private val localRepository = LocalRepository(app)

    suspend fun savePostListToLocalDb(posts: List<Post>) = localRepository.savePostListToLocalDb(posts)

    suspend fun deletePostListFromLocalDb() = localRepository.deletePostListFromLocalDb()

    fun getPostListFromLocalDb() = localRepository.getPostListFromLocalDb()

    suspend fun saveProfileToLocalDb(profile: Profile) = localRepository.saveProfileToLocalDb(profile)

    suspend fun deleteProfileFromLocalDb() = localRepository.deleteProfileFromLocalDb()

    fun getProfileFromLocalDb() = localRepository.getProfileFromLocalDb()

    suspend fun saveProfilePostListToLocalDb(profilePosts: List<ProfilePost>) =
        localRepository.saveProfilePostListToLocalDb(profilePosts)

    suspend fun deleteProfilePostListFromLocalDb() = localRepository.deleteProfilePostListFromLocalDb()

    fun getProfilePostListFromLocalDb() = localRepository.getProfilePostListFromLocalDb()

}