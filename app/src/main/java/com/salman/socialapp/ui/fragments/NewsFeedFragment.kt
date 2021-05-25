package com.salman.socialapp.ui.fragments

import android.content.Context
import android.database.Observable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.salman.socialapp.R
import com.salman.socialapp.adapters.PostAdapter
import com.salman.socialapp.adapters.StoryAdapter
import com.salman.socialapp.local.viewmodel.LocalViewModel
import com.salman.socialapp.model.FirebaseUserInfo
import com.salman.socialapp.model.PerformReaction
import com.salman.socialapp.model.Post
import com.salman.socialapp.model.Story
import com.salman.socialapp.ui.activities.AddStoryActivity
import com.salman.socialapp.ui.activities.MainActivity
import com.salman.socialapp.util.Utils
import com.salman.socialapp.util.showToast
import com.salman.socialapp.viewmodels.MainViewModel
import com.salman.socialapp.viewmodels.ViewModelFactory
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_news_feed.*
import kotlinx.coroutines.*
import java.lang.Runnable
import kotlin.concurrent.thread

private const val TAG = "NewsFeed"

@AndroidEntryPoint
class NewsFeedFragment : Fragment() {

    lateinit var mContext: Context
    lateinit var mainViewModel: MainViewModel
    lateinit var localViewModel: LocalViewModel
    var postAdapter: PostAdapter? = null
    var storyAdapter: StoryAdapter? = null
    var currentUserId: String? = ""
    var limit = 5
    var offset = 0
    var isFirstLoading = true
    var isDataAvailable = true
    private val postItems: MutableList<Post> = ArrayList()
    private val storyList: MutableList<Story> = ArrayList()
    private val userList: MutableList<FirebaseUserInfo> = ArrayList()
    private var friendIds: ArrayList<String> = ArrayList()
    private var userIds: ArrayList<String> = ArrayList()
//    private var postListFromLocalDb: List<Post> = ArrayList()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_news_feed, container, false)

        // get user from sharedRef
        this.currentUserId = (activity as MainActivity).currentUserId
        Log.d(TAG, "onCreateView called currentUserId: $currentUserId")
        return view
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d(TAG, "onViewCreated: called")
        init()

