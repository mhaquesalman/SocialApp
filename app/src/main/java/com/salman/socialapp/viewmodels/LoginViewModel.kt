package com.salman.socialapp.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.salman.socialapp.model.AuthResponse
import com.salman.socialapp.model.UserInfo
import com.salman.socialapp.repositories.Repository

class LoginViewModel(private val repository: Repository?): ViewModel() {

/*    fun login(userInfo: UserInfo): LiveData<AuthResponse>? {
       return repository?.login(userInfo)
    }*/

    fun login(userInfo: UserInfo) = repository?.login(userInfo)

}