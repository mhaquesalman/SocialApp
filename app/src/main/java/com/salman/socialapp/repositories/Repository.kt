package com.salman.socialapp.repositories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.salman.socialapp.model.AuthResponse
import com.salman.socialapp.model.UserInfo
import com.salman.socialapp.network.ApiError
import com.salman.socialapp.network.ApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException

class Repository(val apiService: ApiService?) {

    companion object {
        private var repository: Repository? = null

        fun getRepository(apiService: ApiService?): Repository? {
            if (repository == null) {
                repository = Repository(apiService)
            }
            return repository
        }

    }

    fun login(userInfo: UserInfo): LiveData<AuthResponse> {
        val authLiveData: MutableLiveData<AuthResponse> = MutableLiveData<AuthResponse>()
        val call = apiService?.login(userInfo)
        call?.enqueue(object : Callback<AuthResponse> {
            override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                if (response.isSuccessful) {
                    authLiveData.postValue(response.body())
                } else {
                    val gson = Gson()
                    val authResponse = try {
                        gson.fromJson(response.errorBody()?.toString(), AuthResponse::class.java)
                    } catch (e: IOException) {
                        val errorMessage = ApiError.getErrorFromException(e)
                        AuthResponse(message = errorMessage.message, status = errorMessage.status)
                    }
                    authLiveData.postValue(authResponse)
                }
            }
            override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                val errorMessage = ApiError.getErrorFromThrowable(t)
                val authResponse = AuthResponse(message = errorMessage.message, status = errorMessage.status)
                authLiveData.postValue(authResponse)
            }

        })
        return authLiveData
    }

}