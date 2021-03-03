package com.salman.socialapp.util

interface IOnCommentDelete {
    fun onCommentDelete(
        position: Int,
        cid: String?,
        postId: String?,
        commentOn: String?
    )
}