package com.salman.socialapp.util

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.salman.socialapp.model.UserInfo
import java.lang.reflect.Type

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
}