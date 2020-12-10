package com.salman.socialapp.model

data class Auth(
    val uid: String? = null,
    val name: String? = null,
    val email: String? = null,
    val profleUrl: String? = null,
    val coverUrl: String? = null,
    val userToken: String? = null
)