package com.salman.socialapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.salman.socialapp.network.ApiClient
import com.salman.socialapp.network.ApiService
import com.salman.socialapp.repositories.Repository
import java.lang.IllegalArgumentException

class ViewModelFactory: ViewModelProvider.NewInstanceFactory() {

    var repository: Repository? = null

    init {
        val apiService = ApiClient.getRetrofit()?.create(ApiService::class.java)
        repository = Repository.getRepository(apiService)
    }


    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            return LoginViewModel(repository) as T
        }
        throw IllegalArgumentException("ViewModel not found !")
    }
}