package com.salman.socialapp.model

data class Reaction(
	val loveCount: Int = 0,
	val careCount: Int = 0,
	val wowCount: Int = 0,
	val sadCount: Int = 0,
	val angryCount: Int = 0,
	val likeCount: Int = 0,
	val hahaCount: Int = 0,
	val reactionType: String? = null
)
