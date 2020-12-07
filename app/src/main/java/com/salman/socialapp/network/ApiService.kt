package com.salman.socialapp.network

import com.salman.socialapp.model.AuthResponse
import com.salman.socialapp.model.UserInfo
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    @POST("login")
    fun login(@Body userInfo: UserInfo): Call<AuthResponse>
}