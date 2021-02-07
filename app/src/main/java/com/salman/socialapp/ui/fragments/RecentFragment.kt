package com.salman.socialapp.ui.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.salman.socialapp.R
import com.salman.socialapp.adapters.FirebaseUserInfoAdapter
import com.salman.socialapp.model.FirebaseUserInfo
import com.salman.socialapp.util.showToast
import com.salman.socialapp.viewmodels.ChatViewModel
import kotlinx.android.synthetic.main.fragment_recent.*

class RecentFragment : Fragment() {
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

        fetchRecentChats()
    }

    private fun fetchRecentChats() {
        chatViewModel.fetchRecentChats().observe(viewLifecycleOwner, Observer {
            recentChatList.clear()
            recentChatList.addAll(it)
            firebaseUserInfoAdapter.notifyDataSetChanged()
            Log.d("RecentFragment", "fetchRecentChats: " + it.size)
        })

        chatViewModel.getToastObserver().observe(viewLifecycleOwner, Observer {
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
}