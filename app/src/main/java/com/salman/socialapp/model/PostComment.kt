package com.salman.socialapp.model

data class PostComment(
    val comment: String? = null,
    val commentBy: String? = null,
    val commentPostId: String? = null,
    val postUserId: String? = null,
    val commentOn: String? = null,
    val commentUserId: String? = null,
    val parentId: String? = null
)
