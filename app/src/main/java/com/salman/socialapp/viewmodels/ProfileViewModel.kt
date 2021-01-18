package com.salman.socialapp.viewmodels

import androidx.lifecycle.ViewModel
import com.salman.socialapp.model.PerformAction
import com.salman.socialapp.model.PerformReaction
import com.salman.socialapp.repositories.Repository
import okhttp3.MultipartBody

class ProfileViewModel(val repository: Repository?): ViewModel() {

    fun fetchProfileInfo(params: Map<String, String>) =
        repository?.fetchProfileInfo(params)

    fun loadProfilePosts(params: Map<String, String>) =
        repository?.loadProfilePosts(params)

    fun uploadPost(body: MultipartBody, isCoverOrProfileImg: Boolean) =
        repository?.uploadPost(body, isCoverOrProfileImg)

    fun performFriendAction(performAction: PerformAction) =
        repository?.performFriendAction(performAction)

    fun performReaction(performReaction: PerformReaction) =
        repository?.performReaction(performReaction)

    fun deletePost(postId: String) = repository?.deletePost(postId)
}