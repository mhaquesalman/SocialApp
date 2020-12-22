package com.salman.socialapp.model

data class Result(
	val requests: MutableList<Request>,
	val friends: List<Friend>
)