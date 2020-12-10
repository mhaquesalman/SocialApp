package com.salman.socialapp.model

data class AuthResponse(
    val auth: Auth? = null,
    val message: String? = null,
    val status: Int = 0
)