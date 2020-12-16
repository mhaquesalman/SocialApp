package com.salman.socialapp.viewmodels

import androidx.lifecycle.ViewModel
import com.salman.socialapp.repositories.Repository

class SearchViewModel(val repository: Repository?) : ViewModel() {

    fun search(params: Map<String, String>) = repository?.search(params)

}