package com.salman.socialapp.model

data class PostResponse(
    val message: String? = null,
    val status: Int = 0,
    val posts: List<Post> = ArrayList()
)