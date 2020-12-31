package com.salman.socialapp.ui.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.salman.socialapp.R
import com.salman.socialapp.adapters.FriendRequestAdapter
import com.salman.socialapp.model.PerformAction
import com.salman.socialapp.ui.fragments.FriendsFragment
import com.salman.socialapp.ui.fragments.NewsFeedFragment
import com.salman.socialapp.util.Utils
import com.salman.socialapp.util.showToast
import com.salman.socialapp.viewmodels.MainViewModel
import com.salman.socialapp.viewmodels.ViewModelFactory
import kotlinx.android.synthetic.main.activity_main.*

private const val TAG = "MainActivity"
const val USER_ID = "uid"
class MainActivity : AppCompatActivity(), FriendRequestAdapter.OnClickPerformAction {

     lateinit var mainViewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setFragment(NewsFeedFragment.getInstance())
        setBottomNavigationView()

        mainViewModel = ViewModelProvider(this, ViewModelFactory()).get(MainViewModel::class.java)

        fab.setOnClickListener {
            startActivity(Intent(this, PostUploadActivity::class.java))
        }

        toolbar_search.setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
        }

        // get user from sharedRef
        Utils(this).apply {
            val userFromSharedPref = getUserFromSharedPref()
            Log.d(TAG, "UserInfo: $userFromSharedPref")
        }
    }

    private fun setBottomNavigationView() {
        bottomNavigation.setOnNavigationItemSelectedListener { item ->
            when(item.itemId) {
                R.id.newsFeedFragment -> {
                    setFragment(NewsFeedFragment.getInstance())
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.friendsFragment -> {
                    setFragment(FriendsFragment.getInstance())
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.profileActivity -> {
                    startActivity(Intent(this, ProfileActivity::class.java)
                        .putExtra(USER_ID, FirebaseAuth.getInstance().uid))
                    return@setOnNavigationItemSelectedListener false
                }
            }
            return@setOnNavigationItemSelectedListener true
        }
    }

    private fun setFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.frameLayout, fragment)
            .commit()
    }

    fun showProgressBar() {
        progressbar.visibility = View.VISIBLE

    }

    fun hideProgressBar() {
        progressbar.visibility = View.GONE
    }

    override fun performAction(position: Int, profileId: String, operationType: Int) {
        val currentUserId = FirebaseAuth.getInstance().uid
        showProgressBar()
        val performAction = PerformAction(operationType.toString(), currentUserId, profileId)
        mainViewModel.performFriendAction(performAction)?.observe(this, Observer { generalResponse ->
            hideProgressBar()
            showToast(generalResponse.message)
            if (generalResponse.status == 200) {
                val response = mainViewModel.loadFriends(currentUserId!!)?.value
                response?.let {
                    it.result?.requests?.removeAt(position)
                    mainViewModel.loadFriends(currentUserId)?.value = it
                }
            }
        })
    }

}