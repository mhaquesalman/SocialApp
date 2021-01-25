package com.salman.socialapp.model

data class NotificationResponse(
    val message: String? = null,
    val status: Int = 0,
    val notifications: List<Notification>? = null
)