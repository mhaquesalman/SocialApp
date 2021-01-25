package com.salman.socialapp.repositories

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.salman.socialapp.model.*
import com.salman.socialapp.network.ApiError
import com.salman.socialapp.network.ApiService
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException


const val TAG = "Repository"
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
                        gson.fromJson(response.errorBody()?.string(), AuthResponse::class.java)
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

    fun fetchProfileInfo(params: Map<String, String>): LiveData<ProfileResponse> {
        val profileInfoLiveData: MutableLiveData<ProfileResponse> = MutableLiveData()
        val call = apiService?.fetchProfileInfo(params)
        call?.enqueue(object : Callback<ProfileResponse> {
            override fun onResponse(call: Call<ProfileResponse>, response: Response<ProfileResponse>) {
                if (response.isSuccessful) {
                    profileInfoLiveData.postValue(response.body())
                } else {
                    val gson = Gson()
                    val profileResponse = try {
                        gson.fromJson(response.errorBody()?.toString(), ProfileResponse::class.java)
                    } catch (e: IOException) {
                        val errorMessage = ApiError.getErrorFromException(e)
                        ProfileResponse(message = errorMessage.message, status = errorMessage.status)
                    }
                    profileInfoLiveData.postValue(profileResponse)

                }
            }
            override fun onFailure(call: Call<ProfileResponse>, t: Throwable) {
                val errorMessage = ApiError.getErrorFromThrowable(t)
                val profileResponse = ProfileResponse(message = errorMessage.message, status = errorMessage.status)
                profileInfoLiveData.postValue(profileResponse)
            }
        })
        return profileInfoLiveData
    }

    fun uploadPost(body: MultipartBody, isCoverOrProfileImg: Boolean): LiveData<PostUploadResponse> {
        val postUploadLiveData: MutableLiveData<PostUploadResponse> = MutableLiveData()
        val call = if (isCoverOrProfileImg) {
            apiService?.uploadImage(body)
        } else {
            apiService?.uploadPost(body)
        }
        call?.enqueue(object : Callback<PostUploadResponse> {
            override fun onResponse(call: Call<PostUploadResponse>, response: Response<PostUploadResponse>) {
                if (response.isSuccessful) {
                    postUploadLiveData.postValue(response.body())
                } else {
                    val gson = Gson()
                    val postUploadResponse = try {
                        Log.e(TAG, "ResponseError: " + response.errorBody())
                        gson.fromJson(response.errorBody()?.toString(), PostUploadResponse::class.java)
                    } catch (e: IOException) {
                        val errorMessage = ApiError.getErrorFromException(e)
                        PostUploadResponse(message = errorMessage.message, status = errorMessage.status)
                    }
                    postUploadLiveData.postValue(postUploadResponse)
                }
            }
            override fun onFailure(call: Call<PostUploadResponse>, t: Throwable) {
                val errorMessage = ApiError.getErrorFromThrowable(t)
                val postUpdateResponse = PostUploadResponse(message = errorMessage.message, status = errorMessage.status)
                postUploadLiveData.postValue(postUpdateResponse)
            }
        })
        return postUploadLiveData
    }

    fun updatePost(body: MultipartBody): LiveData<PostUploadResponse> {
        val postUpdateLiveData: MutableLiveData<PostUploadResponse> = MutableLiveData()
        val call = apiService?.updatePost(body)
        call?.enqueue(object : Callback<PostUploadResponse> {
            override fun onResponse(call: Call<PostUploadResponse>, response: Response<PostUploadResponse>) {
                if (response.isSuccessful) {
                    postUpdateLiveData.postValue(response.body())
                } else {
                    val gson = Gson()
                    val postUpdateResponse = try {
                        Log.e(TAG, "ResponseError: " + response.errorBody())
                        gson.fromJson(response.errorBody()?.toString(), PostUploadResponse::class.java)
                    } catch (e: IOException) {
                        val errorMessage = ApiError.getErrorFromException(e)
                        PostUploadResponse(message = errorMessage.message, status = errorMessage.status)
                    }
                    postUpdateLiveData.postValue(postUpdateResponse)
                }
            }
            override fun onFailure(call: Call<PostUploadResponse>, t: Throwable) {
                val errorMessage = ApiError.getErrorFromThrowable(t)
                val postUpdateResponse = PostUploadResponse(message = errorMessage.message, status = errorMessage.status)
                postUpdateLiveData.postValue(postUpdateResponse)
            }
        })
        return postUpdateLiveData
    }

    fun deletePost(postId: String): LiveData<PostUploadResponse> {
        val postDeleteLiveData: MutableLiveData<PostUploadResponse> = MutableLiveData()
        val call = apiService?.deletePost(postId)
        call?.enqueue(object : Callback<PostUploadResponse> {
            override fun onResponse(call: Call<PostUploadResponse>, response: Response<PostUploadResponse>) {
                if (response.isSuccessful) {
                    postDeleteLiveData.postValue(response.body())
                } else {
                    val gson = Gson()
                    val postDeleteResponse = try {
                        Log.e(TAG, "ResponseError: " + response.errorBody())
                        gson.fromJson(response.errorBody()?.toString(), PostUploadResponse::class.java)
                    } catch (e: IOException) {
                        val errorMessage = ApiError.getErrorFromException(e)
                        PostUploadResponse(message = errorMessage.message, status = errorMessage.status)
                    }
                    postDeleteLiveData.postValue(postDeleteResponse)
                }
            }
            override fun onFailure(call: Call<PostUploadResponse>, t: Throwable) {
                val errorMessage = ApiError.getErrorFromThrowable(t)
                val postDeleteResponse = PostUploadResponse(message = errorMessage.message, status = errorMessage.status)
                postDeleteLiveData.postValue(postDeleteResponse)
            }
        })
        return postDeleteLiveData
    }

    fun search(params: Map<String, String>): LiveData<SearchResponse> {
        val searchLiveData: MutableLiveData<SearchResponse> = MutableLiveData()
        val call = apiService?.search(params)
        call?.enqueue(object : Callback<SearchResponse> {
            override fun onResponse(call: Call<SearchResponse>, response: Response<SearchResponse>) {
                if (response.isSuccessful) {
                    searchLiveData.postValue(response.body())
                } else {
                    val gson = Gson()
                    val searchResponse = try {
                        gson.fromJson(response.errorBody()?.toString(), SearchResponse::class.java)
                    } catch (e: IOException) {
                        val errorMessage = ApiError.getErrorFromException(e)
                        SearchResponse(message = errorMessage.message, status = errorMessage.status)
                    }
                    searchLiveData.postValue(searchResponse)

                }
            }
            override fun onFailure(call: Call<SearchResponse>, t: Throwable) {
                val errorMessage = ApiError.getErrorFromThrowable(t)
                val searchResponse = SearchResponse(message = errorMessage.message, status = errorMessage.status)
                searchLiveData.postValue(searchResponse)
            }
        })
        return searchLiveData
    }

    fun performFriendAction(performAction: PerformAction): LiveData<GeneralResponse> {
        val friendActionLiveData: MutableLiveData<GeneralResponse> = MutableLiveData()
        val call = apiService?.performAction(performAction)
        call?.enqueue(object : Callback<GeneralResponse> {
            override fun onResponse(call: Call<GeneralResponse>, response: Response<GeneralResponse>) {
                if (response.isSuccessful) {
                    friendActionLiveData.postValue(response.body())
                } else {
                    val gson = Gson()
                    val generalResponse = try {
                        gson.fromJson(response.errorBody()?.toString(), GeneralResponse::class.java)
                    } catch (e: IOException) {
                        val errorMessage = ApiError.getErrorFromException(e)
                        GeneralResponse(message = errorMessage.message, status = errorMessage.status)
                    }
                    friendActionLiveData.postValue(generalResponse)

                }
            }
            override fun onFailure(call: Call<GeneralResponse>, t: Throwable) {
                val errorMessage = ApiError.getErrorFromThrowable(t)
                val generalResponse = GeneralResponse(message = errorMessage.message, status = errorMessage.status)
                friendActionLiveData.postValue(generalResponse)
            }
        })
        return friendActionLiveData
    }

    fun loadfriends(uid: String): MutableLiveData<FriendResponse> {
        val friendsLiveData: MutableLiveData<FriendResponse> = MutableLiveData()
        val call = apiService?.loadfriends(uid)
        call?.enqueue(object : Callback<FriendResponse> {
            override fun onResponse(call: Call<FriendResponse>, response: Response<FriendResponse>) {
                if (response.isSuccessful) {
                    friendsLiveData.postValue(response.body())
                } else {
                    val gson = Gson()
                    val friendResponse = try {
                        Log.e(TAG, "ResponseError: " + response.errorBody())
                        gson.fromJson(response.errorBody()?.string(), FriendResponse::class.java)
                    } catch (e: IOException) {
                        val errorMessage = ApiError.getErrorFromException(e)
                        FriendResponse(message = errorMessage.message, status = errorMessage.status)
                    }

                    friendsLiveData.postValue(friendResponse)

                }
            }
            override fun onFailure(call: Call<FriendResponse>, t: Throwable) {
                val errorMessage = ApiError.getErrorFromThrowable(t)
                val friendResponse = FriendResponse(message = errorMessage.message, status = errorMessage.status)
                friendsLiveData.postValue(friendResponse)
            }
        })
        return friendsLiveData
    }

    fun getNewsFeed(params: Map<String, String>): LiveData<PostResponse> {
        val newsFeedLiveData: MutableLiveData<PostResponse> = MutableLiveData()
        val call = apiService?.getNewsFeed(params)
        call?.enqueue(object : Callback<PostResponse> {
            override fun onResponse(call: Call<PostResponse>, response: Response<PostResponse>) {
                if (response.isSuccessful) {
                    newsFeedLiveData.postValue(response.body())
                } else {
                    val gson = Gson()
                    val postResponse = try {
                        Log.e(TAG, "ResponseError: " + response.errorBody())
                        gson.fromJson(response.errorBody()?.string(), PostResponse::class.java)
                    } catch (e: IOException) {
                        val errorMessage = ApiError.getErrorFromException(e)
                        PostResponse(message = errorMessage.message, status = errorMessage.status)
                    }

                    newsFeedLiveData.postValue(postResponse)

                }
            }
            override fun onFailure(call: Call<PostResponse>, t: Throwable) {
                val errorMessage = ApiError.getErrorFromThrowable(t)
                val postResponse = PostResponse(message = errorMessage.message, status = errorMessage.status)
                newsFeedLiveData.postValue(postResponse)
            }
        })
        return newsFeedLiveData
    }

    fun loadProfilePosts(params: Map<String, String>): LiveData<PostResponse> {
        val profilePostLiveData: MutableLiveData<PostResponse> = MutableLiveData()
        val call = apiService?.loadProfilePosts(params)
        call?.enqueue(object : Callback<PostResponse> {
            override fun onResponse(call: Call<PostResponse>, response: Response<PostResponse>) {
                if (response.isSuccessful) {
                    profilePostLiveData.postValue(response.body())
                } else {
                    val gson = Gson()
                    val postResponse = try {
                        Log.e(TAG, "ResponseError: " + response.errorBody())
                        gson.fromJson(response.errorBody()?.string(), PostResponse::class.java)
                    } catch (e: IOException) {
                        val errorMessage = ApiError.getErrorFromException(e)
                        PostResponse(message = errorMessage.message, status = errorMessage.status)
                    }

                    profilePostLiveData.postValue(postResponse)

                }
            }
            override fun onFailure(call: Call<PostResponse>, t: Throwable) {
                val errorMessage = ApiError.getErrorFromThrowable(t)
                val postResponse = PostResponse(message = errorMessage.message, status = errorMessage.status)
                profilePostLiveData.postValue(postResponse)
            }
        })
        return profilePostLiveData
    }

    fun performReaction(performReaction: PerformReaction): LiveData<ReactionResponse> {
        val reactionLiveData: MutableLiveData<ReactionResponse> = MutableLiveData()
        val call = apiService?.performReaction(performReaction)
        call?.enqueue(object : Callback<ReactionResponse> {
            override fun onResponse(call: Call<ReactionResponse>, response: Response<ReactionResponse>) {
                if (response.isSuccessful) {
                    reactionLiveData.postValue(response.body())
                } else {
                    val gson = Gson()
                    val reactResponse = try {
                        Log.e(TAG, "ResponseError: " + response.errorBody())
                        gson.fromJson(response.errorBody()?.string(), ReactionResponse::class.java)
                    } catch (e: IOException) {
                        val errorMessage = ApiError.getErrorFromException(e)
                        ReactionResponse(message = errorMessage.message, status = errorMessage.status)
                    }

                    reactionLiveData.postValue(reactResponse)

                }
            }
            override fun onFailure(call: Call<ReactionResponse>, t: Throwable) {
                val errorMessage = ApiError.getErrorFromThrowable(t)
                val reactResponse = ReactionResponse(message = errorMessage.message, status = errorMessage.status)
                reactionLiveData.postValue(reactResponse)
            }
        })
        return reactionLiveData
    }

    fun postComment(postComment: PostComment): LiveData<CommentResponse> {
        val postCommentLiveData: MutableLiveData<CommentResponse> = MutableLiveData()
        val call = apiService?.postComment(postComment)
        call?.enqueue(object : Callback<CommentResponse> {
            override fun onResponse(call: Call<CommentResponse>, response: Response<CommentResponse>) {
                if (response.isSuccessful) {
                    postCommentLiveData.postValue(response.body())
                } else {
                    val gson = Gson()
                    val commentResponse = try {
                        Log.e(TAG, "ResponseError: " + response.errorBody())
                        gson.fromJson(response.errorBody()?.string(), CommentResponse::class.java)
                    } catch (e: IOException) {
                        val errorMessage = ApiError.getErrorFromException(e)
                        CommentResponse(message = errorMessage.message, status = errorMessage.status)
                    }

                    postCommentLiveData.postValue(commentResponse)

                }
            }
            override fun onFailure(call: Call<CommentResponse>, t: Throwable) {
                val errorMessage = ApiError.getErrorFromThrowable(t)
                val commentResponse = CommentResponse(message = errorMessage.message, status = errorMessage.status)
                postCommentLiveData.postValue(commentResponse)
            }
        })
        return postCommentLiveData
    }

    fun getPostComments(postId: String, postUserId: String): LiveData<CommentResponse> {
        val getPostCommentsLiveData: MutableLiveData<CommentResponse> = MutableLiveData()
        val call = apiService?.getPostComments(postId, postUserId)
        call?.enqueue(object : Callback<CommentResponse> {
            override fun onResponse(call: Call<CommentResponse>, response: Response<CommentResponse>) {
                if (response.isSuccessful) {
                    getPostCommentsLiveData.postValue(response.body())
                } else {
                    val gson = Gson()
                    val commentResponse = try {
                        Log.e(TAG, "ResponseError: " + response.errorBody())
                        gson.fromJson(response.errorBody()?.string(), CommentResponse::class.java)
                    } catch (e: IOException) {
                        val errorMessage = ApiError.getErrorFromException(e)
                        CommentResponse(message = errorMessage.message, status = errorMessage.status)
                    }

                    getPostCommentsLiveData.postValue(commentResponse)

                }
            }
            override fun onFailure(call: Call<CommentResponse>, t: Throwable) {
                val errorMessage = ApiError.getErrorFromThrowable(t)
                val commentResponse = CommentResponse(message = errorMessage.message, status = errorMessage.status)
                getPostCommentsLiveData.postValue(commentResponse)
            }
        })
        return getPostCommentsLiveData
    }

    fun getCommentReplies(postId: String, commentId: String): LiveData<CommentResponse> {
        val getCommentRepliesLiveData: MutableLiveData<CommentResponse> = MutableLiveData()
        val call = apiService?.getCommentReplies(postId, commentId)
        call?.enqueue(object : Callback<CommentResponse> {
            override fun onResponse(call: Call<CommentResponse>, response: Response<CommentResponse>) {
                if (response.isSuccessful) {
                    getCommentRepliesLiveData.postValue(response.body())
                } else {
                    val gson = Gson()
                    val commentResponse = try {
                        Log.e(TAG, "ResponseError: " + response.errorBody())
                        gson.fromJson(response.errorBody()?.string(), CommentResponse::class.java)
                    } catch (e: IOException) {
                        val errorMessage = ApiError.getErrorFromException(e)
                        CommentResponse(message = errorMessage.message, status = errorMessage.status)
                    }

                    getCommentRepliesLiveData.postValue(commentResponse)

                }
            }
            override fun onFailure(call: Call<CommentResponse>, t: Throwable) {
                val errorMessage = ApiError.getErrorFromThrowable(t)
                val commentResponse = CommentResponse(message = errorMessage.message, status = errorMessage.status)
                getCommentRepliesLiveData.postValue(commentResponse)
            }
        })
        return getCommentRepliesLiveData
    }

    fun getNotification(uid: String): MutableLiveData<NotificationResponse> {
        val notificationsLiveData: MutableLiveData<NotificationResponse> = MutableLiveData()
        val call = apiService?.getNotification(uid)
        call?.enqueue(object : Callback<NotificationResponse> {
            override fun onResponse(call: Call<NotificationResponse>, response: Response<NotificationResponse>) {
                if (response.isSuccessful) {
                    notificationsLiveData.postValue(response.body())
                } else {
                    val gson = Gson()
                    val notificationResponse = try {
                        Log.e(TAG, "ResponseError: " + response.errorBody())
                        gson.fromJson(response.errorBody()?.string(), NotificationResponse::class.java)
                    } catch (e: IOException) {
                        val errorMessage = ApiError.getErrorFromException(e)
                        NotificationResponse(message = errorMessage.message, status = errorMessage.status)
                    }

                    notificationsLiveData.postValue(notificationResponse)

                }
            }
            override fun onFailure(call: Call<NotificationResponse>, t: Throwable) {
                val errorMessage = ApiError.getErrorFromThrowable(t)
                val notificationResponse = NotificationResponse(message = errorMessage.message, status = errorMessage.status)
                notificationsLiveData.postValue(notificationResponse)
            }
        })
        return notificationsLiveData
    }

    fun fetchPostDetail(params: Map<String, String>): LiveData<PostResponse> {
        val postDetailLiveData: MutableLiveData<PostResponse> = MutableLiveData()
        val call = apiService?.fetchPostDetail(params)
        call?.enqueue(object : Callback<PostResponse> {
            override fun onResponse(call: Call<PostResponse>, response: Response<PostResponse>) {
                if (response.isSuccessful) {
                    postDetailLiveData.postValue(response.body())
                } else {
                    val gson = Gson()
                    val postDetailResponse = try {
                        Log.e(TAG, "ResponseError: " + response.errorBody())
                        gson.fromJson(response.errorBody()?.toString(), PostResponse::class.java)
                    } catch (e: IOException) {
                        val errorMessage = ApiError.getErrorFromException(e)
                        PostResponse(message = errorMessage.message, status = errorMessage.status)
                    }
                    postDetailLiveData.postValue(postDetailResponse)

                }
            }
            override fun onFailure(call: Call<PostResponse>, t: Throwable) {
                val errorMessage = ApiError.getErrorFromThrowable(t)
                val postResponse = PostResponse(message = errorMessage.message, status = errorMessage.status)
                postDetailLiveData.postValue(postResponse)
            }
        })
        return postDetailLiveData
    }
}