package com.salman.socialapp.util

import android.content.Context
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.salman.socialapp.model.UserInfo
import com.salman.socialapp.ui.activities.MY_LANGUAGE
import java.lang.reflect.Type
import java.net.InetSocketAddress
import java.net.Socket
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*

private const val TAG = "Utils"
private const val SHARED_PREF = "logged_in_user"
private const val CURRENT_USER = "user"
const val ADDRESS = "www.google.com"
const val PORT = 80
const val TIMEOUT = 1500

class Utils(val context: Context) {

    fun addUserToSharedPref(userInfo: UserInfo) {
        val sharedPreferences = context.getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(CURRENT_USER, Gson().toJson(userInfo))
        editor.apply()
    }

    fun getUserFromSharedPref(): UserInfo? {
        val sharedPreferences = context.getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE)
        val type = object : TypeToken<UserInfo>() {}.type
        val userInfo = Gson().fromJson<UserInfo>(sharedPreferences.getString(CURRENT_USER, null), type)
        return userInfo

    }

    fun removeUserFromSharedPref() {
        val sharedPreferences = context.getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.remove(CURRENT_USER)
        editor.commit()
    }

    companion object {

        fun setLanguage(lang: String, context: Context) {
            val locale = Locale(lang)
            Locale.setDefault(locale)
            val configuration = Configuration()
            configuration.locale = locale
            context.resources.updateConfiguration(configuration, context.resources.displayMetrics)
//            val editor = context.getSharedPreferences("Settings", Context.MODE_PRIVATE).edit()
//            editor.putString(MY_LANGUAGE, lang)
//            editor.commit()
        }

        fun getLanguage(context: Context): String {
            val prefs = context.getSharedPreferences("Settings", Context.MODE_PRIVATE)
            val lang = prefs.getString(MY_LANGUAGE, "en") ?: "en"
            setLanguage(lang, context)
            return lang
        }

        fun formatDate(date: String?): String {
/*         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val localDateTime = LocalDateTime.parse(date)
                val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG, FormatStyle.SHORT)
                val output = localDateTime.format(formatter)
                Log.d(TAG, "LocalDateTime: $output")
                return output
            }*/

            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val outputFormat = SimpleDateFormat("dd MMM yyyy hh:mm a")
            val parsedDate = inputFormat.parse(date)
            val output = outputFormat.format(parsedDate)
            Log.d(TAG, "SimpleDateFormat: $output")
            return output
        }

        fun isNetworkAvailable(context: Context): Boolean {
            var result = false
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val capabilities =
                    connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
                if (capabilities != null) {
                    result = when {
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                        else -> false
                    }
                }
            } else {
                val activeNetworkInfo = connectivityManager.activeNetworkInfo
                result = activeNetworkInfo != null && activeNetworkInfo.isConnected
            }
            return result
        }

        fun isNetConnectionAvailable(): Boolean {
            return try {
                val socket = Socket()
                val socketAddress = InetSocketAddress(ADDRESS, PORT)
                socket.connect(socketAddress, TIMEOUT)
                socket.close()
                true
            } catch (e: Exception) {
                false
            }
        }

        /*fun isNetWorkAvailable(context: Context): Boolean {
        var result = false
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        connectivityManager?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                it.getNetworkCapabilities(it.activeNetwork)?.apply {
                    result = when {
                        hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                        hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                        else -> false
                    }
                }
            }
        }
        return result
    }*/

    }

}