package com.salman.socialapp.viewmodels

import androidx.lifecycle.ViewModel
import com.salman.socialapp.model.PostComment
import com.salman.socialapp.repositories.Repository

class CommentViewModel(val repository: Repository?) : ViewModel() {

    fun postComment(postComment: PostComment) = repository?.postComment(postComment)

    fun getPostComments(postId: String, postUserId: String) =
        repository?.getPostComments(postId, postUserId)

    fun getCommentReplies(postId: String, commentId: String) =
        repository?.getCommentReplies(postId, commentId)

    fun deleteComment(params: Map<String, String>) = repository?.deleteComment(params)

}