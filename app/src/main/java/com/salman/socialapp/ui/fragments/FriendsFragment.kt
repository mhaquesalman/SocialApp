package com.salman.socialapp.ui.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.salman.socialapp.R
import com.salman.socialapp.adapters.FriendRequestAdapter
import com.salman.socialapp.adapters.FriendsAdapter
import com.salman.socialapp.ui.activities.MainActivity
import com.salman.socialapp.util.Utils
import com.salman.socialapp.util.hide
import com.salman.socialapp.util.show
import com.salman.socialapp.util.showToast
import com.salman.socialapp.viewmodels.MainViewModel
import com.salman.socialapp.viewmodels.ViewModelFactory
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_friends.*


class FriendsFragment : Fragment() {
    private val TAG = "FriendsFragment"
    lateinit var mContext: Context
    lateinit var mainViewModel: MainViewModel
    lateinit var friendsAdapter: FriendsAdapter
    lateinit var friendRequestAdapter: FriendRequestAdapter
    var userIdFromSharedPref: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_friends, container, false)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initialization()
        loadFriends()

    }

    private fun initialization() {
        mainViewModel = ViewModelProvider(mContext as FragmentActivity, ViewModelFactory()).get(MainViewModel::class.java)
        friendsAdapter = FriendsAdapter(mContext)
        friend_listRV.layoutManager = LinearLayoutManager(mContext)
        friend_listRV.adapter = friendsAdapter
        friendRequestAdapter = FriendRequestAdapter(mContext)
        friend_reqRV.layoutManager = LinearLayoutManager(mContext)
        friend_reqRV.adapter = friendRequestAdapter

/*        Log.d(TAG, "userid-01: " + userIdFromSharedPref!!)
        Log.d(TAG, "userid-02: " + currentUserId!!)
        Log.d(TAG, "useridFirebase: " + FirebaseAuth.getInstance().uid!!) */
    }

    private fun loadFriends() {
//        progressbar.show()
        (activity as MainActivity).showProgressBar()
        mainViewModel.loadFriends(FirebaseAuth.getInstance().uid!!)?.observe(this, Observer { friendResponse ->
//            progressbar.hide()
            (activity as MainActivity).hideProgressBar()
            if (friendResponse.status == 200) {
                friendsAdapter.updateList(friendResponse.result!!.friends)
                friendRequestAdapter.updateList(friendResponse.result.requests)
                if (friendResponse.result.friends.size > 0) {
                    friend_title.visibility = View.VISIBLE
                } else {
                    friend_title.visibility = View.GONE
                }
                if (friendResponse.result.requests.size > 0) {
                    request_title.visibility = View.VISIBLE
                } else {
                    request_title.visibility = View.GONE
                }
                if (friendResponse.result.friends.size == 0 && friendResponse.result.requests.size == 0) {
                    defaultTV.visibility = View.VISIBLE
                }
            } else {
//                Toast.makeText(mContext, friendResponse.message, Toast.LENGTH_LONG).show()
                mContext.showToast(friendResponse.message)
            }
        })
    }

    companion object {
        fun getInstance() = FriendsFragment()
    }
}