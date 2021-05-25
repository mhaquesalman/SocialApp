package com.salman.socialapp.util

import android.content.Context
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.preference.PreferenceManager
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.salman.socialapp.model.Chat
import com.salman.socialapp.model.FirebaseUserInfo
import com.salman.socialapp.model.Friend
import com.salman.socialapp.model.UserInfo
import com.salman.socialapp.ui.activities.MY_LANGUAGE
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import java.io.*
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
private const val KEY_SAVED_AT = "savedAt"
const val MINIUMUM_INTERVAL = 6
const val ADDRESS = "www.google.com"
const val PORT = 80
const val TIMEOUT = 1500
const val FILENAME_V1 = "Friends.data"
const val FILENAME_V2 = "Chats.data"
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

    fun setLastSavedAt(savedAt: String) {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        sharedPreferences.edit().putString(KEY_SAVED_AT, savedAt).apply()
    }

    fun getLastSavedAt(): String? {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        return sharedPreferences.getString(KEY_SAVED_AT, null)
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

        private fun getDataFile(context: Context) = File(context.filesDir, FILENAME_V1)

        fun writeFriendListToFile(context: Context, friendList: List<Friend>)  {
/*            val outputStream = FileOutputStream(getDataFile(context))
            ObjectOutputStream(outputStream).use {
                it.writeObject(friendList)
            }*/

            val moshi = Moshi.Builder().build()
            val type = Types.newParameterizedType(List::class.java, Friend::class.java)
            val adapter: JsonAdapter<List<Friend>> = moshi.adapter(type)
            val json = adapter.toJson(friendList)
            val file = File(context.filesDir, FILENAME_V1)
            file.writeText(json, Charsets.UTF_8)
        }

        fun removeFriendListFromFile(context: Context, json: String)  {
/*            val outputStream = FileOutputStream(getDataFile(context))
            ObjectOutputStream(outputStream).use {
                it.writeObject(emptyList)
            }*/

            val file = File(context.filesDir, FILENAME_V1)
            file.writeText(json, Charsets.UTF_8)
        }

        fun readFriendListFromFile(context: Context): List<Friend>? {
/*            val dataFile = getDataFile(context)
            if (!dataFile.exists()) {
                return null
            }
            val inputStream = FileInputStream(getDataFile(context))
            ObjectInputStream(inputStream).use {
                return it.readObject() as List<Friend>
            }*/

            val file = File(context.filesDir, FILENAME_V1)
            if (!file.exists()) {
                return null
            }
            val moshi = Moshi.Builder().build()
            val type = Types.newParameterizedType(List::class.java, Friend::class.java)
            val adapter: JsonAdapter<List<Friend>> = moshi.adapter(type)
            val friends = adapter.fromJson(file.readText())
            return friends
        }

        fun saveChatListToFile(context: Context, chats: List<Chat>) {
            val moshi = Moshi.Builder().build()
            val type = Types.newParameterizedType(List::class.java, Chat::class.java)
            val adapter: JsonAdapter<List<Chat>> = moshi.adapter(type)
            val json = adapter.toJson(chats)
            val file = File(context.filesDir, FILENAME_V2)
            file.writeText(json, Charsets.UTF_8)
        }

        fun removeChatListFromFile(context: Context, json: String) {
            val file = File(context.filesDir, FILENAME_V2)
            file.writeText(json, Charsets.UTF_8)
        }

        fun readChatListFromFile(context: Context): List<Chat>? {
            val file = File(context.filesDir, FILENAME_V2)
            if (!file.exists()) {
                return null
            }
            val moshi = Moshi.Builder().build()
            val type = Types.newParameterizedType(List::class.java, Chat::class.java)
            val adapter: JsonAdapter<List<Chat>> = moshi.adapter(type)
            val chats = adapter.fromJson(file.readText())
            return chats
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