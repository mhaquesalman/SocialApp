package com.salman.socialapp.model

data class Post(
    val postId: Int = 0,
    val postUserId: String? = null,
    val post: String? = null,
    val statusImage: String? = null,
    val statusTime: String? = null,
    val privacy: String? = null,
    val uid: String? = null,
    val name: String? = null,
    val email: String? = null,
    val profileUrl: String? = null,
    val coverUrl: String? = null,
    val userToken: String? = null,
    var loveCount: Int = 0,
    var careCount: Int = 0,
    var wowCount: Int = 0,
    var sadCount: Int = 0,
    var angryCount: Int = 0,
    var likeCount: Int = 0,
    var hahaCount: Int = 0,
    var commentCount: Int = 0,
    var reactionType: String? = null
)