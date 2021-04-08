package com.salman.socialapp.local.repository

import com.salman.socialapp.local.ProfilePost
import com.salman.socialapp.local.db.SocialAppDao
import com.salman.socialapp.model.Post
import com.salman.socialapp.model.Profile
import javax.inject.Inject

class LocalRepository
@Inject
constructor (val socialAppDao: SocialAppDao) {

//    private val socialDao = SocialAppLocalDatabase.getSocialAppLocalDatabase(app).socialAppDao()

/*    init {
        CoroutineScope(Dispatchers.IO).launch {
            val data = socialDao.getPostListFromRoom()
        }
    }*/

    suspend fun savePostListToLocalDb(posts: List<Post>) = socialAppDao.savePostListToRoom(posts)

    suspend fun deletePostListFromLocalDb() = socialAppDao.deletePostListFromRoom()

    fun getPostListFromLocalDb() = socialAppDao.getPostListFromRoom()

    suspend fun saveProfileToLocalDb(profile: Profile) = socialAppDao.saveProfileDataToRoom(profile)

    suspend fun deleteProfileFromLocalDb() = socialAppDao.deleteProfileDataFromRoom()

    fun getProfileFromLocalDb() = socialAppDao.getProfileDataFromRoom()

    suspend fun saveProfilePostListToLocalDb(profilePosts: List<ProfilePost>) =
        socialAppDao.saveProfilePostListToRoom(profilePosts)

    suspend fun deleteProfilePostListFromLocalDb() = socialAppDao.deleteProfilePostListFromRoom()

    fun getProfilePostListFromLocalDb() = socialAppDao.getProfilePostListFromRoom()


}