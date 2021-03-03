package com.salman.socialapp.local.db

import androidx.room.*
import com.salman.socialapp.model.Post

@Dao
interface SocialAppDao {

    @Query("SELECT * FROM posts ORDER BY statusTime DESC")
    fun getPostListFromRoom(): List<Post>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePostListToRoom(posts: List<Post>)

    @Insert
    suspend fun savePostToRoom(post: Post)

    @Query("DELETE FROM posts")
    suspend fun deletePostListFromRoom()
}