package com.salman.socialapp.network

import android.util.MalformedJsonException
import retrofit2.HttpException
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException

object ApiError {

    class ErrorMessage(val message: String?, val status: Int = 0)

    fun getErrorFromThrowable(t: Throwable): ErrorMessage {
        when (t) {
            is HttpException -> {
                return ErrorMessage(t.message, t.code())
            }
            is SocketTimeoutException -> {
                return ErrorMessage("Timeout")
            }
            is IOException -> {
                if (t is MalformedJsonException) {
                    return ErrorMessage("MalFormedJsonException from server !")
                } else if (t is ConnectException) {
                    return ErrorMessage(t.message +" ConnectException !")
                } else {
                    return ErrorMessage("No internet connection !")
                }
            }
            else -> {
                return ErrorMessage("Unknown error !")
            }
        }
    }

    fun getErrorFromException(e: Exception) = ErrorMessage(e.message, e.hashCode())
}