package com.salman.socialapp.viewmodels

import androidx.lifecycle.ViewModel
import com.salman.socialapp.model.PerformReaction
import com.salman.socialapp.repositories.Repository

class PostDetailViewModel(val repository: Repository?) : ViewModel() {

    fun performReaction(performReaction: PerformReaction) =
        repository?.performReaction(performReaction)

}