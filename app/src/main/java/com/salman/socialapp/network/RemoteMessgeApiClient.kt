package com.salman.socialapp.network

import com.salman.socialapp.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit

const val REMOTE_MSG_BASE_URL = "https://fcm.googleapis.com/fcm/"
class RemoteMessgeApiClient {

    companion object {
        private var retrofit: Retrofit? = null

        fun getRetrofit(): Retrofit? {

            if (retrofit == null) {
                retrofit = Retrofit.Builder()
                    .baseUrl(REMOTE_MSG_BASE_URL)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .build()
            }
            return retrofit
        }
    }

}