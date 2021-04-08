package com.salman.socialapp.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profile_posts")
data class ProfilePost(
    @PrimaryKey(autoGenerate = false)
    val postId: Int,
    val postUserId: String?,
    val post: String?,
    val statusImage: String?,
    val statusTime: String?,
    val privacy: String?,
    val uid: String?,
    val name: String?,
    val profileUrl: String?,
    var loveCount: Int,
    var careCount: Int,
    var wowCount: Int,
    var sadCount: Int,
    var angryCount: Int,
    var likeCount: Int,
    var hahaCount: Int,
    var commentCount: Int,
    var reactionType: String?
)