package com.salman.socialapp.model

data class SearchResponse(
    val message: String? = null,
    val status: Int = 0,
    val user: List<User> = ArrayList()
)