package com.salman.socialapp.viewmodels

import androidx.lifecycle.ViewModel
import com.salman.socialapp.repositories.Repository
import okhttp3.MultipartBody

class ProfileViewModel(val repository: Repository?): ViewModel() {

    fun fetchProfileInfo(params: Map<String, String>) =
        repository?.fetchProfileInfo(params)

    fun uploadPost(body: MultipartBody, isCoverOrProfileImg: Boolean) =
        repository?.uploadPost(body, isCoverOrProfileImg)
}