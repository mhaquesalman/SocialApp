package com.salman.socialapp.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.salman.socialapp.model.FriendResponse
import com.salman.socialapp.model.PerformAction
import com.salman.socialapp.repositories.Repository

class MainViewModel(val repository: Repository?) : ViewModel() {

    private var friends: MutableLiveData<FriendResponse>? = null

//    fun loadFriends(uid: String) = repository?.loadfriends(uid)

    fun loadFriends(uid: String): MutableLiveData<FriendResponse>? {
        if (friends == null) {
            friends = repository?.loadfriends(uid)
        }
        return friends
    }

    fun performFriendAction(performAction: PerformAction) =
        repository?.performFriendAction(performAction)

    fun getNewsFeed(params: Map<String, String>)= repository?.getNewsFeed(params)
}