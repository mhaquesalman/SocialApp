package com.salman.socialapp.viewmodels

import androidx.lifecycle.ViewModel
import com.salman.socialapp.repositories.Repository
import okhttp3.MultipartBody

class PostUploadViewModel(val repository: Repository?) : ViewModel() {

    fun uploadPost(body: MultipartBody, isCoverOrProfileImg: Boolean) =
        repository?.uploadPost(body, isCoverOrProfileImg)

}