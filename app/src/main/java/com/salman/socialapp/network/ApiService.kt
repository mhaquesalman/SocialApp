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

    @POST("updatepost")
    fun updatePost(@Body body: MultipartBody): Call<PostUploadResponse>

    @GET("deletepost")
    fun deletePost(@Query("postId") postId: String): Call<PostUploadResponse>

    @GET("postdetail")
    fun fetchPostDetail(@QueryMap params: Map<String, String>): Call<PostResponse>

    @POST("uploadImage")
    fun uploadImage(@Body body: MultipartBody): Call<PostUploadResponse>

    @GET("loadprofileinfo")
    fun fetchProfileInfo(@QueryMap params: Map<String, String>): Call<ProfileResponse>

    @GET("search")
    fun search(@QueryMap params: Map<String, String>): Call<SearchResponse>

    @GET("loadfriends")
    fun loadfriends(@Query("uid") uid: String): Call<FriendResponse>

    @GET("getnotification")
    fun getNotification(@Query("uid") uid: String): Call<NotificationResponse>

    @GET("getnewsfeed")
    fun getNewsFeed(@QueryMap params: Map<String, String>): Call<PostResponse>

    @GET("loadprofileposts")
    fun loadProfilePosts(@QueryMap params: Map<String, String>): Call<PostResponse>

    @POST("performaction")
    fun performAction(@Body performAction: PerformAction): Call<GeneralResponse>

    @POST("performreaction")
    fun performReaction(@Body performReaction: PerformReaction): Call<ReactionResponse>

    @POST("postcomment")
    fun postComment(@Body postComment: PostComment): Call<CommentResponse>

    @GET("getpostcomments")
    fun getPostComments(@Query("postId") postId: String, @Query("postUserId") postUserId: String): Call<CommentResponse>

    @GET("getcommentreplies")
    fun getCommentReplies(@Query("postId") postId: String, @Query("commentId") commentId: String): Call<CommentResponse>

}