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
                val profileResponse = PostUploadResponse(message = errorMessage.message, status = errorMessage.status)
                postUploadLiveData.postValue(profileResponse)
            }
        })
        return postUploadLiveData
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
}