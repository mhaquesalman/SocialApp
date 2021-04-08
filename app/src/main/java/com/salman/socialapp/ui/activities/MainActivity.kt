package com.salman.socialapp.ui.activities

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.salman.socialapp.R
import com.salman.socialapp.adapters.FriendRequestAdapter
import com.salman.socialapp.adapters.PostAdapter
import com.salman.socialapp.model.PerformAction
import com.salman.socialapp.ui.fragments.FriendsFragment
import com.salman.socialapp.ui.fragments.NewsFeedFragment
import com.salman.socialapp.ui.fragments.NotificationFragment
import com.salman.socialapp.util.*
import com.salman.socialapp.viewmodels.MainViewModel
import com.salman.socialapp.viewmodels.ViewModelFactory
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

private const val TAG = "MainActivity"
const val USER_ID = "uid"
const val MY_LANGUAGE = "My_language"

@AndroidEntryPoint
class MainActivity : AppCompatActivity(),
    FriendRequestAdapter.OnClickPerformAction,
    PostAdapter.IUpdateUserReaction,
    IOnCommentAdded,
    IOnCommentDeletePostUpdate {

    lateinit var mainViewModel: MainViewModel
    val newsFeedFragment = NewsFeedFragment.getInstance()
    val friendsFragment = FriendsFragment.getInstance()
    val notificationFragment = NotificationFragment.getInstance()
    var currentUserId: String? = ""
    var userImage: String? = ""
    var getLang: String = ""
    var firebaseUser: FirebaseUser? = null
    var reference: DatabaseReference? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // load language from sharedPref
        getLang = getLocale()
        setContentView(R.layout.activity_main)

        setSupportActionBar(toolbar)
//        supportActionBar?.setTitle(Html.fromHtml("<font color='#FFFFFF'>SocialApp</font>"))


        setMenuTintColor(toolbar)

        val bundle = intent.extras
        if (bundle != null) {
            if (bundle.containsKey("isFromNotification")) {
                setFragment(notificationFragment)
            }
            bottomNavigation.menu.findItem(R.id.notificationFragment).setChecked(true)
        } else {
            setFragment(newsFeedFragment)
        }

        // set bottom navigation bar
        setBottomNavigationView()

        // get user from sharedRef
        getUserFromSharedPref()

        mainViewModel = ViewModelProvider(this, ViewModelFactory()).get(MainViewModel::class.java)

        CommentsBottomDialog.setIOnCommentDeletePostUpdate(this)
        CommentRepliesBottomDialog.setIOnCommentDeletePostUpdate(this)

        fab.setOnClickListener {
            startActivity(Intent(this, PostUploadActivity::class.java)
                .putExtra("profileUrl", userImage)
                .putExtra("editPost", false))
        }

        toolbar_search.setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
        }

    }

    private fun setMenuTintColor(toolbar: Toolbar?) {
        var drawable = toolbar?.overflowIcon
        if (drawable != null) {
            drawable = DrawableCompat.wrap(drawable)
            DrawableCompat.setTint(drawable.mutate(), resources.getColor(android.R.color.white))
            toolbar?.overflowIcon = drawable
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.app_bar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.message_menu -> {
                val mIntent = Intent(this, MessageActivity::class.java)
                startActivity(mIntent)
            }
            R.id.settings_menu -> {
                showDialog()
            }
        }
        return true
    }

    private fun getUserFromSharedPref() {
        Utils(this).apply {
            val userInfo = getUserFromSharedPref()
            Log.d(TAG, "UserInfo: $userInfo")
            if (userInfo != null) {
                currentUserId = userInfo.uid
                userImage = userInfo.profileUrl
            } else {
                firebaseUser = FirebaseAuth.getInstance().currentUser
                currentUserId = firebaseUser?.uid
            }
        }
    }

    private fun setBottomNavigationView() {
        bottomNavigation.setOnNavigationItemSelectedListener { item ->
            when(item.itemId) {
                R.id.newsFeedFragment -> {
                    setFragment(newsFeedFragment)
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.friendsFragment -> {
                    setFragment(friendsFragment)
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.profileActivity -> {
                    startActivity(Intent(this, ProfileActivity::class.java)
                        .putExtra(USER_ID, currentUserId))
                    return@setOnNavigationItemSelectedListener false
                }
                R.id.notificationFragment -> {
                    setFragment(notificationFragment)
                    return@setOnNavigationItemSelectedListener true
                }
            }
            return@setOnNavigationItemSelectedListener true
        }
    }

    private fun showDialog() {
        val items = arrayOf("English", "বাংলা")
        val builder = AlertDialog.Builder(this).apply {
            setTitle(resources.getString(R.string.choose_Language_title))
            val checkedItem = if (getLang.equals("bn")) 1 else 0
            setSingleChoiceItems(items, checkedItem, DialogInterface.OnClickListener { dialogInterface, i ->
                if (i == 0) {
                    setLocale("en")
                    recreate()
                } else {
                    setLocale("bn")
                    recreate()
                }
                dialogInterface.dismiss()
            })
        }
        val dialog = builder.create()
        dialog.show()
    }

    private fun setLocale(lang: String) {
        val locale = Locale(lang)
        Locale.setDefault(locale)
        val configuration = Configuration()
        configuration.locale = locale
        baseContext.resources.updateConfiguration(configuration, baseContext.resources.displayMetrics)
        val editor = getSharedPreferences("Settings", Context.MODE_PRIVATE).edit()
        editor.putString(MY_LANGUAGE, lang)
        editor.commit()
    }

    private fun getLocale(): String {
        val prefs = getSharedPreferences("Settings", Context.MODE_PRIVATE)
        val lang = prefs.getString(MY_LANGUAGE, "en") ?: "en"
        setLocale(lang)
        return lang
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

    override fun updateUserReaction(
        uId: String,
        postId: Int,
        postOwnerId: String,
        previousReactionType: String,
        newReactionType: String,
        position: Int
    ) {
        newsFeedFragment.updateUserReaction(uId, postId, postOwnerId, previousReactionType, newReactionType, position)
    }

    override fun onCommentAdded(position: Int) {
        newsFeedFragment.onCommentAdded(position)
    }

    override fun onCommentDeletePostUpdate(position: Int) {
        newsFeedFragment.onCommentDelete(position)
    }

    companion object {

    }

}