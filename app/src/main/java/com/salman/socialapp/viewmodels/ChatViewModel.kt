package com.salman.socialapp.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.salman.socialapp.model.ChatList
import com.salman.socialapp.model.FirebaseUserInfo

class ChatViewModel: ViewModel() {
    private var firabseUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser
    private var recentChatList: MutableList<ChatList> = ArrayList()
    private var userList: MutableList<FirebaseUserInfo> = ArrayList()
    private val toastMessageObserver: MutableLiveData<String> = MutableLiveData()
//    private val recentChatListLiveData: MutableLiveData<List<ChatList>> = MutableLiveData()
    private val recentChatUserListLiveData: MutableLiveData<List<FirebaseUserInfo>> = MutableLiveData()


    fun fetchRecentChats(): MutableLiveData<List<FirebaseUserInfo>> {
        val dataRef = FirebaseDatabase.getInstance().getReference("chatList")
            .child(firabseUser?.uid!!)

        dataRef.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (snapshot in dataSnapshot.children) {
                    val chat = snapshot.getValue(ChatList::class.java)
                    recentChatList.add(chat!!)
                }
                showUsersOfRecentChats()
            }

            override fun onCancelled(error: DatabaseError) {
               toastMessageObserver.value = error.message
            }
        })

        return recentChatUserListLiveData
    }

    fun showUsersOfRecentChats() {
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
                recentChatUserListLiveData.value = userList
            }

            override fun onCancelled(error: DatabaseError) {
                toastMessageObserver.value = error.message
            }
        })
    }

    fun getToastObserver(): LiveData<String> {
        return toastMessageObserver
    }

}