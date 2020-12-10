package com.salman.socialapp.ui.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.salman.socialapp.R
import com.salman.socialapp.ui.fragments.FriendsFragment
import com.salman.socialapp.ui.fragments.NewsFeedFragment
import kotlinx.android.synthetic.main.activity_main.*

const val USER_ID = "uid"
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setFragment(NewsFeedFragment.getInstance())
        setBottomNavigationView()

        fab.setOnClickListener {
            startActivity(Intent(this, PostUploadActivity::class.java))
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
}