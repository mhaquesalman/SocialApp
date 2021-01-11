package com.salman.socialapp.util

import android.content.Context
import android.os.Build
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.salman.socialapp.model.UserInfo
import java.lang.reflect.Type
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*

private const val TAG = "Utils"
private const val SHARED_PREF = "logged_in_user"
private const val CURRENT_USER = "user"
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
        fun formatDate(date: String?): String {
/*            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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
    }

}