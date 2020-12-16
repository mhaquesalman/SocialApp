package com.salman.socialapp.model

data class User(
    val uid: String?,
    val name: String?,
    val email: String?,
    val profileUrl: String?,
    val coverUrl: String?,
    val userToken: String?
)