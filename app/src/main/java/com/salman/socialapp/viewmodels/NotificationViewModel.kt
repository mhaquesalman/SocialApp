package com.salman.socialapp.viewmodels

import androidx.lifecycle.ViewModel
import com.salman.socialapp.repositories.Repository

class NotificationViewModel(val repository: Repository?) : ViewModel() {

    fun getNotification(uid: String) = repository?.getNotification(uid)
}