        if (Utils.isNetworkAvailable(mContext)) {
            fetchNewsFeed()
            loadFriends()
        } else {
            getPostListFromLocalDb()

/*            CoroutineScope(Dispatchers.IO).launch {
                getPostListFromLocalDb()
                withContext(Dispatchers.Main) {
                    delay(500)
                    mContext.showToast("No Internet Connection !")
                }
            }*/
        }


    }


    private fun init() {
        mainViewModel = ViewModelProvider(mContext as FragmentActivity, ViewModelFactory()).get(MainViewModel::class.java)
        localViewModel = ViewModelProvider(mContext as FragmentActivity).get(LocalViewModel::class.java)
//        localViewModel = ViewModelProvider((mContext as FragmentActivity), ViewModelProvider.AndroidViewModelFactory((mContext as FragmentActivity).application)).get(LocalViewModel::class.java)
        newsFeedRV.layoutManager = LinearLayoutManager(mContext)

        storyRV.layoutManager = LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false)
        storyRV.setHasFixedSize(true)

        storyAdapter = StoryAdapter(mContext, storyList, currentUserId!!, userList)
        storyRV.adapter = storyAdapter

        scrollToMorePosts()

        refreshPosts()

        AddStoryActivity.setOnStoryAddedAction {
            storyAdapter?.showShimmer = true
            storyAdapter?.notifyDataSetChanged()
            fetchStories()
        }

    }

    private fun loadFriends() {
        Log.d(TAG, "loadFriends: called")
        mainViewModel.loadFriends(currentUserId!!)?.observe(viewLifecycleOwner, Observer { friendResponse ->
                if (friendResponse.status == 200) {
                    if (friendResponse.result != null) {
                        friendIds.add(FirebaseAuth.getInstance().currentUser?.uid!!)
                        val friends = friendResponse.result.friends
                        for (friend in friends) {
                            friendIds.add(friend.uid!!)
                        }
                    }
                }
            fetchStories()
            actionStoryAdapter()
            })

    }

    private fun fetchStories() {
        var story: Story?
        var count: Int
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val dataRef = FirebaseDatabase.getInstance().getReference("Story")
        dataRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Log.d(TAG, "onDataChange: called")
                storyList.clear()
                userIds.clear()
                if (!dataSnapshot.child(currentUserId!!).exists()) {
                    Log.d(TAG, "onDataChange: not exists called")
                    storyList.add(Story("", 0, 0, "", firebaseUser?.uid))
                    userIds.add(firebaseUser?.uid!!)
                }
                Log.d(TAG, "onDataChange: friendIds: $friendIds")
                for (id in friendIds) {
                    story = null
                    count = 0
                    for (snapshot in dataSnapshot.child(id).children) {
                        story = snapshot.getValue(Story::class.java)
                        val timeCurrent = System.currentTimeMillis()
                        if (timeCurrent > story!!.timeStart && timeCurrent < story!!.timeEnd) {
                            count++
                        }
                    }
                    Log.d(TAG, "onDataChange: story: ${story}")
                    if (count > 0 && story != null) {
                        storyList.add(story!!)
                        userIds.add(story?.userid!!)
                        Log.d(TAG, " onDataChange count > 0: $userIds")
                    } else if (id.equals(currentUserId) && count == 0 &&
                        dataSnapshot.child(currentUserId!!).exists()) {
                        storyList.add(Story("", 0, 0, "", firebaseUser?.uid))
                        userIds.add(firebaseUser?.uid!!)
                        Log.d(TAG, "onDataChange count == -1: $userIds")
                    }

                }

                fetchFirebaseUsers()

            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun fetchFirebaseUsers() {
        Log.d(TAG, "onDataChange: fetchFirebaseUsers called")
        val dataRef = FirebaseDatabase.getInstance().getReference("fusers")
        dataRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                userList.clear()
                for (id in userIds) {
                    for (snapshot in dataSnapshot.children) {
                        val firebaseUser = snapshot.getValue(FirebaseUserInfo::class.java)
                        Log.d(TAG, "onDataChange: fusers: $firebaseUser")
                        if (firebaseUser!!.id.equals(id)) {
                            userList.add(firebaseUser)
                        }
                    }
                }

/*                val isUserExist = userList.stream().anyMatch {
                    it.id.equals(firebaseUser?.id)
                }
                if (!isUserExist) {
                    userList.add(firebaseUser!!)
                }*/

                storyAdapter?.showShimmer = false
                storyAdapter?.notifyDataSetChanged()
//                storyRV.visibility = View.VISIBLE
                Log.d(TAG, "onDataChange: userIds: ${userIds}")
                Log.d(TAG, "onDataChange: users: ${userList}")
                Log.d(TAG, "onDataChange: stories: ${storyList}")
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }


    private fun actionStoryAdapter() {

        storyAdapter?.setUserDetailsAction { userId ->
            var firebaseUser: FirebaseUserInfo? = null
            val dataRef = FirebaseDatabase.getInstance().getReference("fusers").child(userId)
            dataRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    firebaseUser = dataSnapshot.getValue(FirebaseUserInfo::class.java)
                    storyAdapter?.user = firebaseUser
//                    storyAdapter?.notifyDataSetChanged()
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
        }

        storyAdapter?.setSeenStoryAction { userId ->
            var count: Int
            val firebaseUser = FirebaseAuth.getInstance().currentUser
            val dataRef = FirebaseDatabase.getInstance().getReference("Story").child(userId)
            dataRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    count = 0
                    val timeCurrent = System.currentTimeMillis()
                    for (snapshot in dataSnapshot.children) {
                        val story = snapshot.getValue(Story::class.java)
                        if (snapshot.child("views").child(firebaseUser!!.uid).exists()
                            && timeCurrent < story!!.timeEnd) {
                            count ++
                        }
                    }
                    storyAdapter?.seenStoryCount?.postValue(count)
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
        }

        storyAdapter?.setMyStoryAction { userId ->
            var count: Int
            val dataRef = FirebaseDatabase.getInstance().getReference("Story").child(userId)
            dataRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    count = 0
                    val timeCurrent = System.currentTimeMillis()
                    for (snapshot in dataSnapshot.children) {
                        val story = snapshot.getValue(Story::class.java)
                        if (timeCurrent > story!!.timeStart && timeCurrent < story.timeEnd) {
                            count++
                        }
                    }
                    storyAdapter?.myStoryCount?.postValue(count)
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
        }

    }

    private fun scrollToMorePosts() {
        newsFeedRV.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0)
                    (activity as MainActivity).fabCollapse()
                else
                    (activity as MainActivity).fabExpand()

                if (Utils.isNetworkAvailable(mContext)) {
                    if (isLastItemReached() && isDataAvailable) {
                        offset += limit
                        fetchNewsFeed()
                    }
                }
            }
        })
    }

    private fun refreshPosts() {
        swipe.setOnRefreshListener {
            if (Utils.isNetworkAvailable(mContext)) {
                offset = 0
                isFirstLoading = true
                fetchNewsFeed()
            } else {
                mContext.showToast("Try connecting to the internet..")
                swipe.isRefreshing = false
            }
        }
    }

    private fun isLastItemReached(): Boolean {
        val layoutManager: LinearLayoutManager = newsFeedRV.layoutManager as LinearLayoutManager
        val position = layoutManager.findLastCompletelyVisibleItemPosition()
        val numberOfItems = postAdapter!!.itemCount
        Log.d(TAG, "position: $position | numberOfItems: $numberOfItems")
        return (position != -1 && position >= numberOfItems - 1)
    }

