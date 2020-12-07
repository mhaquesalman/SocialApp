package com.salman.socialapp.network

import android.util.MalformedJsonException
import retrofit2.HttpException
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException

object ApiError {

    class ErrorMessage(val message: String?, val status: Int = 0)

    fun getErrorFromThrowable(t: Throwable): ErrorMessage {
        if (t is HttpException) {
            return ErrorMessage(t.message, t.code())
        } else if(t is SocketTimeoutException) {
            return ErrorMessage("Timeout")
        } else if(t is IOException) {
            if (t is MalformedJsonException) {
                return ErrorMessage("MalFormedJsonException from server !")
            } else if (t is ConnectException) {
                return ErrorMessage(t.message +" Different IP or XAMPP is not running !")
            } else {
                return ErrorMessage("No internet connection !")
            }
        } else {
            return ErrorMessage("Unknown error !")
        }
    }

    fun getErrorFromException(e: Exception) =
        ErrorMessage(e.message, e.hashCode())
}