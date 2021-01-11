package com.salman.socialapp.model

data class CommentResponse(
	val comments: List<Comment> = ArrayList(),
	val message: String? = null,
	val status: Int = 0
)
