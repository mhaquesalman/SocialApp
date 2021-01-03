package com.salman.socialapp.model

data class PerformReaction(
    val userId: String? = null,
    val postId: String? = null,
    val postOwnerId: String? = null,
    val previousReactionType: String? = null,
    val newReactionType: String? = null
)