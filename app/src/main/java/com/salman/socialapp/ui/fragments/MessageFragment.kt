package com.salman.socialapp.ui.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.salman.socialapp.R
import com.salman.socialapp.adapters.FirebaseUserInfoAdapter
import com.salman.socialapp.adapters.FriendsAdapter
import com.salman.socialapp.model.FirebaseUserInfo
import com.salman.socialapp.model.Friend
import com.salman.socialapp.ui.activities.MessageActivity
import com.salman.socialapp.util.showToast
import com.salman.socialapp.viewmodels.MainViewModel
import com.salman.socialapp.viewmodels.ViewModelFactory
import kotlinx.android.synthetic.main.fragment_message.*

private const val TAG = "MessageFragment"
class MessageFragment : Fragment() {
    lateinit var mContext: Context
    lateinit var mainViewModel: MainViewModel
    lateinit var firebaseUserInfoAdapter: FirebaseUserInfoAdapter
    var friendList: MutableList<FirebaseUserInfo> = ArrayList()
    var userId: String? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
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
    private fun initialization() {
        mainViewModel = ViewModelProvider(mContext as FragmentActivity, ViewModelFactory()).get(MainViewModel::class.java)
        messageRV.setHasFixedSize(true)
        messageRV.layoutManager = LinearLayoutManager(mContext)
    }

    private fun loadFriends() {
        (activity as MessageActivity).showProgressBar()
        mainViewModel.loadFriends(FirebaseAuth.getInstance().uid!!)?.observe(viewLifecycleOwner, Observer { friendResponse ->
            if (friendResponse.status == 200) {
//                friendList.clear()
                val friends: List<Friend> = friendResponse.result!!.friends
//                friendList.addAll(friends)
                loadFriendsFromFirebase(friends)
//                friendsAdapter.updateList(friends)
//                if (friends.size == 0)
//                    defaultTV.visibility = View.VISIBLE
//                else
//                    defaultTV.visibility = View.GONE
            } else {
//                Toast.makeText(mContext, friendResponse.message, Toast.LENGTH_LONG).show()
                mContext.showToast(friendResponse.message)
            }
        })
    }

    private fun loadFriendsFromFirebase(friends: List<Friend>) {
        (activity as MessageActivity).hideProgressBar()
        val dataRef = FirebaseDatabase.getInstance().getReference("fusers")

        dataRef.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                friendList.clear()
                for (snapshot in dataSnapshot.children) {
                    val firebaseUserInfo = snapshot.getValue(FirebaseUserInfo::class.java)
                    for (friend in friends) {
                        if (firebaseUserInfo!!.id.equals(friend.uid)) {
                            friendList.add(firebaseUserInfo)
                        }
                    }
                }
                firebaseUserInfoAdapter = FirebaseUserInfoAdapter(mContext, friendList, true)
                messageRV.adapter = firebaseUserInfoAdapter

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


    companion object {
        fun getInstance()= MessageFragment()
    }
}