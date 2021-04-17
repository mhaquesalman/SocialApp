package com.salman.socialapp.ui.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuItemCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import com.salman.socialapp.R
import com.salman.socialapp.adapters.FirebaseUserInfoAdapter
import com.salman.socialapp.model.FirebaseUserInfo
import com.salman.socialapp.model.Friend
import com.salman.socialapp.ui.activities.MessageActivity
import com.salman.socialapp.ui.activities.OutgoingInvitationActivity
import com.salman.socialapp.util.Utils
import com.salman.socialapp.util.showToast
import com.salman.socialapp.viewmodels.MainViewModel
import com.salman.socialapp.viewmodels.ViewModelFactory
import kotlinx.android.synthetic.main.fragment_message.*

private const val TAG = "MessageFragment"
class MessageFragment : Fragment(), FirebaseUserInfoAdapter.IOCallSetup {
    lateinit var mContext: Context
    lateinit var mainViewModel: MainViewModel
    lateinit var firebaseUserInfoAdapter: FirebaseUserInfoAdapter
    var friendList: MutableList<FirebaseUserInfo> = ArrayList()
    var friends: List<Friend> = ArrayList()
    var userId: String? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_message, container, false)

        userId = FirebaseAuth.getInstance().currentUser?.uid
        Log.d(TAG, "onCreateView: $userId")

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initialization()

        loadFriends()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.message_menu, menu)

        // SearchView
        val item = menu.findItem(R.id.search_menu)
        val searchView = MenuItemCompat.getActionView(item) as SearchView
//        val search_view = item.getActionView() as SearchView

        // SearchView Listener
        searchView.setOnQueryTextListener(onQueryTextListener)

        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun initialization() {
        mainViewModel = ViewModelProvider(mContext as FragmentActivity, ViewModelFactory()).get(MainViewModel::class.java)
        messageRV.setHasFixedSize(true)
        messageRV.layoutManager = LinearLayoutManager(mContext)
        firebaseUserInfoAdapter = FirebaseUserInfoAdapter(mContext, friendList, true)
        messageRV.adapter = firebaseUserInfoAdapter
        firebaseUserInfoAdapter.setIOcallsetup(this)
    }

    private fun loadFriends() {
        (activity as MessageActivity).showProgressBar()
        mainViewModel.loadFriends(FirebaseAuth.getInstance().uid!!)?.observe(viewLifecycleOwner, Observer { friendResponse ->
                if (friendResponse.status == 200) {
//                friendList.clear()
                    val friends: List<Friend> = friendResponse.result!!.friends
                    this.friends = friends
//                friendList.addAll(friends)
                    loadFriendsFromFirebase()
//                friendsAdapter.updateList(friends)
//                if (friends.size == 0)
//                    defaultTV.visibility = View.VISIBLE
//                else
//                    defaultTV.visibility = View.GONE
                } else {
                    mContext.showToast(friendResponse.message)
                }
            })
    }

    private fun loadFriendsFromFirebase() {
        val dataRef = FirebaseDatabase.getInstance().getReference("fusers")

        dataRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                (activity as MessageActivity).hideProgressBar()
                friendList.clear()
                for (snapshot in dataSnapshot.children) {
                    val firebaseUserInfo = snapshot.getValue(FirebaseUserInfo::class.java)
                    for (friend in friends) {
                        if (firebaseUserInfo!!.id.equals(friend.uid)) {
                            friendList.add(firebaseUserInfo)
                        }
                    }
                }
                firebaseUserInfoAdapter.notifyDataSetChanged()

                if (friends.size == 0)
                    defaultTV.visibility = View.VISIBLE
                else
                    defaultTV.visibility = View.GONE
            }
            override fun onCancelled(error: DatabaseError) {
                mContext.showToast(error.message)
            }
        })
    }

    private fun searchFriendsFromFirebase(query: String) {
        val searchText = query.toLowerCase()
        val dataRef = FirebaseDatabase.getInstance().getReference("fusers")

        dataRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                friendList.clear()
                for (snapshot in dataSnapshot.children) {
                    val firebaseUserInfo = snapshot.getValue(FirebaseUserInfo::class.java)
                    for (friend in friends) {
                        if (firebaseUserInfo!!.id.equals(friend.uid)) {
                            if (firebaseUserInfo.name.toLowerCase().contains(searchText)) {
                                friendList.add(firebaseUserInfo)
                            }
                        }
                    }
                }
                // refresh adapter after searching
                firebaseUserInfoAdapter.notifyDataSetChanged()

                if (friends.size == 0) defaultTV.visibility = View.VISIBLE
                else defaultTV.visibility = View.GONE
            }
            override fun onCancelled(error: DatabaseError) {
                mContext.showToast(error.message)
            }
        })
    }

    private val onQueryTextListener: SearchView.OnQueryTextListener = object : SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(s: String?): Boolean {
            if (!TextUtils.isEmpty(s!!.trim())) {
                searchFriendsFromFirebase(s)
            } else {
                loadFriendsFromFirebase()
            }
            return false
        }

        override fun onQueryTextChange(s: String?): Boolean {
            if (!TextUtils.isEmpty(s!!.trim())) {
                searchFriendsFromFirebase(s)
            } else {
                loadFriendsFromFirebase()
            }
            return false
        }
    }

    companion object {
        fun getInstance()= MessageFragment()
    }

    override fun initiateAudioCall(firebaseUserInfo: FirebaseUserInfo) {
        val mIntent = Intent(mContext, OutgoingInvitationActivity::class.java)
        mIntent.putExtra("user", firebaseUserInfo)
        mIntent.putExtra("type", "audio")
        startActivity(mIntent)
    }

    override fun initiatevideoCall(firebaseUserInfo: FirebaseUserInfo) {
        val mIntent = Intent(mContext, OutgoingInvitationActivity::class.java)
        mIntent.putExtra("user", firebaseUserInfo)
        mIntent.putExtra("type", "video")
        startActivity(mIntent)
    }

    override fun onMultipleUsersAction(isMulipleUserSelected: Boolean) {
        Log.d(TAG, "initiateAudioCall: $isMulipleUserSelected")
        if (isMulipleUserSelected) {
            val selectedUsers = firebaseUserInfoAdapter.getSelectedUsers()
            if (selectedUsers.size >= 2)
                conference_btn.visibility = View.VISIBLE
            conference_btn.setOnClickListener {
                val intent = Intent(mContext, OutgoingInvitationActivity::class.java)
                intent.putExtra("selectedUsers", Gson().toJson(selectedUsers))
                intent.putExtra("type", "video")
                intent.putExtra("isMultiple", true)
                startActivity(intent)
            }
        } else {
            conference_btn.visibility = View.GONE
        }
    }
}