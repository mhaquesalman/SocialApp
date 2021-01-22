package com.salman.socialapp.ui.activities

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.amrdeveloper.reactbutton.FbReactions
import com.amrdeveloper.reactbutton.ReactButton
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.salman.socialapp.R
import com.salman.socialapp.model.PerformReaction
import com.salman.socialapp.model.Post
import com.salman.socialapp.network.BASE_URL
import com.salman.socialapp.util.CommentsBottomDialog
import com.salman.socialapp.util.IOnCommentAdded
import com.salman.socialapp.util.Utils
import com.salman.socialapp.util.showToast
import com.salman.socialapp.viewmodels.PostDetailViewModel
import com.salman.socialapp.viewmodels.ViewModelFactory
import kotlinx.android.synthetic.main.activity_post_detail.*

class PostDetailActivity : AppCompatActivity(), IOnCommentAdded {
    var position = 0
    var previousReactionType = ""
    var newReactionType = ""
    var totalReaction = 0
    var totalComment = 0
    var userId: String? = ""
    lateinit var post: Post
    lateinit var postDetailViewModel: PostDetailViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_detail)

        setSupportActionBar(toolbar)
        toolbar.setNavigationIcon(R.drawable.ic_back)
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        postDetailViewModel = ViewModelProvider(this, ViewModelFactory()).get(PostDetailViewModel::class.java)
        with(Utils(this)){
            userId = getUserFromSharedPref()?.uid
        }

        intent?.let {
            if (it.getBooleanExtra("loadFromApi", false)) {
                
            } else {
                scrollView.visibility = View.VISIBLE
                position = it.getIntExtra("position", 0)
                post = Gson().fromJson<Post>(it.getStringExtra("post"), Post::class.java)
                showPostDetail()
            }
        }
    }

    private fun showPostDetail() {
        people_name.text = post.name
        date.text = Utils.formatDate(post.statusTime)

//        date.setText(AgoDateParser.getTimeAgo(post.statusTime))

        reaction.isEnabled = true

        if (post.privacy == "0") {
            privacy_icon.setImageResource(R.drawable.ic_friends)
        } else if (post.privacy == "1") {
            privacy_icon.setImageResource(R.drawable.ic_only_me)
        } else if (post.privacy == "2") {
            privacy_icon.setImageResource(R.drawable.ic_public)
        }

        val userUri = Uri.parse(post.profileUrl)
        val userImage = if (userUri.authority == null) {
            BASE_URL + post.profileUrl
        } else {
            post.profileUrl
        }
//        Log.d(TAG, "userImage: $userImage")
        if (!userImage!!.isEmpty()) {
            Glide.with(this)
                .load(userImage)
                .into(people_image)
        }

        post_title.text = post.post

        val postUri = Uri.parse(post.statusImage)
        val postImage = if (postUri.authority == null && !post.statusImage!!.isEmpty()) {
            BASE_URL + post.statusImage
        } else {
            post.statusImage
        }

        if (!postImage!!.isEmpty()) {
            post_image.visibility = View.VISIBLE
            Glide.with(this)
                .load(postImage)
                .placeholder(R.drawable.progress_animation)
                .error(R.drawable.try_later)
                .into(post_image)
        } else {
            post_image.visibility = View.GONE
        }

        post.apply {
            totalReaction = likeCount + loveCount + hahaCount + wowCount + angryCount + sadCount + careCount
        }
        when (totalReaction) {
            0 -> {
                total_reactionTV.visibility = View.INVISIBLE
            }
            1 -> {
                total_reactionTV.visibility = View.VISIBLE
                total_reactionTV.setText("$totalReaction Reaction")
            }
            else -> {
                total_reactionTV.visibility = View.VISIBLE
                total_reactionTV.setText("$totalReaction Reactions")
            }
        }

        reaction.currentReaction = FbReactions.getReaction(post.reactionType)

        reaction.setReactClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                onReactionChanged(v as ReactButton)
            }
        })

        reaction.setReactDismissListener(object : View.OnLongClickListener {
            override fun onLongClick(v: View): Boolean {
                onReactionChanged(v as ReactButton)
                return false
            }
        })

        commentSection.setOnClickListener {
            val bundle = Bundle().apply {
                putString("userId", userId)
                putString("postId", post.postId.toString())
                putString("postUserId", post.postUserId)
                putString("commentOn", "post")
                putString("commentUserId", "-1")
                putString("parentId", "-1")
                putInt("commentCount", post.commentCount)
                putBoolean("openKeyboard", false)
                putInt("postAdapterPosition", position)
            }
            val commentsBottomDialog = CommentsBottomDialog()
            commentsBottomDialog.arguments = bundle
            val fragmentActivity = this as FragmentActivity
            commentsBottomDialog.show(fragmentActivity.supportFragmentManager, "commentFragmentDialog")
        }

        totalComment = post.commentCount
        when (totalComment) {
            0 -> comment_countTV.text = "Comment"
            1 -> comment_countTV.text = "$totalComment Comment"
            else -> comment_countTV.text = "$totalComment Comments"
        }
        
    }

    private fun onReactionChanged(v: ReactButton) {
        previousReactionType = post.reactionType!!
        newReactionType = v.currentReaction.reactType
        if (!previousReactionType.equals(newReactionType)) {
            v.isEnabled = false
            // calling API
            val performReaction = PerformReaction(
                userId,
                post.postId.toString(),
                post.postUserId,
                previousReactionType,
                newReactionType
            )
            postDetailViewModel.performReaction(performReaction)?.observe(this, Observer { reactionResponse ->
                v.isEnabled = true
                if (reactionResponse.status == 200) {
                    post.reactionType = reactionResponse.reaction?.reactionType
                    reactionResponse.reaction!!.apply {
                        totalReaction = likeCount + loveCount + hahaCount + wowCount + angryCount + sadCount + careCount
                    }
                    when (totalReaction) {
                        0 -> {
                            total_reactionTV.visibility = View.INVISIBLE
                        }
                        1 -> {
                            total_reactionTV.visibility = View.VISIBLE
                            total_reactionTV.setText("$totalReaction Reaction")
                        }
                        else -> {
                            total_reactionTV.visibility = View.VISIBLE
                            total_reactionTV.setText("$totalReaction Reactions")
                        }
                    }
                } else {
                    showToast(reactionResponse.message)
                }
            })
        }
    }

    override fun onCommentAdded(position: Int) {
        totalComment += 1
        post.commentCount = totalComment
        when (totalComment) {
            0 -> comment_countTV.setText("Comment")
            1 -> comment_countTV.setText(totalComment.toString() + " Comment")
            else -> comment_countTV.setText(totalComment.toString() + " Comments")
        }
    }
}