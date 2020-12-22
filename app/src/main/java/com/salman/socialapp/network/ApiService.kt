package com.salman.socialapp.network

import com.salman.socialapp.model.*
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.*

interface ApiService {

    @POST("login")
    fun login(@Body userInfo: UserInfo): Call<AuthResponse>

    @POST("uploadpost")
    fun uploadPost(@Body body: MultipartBody): Call<PostUploadResponse>

    @POST("uploadImage")
    fun uploadImage(@Body body: MultipartBody): Call<PostUploadResponse>

    @GET("loadprofileinfo")
    fun fetchProfileInfo(@QueryMap params: Map<String, String>): Call<ProfileResponse>

    @GET("search")
    fun search(@QueryMap params: Map<String, String>): Call<SearchResponse>

    @GET("loadfriends")
    fun loadfriends(@Query("uid") uid: String): Call<FriendResponse>

    @POST("performaction")
    fun performAction(@Body performAction: PerformAction): Call<GeneralResponse>
}