package com.salman.socialapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.salman.socialapp.network.ApiClient
import com.salman.socialapp.network.ApiService
import com.salman.socialapp.repositories.Repository
import java.lang.IllegalArgumentException

class ViewModelFactory : ViewModelProvider.NewInstanceFactory() {

    var repository: Repository? = null
    init {
        val apiService = ApiClient.getRetrofit()?.create(ApiService::class.java)
        repository = Repository.getRepository(apiService)
    }


    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(LoginViewModel::class.java) -> {
                LoginViewModel(repository) as T
            }
            modelClass.isAssignableFrom(ProfileViewModel::class.java) -> {
                ProfileViewModel(repository) as T
            }
            modelClass.isAssignableFrom(PostUploadViewModel::class.java) -> {
                PostUploadViewModel(repository) as T
            }
            modelClass.isAssignableFrom(PostDetailViewModel::class.java) -> {
                PostDetailViewModel(repository) as T
            }
            modelClass.isAssignableFrom(SearchViewModel::class.java) -> {
                SearchViewModel(repository) as T
            }
            modelClass.isAssignableFrom(MainViewModel::class.java) -> {
                MainViewModel(repository) as T
            }
            modelClass.isAssignableFrom(CommentViewModel::class.java) -> {
                CommentViewModel(repository) as T
            }
            else -> throw IllegalArgumentException("ViewModel not found !")
        }
    }
}