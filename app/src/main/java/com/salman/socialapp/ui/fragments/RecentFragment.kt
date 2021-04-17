package com.salman.socialapp.ui.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.salman.socialapp.R
import com.salman.socialapp.adapters.FirebaseUserInfoAdapter
import com.salman.socialapp.model.FirebaseUserInfo
import com.salman.socialapp.ui.activities.MessageActivity
import com.salman.socialapp.ui.activities.OutgoingInvitationActivity
import com.salman.socialapp.util.showToast
import com.salman.socialapp.viewmodels.ChatViewModel
import kotlinx.android.synthetic.main.fragment_message.*
import kotlinx.android.synthetic.main.fragment_recent.*

class RecentFragment : Fragment(), FirebaseUserInfoAdapter.IOCallSetup {
    lateinit var mContext: Context
    var recentChatList: MutableList<FirebaseUserInfo> = ArrayList()
    lateinit var firebaseUserInfoAdapter: FirebaseUserInfoAdapter

    private val chatViewModel: ChatViewModel by viewModels()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_recent, container, false)
    }

/*    private fun fetchUsers() {
        val dataRef = FirebaseDatabase.getInstance().getReference("chatList")
            .child(firabseUser?.uid!!)

        dataRef.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                recentChatList.clear()
                for (snapshot in dataSnapshot.children) {
                    val chatList = snapshot.getValue(ChatList::class.java)
                    recentChatList.add(chatList!!)
                }
                showRecentChatList()
            }

            override fun onCancelled(error: DatabaseError) {
                mContext.showToast(error.message)
            }
        })
    }*/

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recentRV.setHasFixedSize(true)
        recentRV.layoutManager = LinearLayoutManager(mContext)
        firebaseUserInfoAdapter = FirebaseUserInfoAdapter(mContext, recentChatList)
        recentRV.adapter = firebaseUserInfoAdapter
        firebaseUserInfoAdapter.setIOcallsetup(this)

        fetchRecentChats()
    }

    private fun fetchRecentChats() {
        (activity as MessageActivity).showProgressBar()
        chatViewModel.fetchRecentChats().observe(viewLifecycleOwner, Observer {
            (activity as MessageActivity).hideProgressBar()
            recentChatList.clear()
            recentChatList.addAll(it)
            firebaseUserInfoAdapter.notifyDataSetChanged()
            Log.d("RecentFragment", "fetchRecentChats: " + it.size)
        })

        chatViewModel.getToastObserver().observe(viewLifecycleOwner, Observer {
            (activity as MessageActivity).hideProgressBar()
            mContext.showToast(it)
        })
    }

    /*private fun showUsersOfRecentChats() {
        val dataRef = FirebaseDatabase.getInstance().getReference("fusers")

        dataRef.addValueEventListener(object :ValueEventListener {

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                userList.clear()
                for (snapshot in dataSnapshot.children) {
                    val firebaseUserInfo = snapshot.getValue(FirebaseUserInfo::class.java)
                    for (chatList in recentChatList) {
                        if (firebaseUserInfo?.id.equals(chatList.id)) {
                            userList.add(firebaseUserInfo!!)
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }*/

    /*private fun initialization() {
        recentRV.setHasFixedSize(true)
        recentRV.layoutManager = LinearLayoutManager(mContext)
        firabseUser = FirebaseAuth.getInstance().currentUser
    }*/

    companion object {
        fun getInstance() = RecentFragment()
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