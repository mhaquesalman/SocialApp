package com.salman.socialapp.util

import com.salman.socialapp.model.Comment

interface IOnCommentRepliesAdded {
    fun oncommentRepliesAdded(mAdapterPosition: Int, comment: Comment)
}