/*    override fun onResume() {
        super.onResume()
        fetchNewsFeed()
    }*/

    private fun fetchNewsFeed() {
        Log.d(TAG, "fetchNewsFeed: called")
        (activity as MainActivity).showProgressBar()
//        currentUserId = FirebaseAuth.getInstance().uid
        val params = HashMap<String, String>()
        params.put("uid", currentUserId!!)
        params.put("limit", limit.toString())
        params.put("offset", offset.toString())

        mainViewModel.getNewsFeed(params)?.observe(viewLifecycleOwner, Observer { postResponse ->
            (activity as MainActivity).hideProgressBar()
            if (postResponse.status == 200) {
            mContext.showToast(mContext.resources.getString(R.string.data_loading_toast), Toast.LENGTH_SHORT)
                if (swipe.isRefreshing) {
//                    postAdapter.posts.clear()
                    postItems.clear()
                    postAdapter?.notifyDataSetChanged()
                    swipe.isRefreshing = false
                }
                postItems.addAll(postResponse.posts)
//                postAdapter?.updateList(postResponse.posts)

                if (isFirstLoading) {
                    postAdapter = PostAdapter(mContext, postItems, currentUserId!!)
                    newsFeedRV.adapter = postAdapter
                } else {
                    postAdapter?.itemRangeInserted(postItems.size, postResponse.posts.size)
                }
                if (postResponse.posts.size == 0) {
                    offset -= limit
                    isDataAvailable = false
                } else {
                    isDataAvailable = true
                }
                isFirstLoading = false
            } else {
                if (swipe.isRefreshing) {
                    swipe.isRefreshing = false
                }
                mContext.showToast(postResponse.message)
            }
            if (isDataAvailable) {
                Log.d(TAG, "postItems: " + postItems.size)
                deletePostListFromLocalDb()
                savePostListToLocalDb(postItems)
            }
        })
    }

    private fun savePostListToLocalDb(postItems: MutableList<Post>) {
        Log.d(TAG, "savePostListToLocalDb: " + postItems.size)
        lifecycleScope.launch {
            localViewModel.savePostListToLocalDb(postItems)
        }
    }

    private fun deletePostListFromLocalDb() {
        Log.d(TAG, "deletePostListFromLocalDb: called")
        lifecycleScope.launch {
            localViewModel.deletePostListFromLocalDb()
        }
    }

    private fun getPostListFromLocalDb() {
        val handler = Handler()
        Thread (Runnable {
            val postListFromLocalDb = localViewModel.getPostListFromLocalDb()
            Log.d(TAG, "getPostListFromLocalDb: " + postListFromLocalDb.size)

            handler.postDelayed(Runnable {
                mContext.showToast("No Internet Connection !")
                // val list: MutableList<Post> = ArrayList(postListFromLocalDb)
                postAdapter = PostAdapter(mContext, postListFromLocalDb.toMutableList(), currentUserId!!)
                newsFeedRV.adapter = postAdapter
            }, 500)
        }).start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        offset = 0
        postItems.clear()
        isFirstLoading = true
    }

    fun updateUserReaction(
        uId: String,
        postId: Int,
        postOwnerId: String,
        previousReactionType: String,
        newReactionType: String,
        position: Int
    ) {
        val performReaction = PerformReaction(uId, postId.toString(), postOwnerId, previousReactionType, newReactionType)
        mainViewModel.performReaction(performReaction)?.observe(this, Observer { reactionResponse ->
            if (reactionResponse.status == 200) {
                postAdapter?.updatePostAfterReaction(position, reactionResponse.reaction!!)
            } else {
                mContext.showToast(reactionResponse.message)
            }
        })
    }

    fun onCommentAdded(position: Int) {
        postAdapter?.increaseCommentCount(position)
    }

    fun onCommentDelete(position: Int) {
        postAdapter?.decreaseCommentCount(position)
    }

    companion object {
        fun getInstance() = NewsFeedFragment()
    }
}