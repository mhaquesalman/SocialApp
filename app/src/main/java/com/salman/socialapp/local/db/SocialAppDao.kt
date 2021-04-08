package com.salman.socialapp.local.db

import androidx.room.*
import com.salman.socialapp.local.ProfilePost
import com.salman.socialapp.model.Post
import com.salman.socialapp.model.Profile

@Dao
interface SocialAppDao {

    @Query("SELECT * FROM posts ORDER BY statusTime DESC")
    fun getPostListFromRoom(): List<Post>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePostListToRoom(posts: List<Post>)

    @Query("DELETE FROM posts")
    suspend fun deletePostListFromRoom()

    @Query("SELECT * FROM profile")
    fun getProfileDataFromRoom(): Profile

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun saveProfileDataToRoom(profile: Profile)

    @Query("DELETE FROM profile")
    suspend fun deleteProfileDataFromRoom()

    @Query("SELECT * FROM profile_posts ORDER BY statusTime DESC")
    fun getProfilePostListFromRoom(): List<ProfilePost>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun saveProfilePostListToRoom(profilePosts: List<ProfilePost>)

    @Query("DELETE FROM profile_posts")
    suspend fun deleteProfilePostListFromRoom()



}