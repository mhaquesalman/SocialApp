package com.salman.socialapp.model

import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profile")
data class Profile(
    @PrimaryKey(autoGenerate = false)
    val uid: String = "",
    val profileUrl: String? = null,
    val coverUrl: String? = null,
    val userToken: String? = null,
    val name: String? = null,
    val state: String? = null,
    val email: String? = null
)