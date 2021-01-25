package com.salman.socialapp.model

data class Notification(
    val nid: String? = null,
    val notificationTo: String? = null,
    val notificationFrom: String? = null,
    val type: String? = null,
    val notificationTime: String? = null,
    val postId: String? = null,
    val name: String? = null,
    val profileUrl: String? = null,
    val post: String? = null
)