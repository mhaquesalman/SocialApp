package com.salman.socialapp.ui.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.salman.socialapp.R
import com.salman.socialapp.adapters.PostAdapter
import com.salman.socialapp.model.PerformReaction
import com.salman.socialapp.model.Post
import com.salman.socialapp.ui.activities.MainActivity
import com.salman.socialapp.util.showToast
import com.salman.socialapp.viewmodels.MainViewModel
import com.salman.socialapp.viewmodels.ViewModelFactory
import kotlinx.android.synthetic.main.fragment_news_feed.*

private const val TAG = "NewsFeedFragment"
class NewsFeedFragment : Fragment() {

    lateinit var mContext: Context
    lateinit var mainViewModel: MainViewModel
    var postAdapter: PostAdapter? = null
    var currentUserId: String? = ""
    var limit = 5
    var offset = 0
    var isFirstLoading = true
    var isDataAvailable = true
    private val postItems: MutableList<Post> = ArrayList()

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
        Log.d(TAG, "currentUserId: $currentUserId")
        return view
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initialization()
        fetchNewsFeed()
    }

    private fun initialization() {
        mainViewModel = ViewModelProvider(mContext as FragmentActivity, ViewModelFactory()).get(MainViewModel::class.java)
        newsFeedRV.layoutManager = LinearLayoutManager(mContext)

        newsFeedRV.addOnScrollListener(object : RecyclerView.OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (isLastItemReached() && isDataAvailable) {
                    offset += limit
                    fetchNewsFeed()
                }
            }
        })

        swipe.setOnRefreshListener {
            offset = 0
            isFirstLoading = true
            fetchNewsFeed()
        }
    }

    private fun isLastItemReached(): Boolean {
        val layoutManager: LinearLayoutManager = newsFeedRV.layoutManager as LinearLayoutManager
        val position = layoutManager.findLastCompletelyVisibleItemPosition()
        val numberOfItems = postAdapter!!.itemCount
        Log.d(TAG, "position: $position numberOfItems: $numberOfItems")
        return (position >= numberOfItems - 1)
    }

/*    override fun onResume() {
        super.onResume()
        fetchNewsFeed()
    }*/

    private fun fetchNewsFeed() {
        (activity as MainActivity).showProgressBar()
//        currentUserId = FirebaseAuth.getInstance().uid
        val params = HashMap<String, String>()
        params.put("uid", currentUserId!!)
        params.put("limit", limit.toString())
        params.put("offset", offset.toString())

        mainViewModel.getNewsFeed(params)?.observe(viewLifecycleOwner, Observer { postResponse ->
            (activity as MainActivity).hideProgressBar()
            if (postResponse.status == 200) {
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
        })
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
        postAdapter?.updateCommentCount(position)
    }

    companion object {
        fun getInstance() = NewsFeedFragment()
    }
}