package com.salman.socialapp.model

data class Chat(
    val id: String? = null,
    val sender: String? = null,
    val receiver: String? = null,
    val message: String? = null,
    val image: Boolean = false,
    val seen: Boolean = false
)