package com.salman.socialapp.model

data class Post(
    val postId: String? = null,
    val postUserId: String? = null,
    val post: String? = null,
    val statusImage: String? = null,
    val statusTime: String? = null,
    val privacy: String? = null,
    val uid: String? = null,
    val name: String? = null,
    val email: String? = null,
    val profileUrl: String? = null,
    val coverUrl: String? = null,
    val userToken: String? = null
)