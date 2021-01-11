package com.salman.socialapp.model

data class Comment(
    val profileUrl: String? = null,
    val userToken: String? = null,
    val postUserId: String? = null,
    var comments: MutableList<Comment> = ArrayList(),
    val commentBy: String? = null,
    val commentDate: String? = null,
    val name: String? = null,
    val comment: String? = null,
    val commentPostId: String? = null,
    val commentOn: String? = null,
    val parentId: String? = null,
    val cid: String? = null,
    var totalCommentReplies: Int = 0
)