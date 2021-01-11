package com.salman.socialapp.adapters

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.amrdeveloper.reactbutton.FbReactions
import com.amrdeveloper.reactbutton.ReactButton
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.salman.socialapp.R
import com.salman.socialapp.model.Post
import com.salman.socialapp.model.Reaction
import com.salman.socialapp.model.User
import com.salman.socialapp.network.BASE_URL
import com.salman.socialapp.util.AgoDateParser
import com.salman.socialapp.util.CommentsBottomDialog
import com.salman.socialapp.util.Utils
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.item_post.view.*
import kotlinx.android.synthetic.main.item_search.view.*

private const val TAG = "PostAdapter"
class PostAdapter(val context: Context, val postItems: MutableList<Post>, val userId: String = "") :
    RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    var totalReaction = 0
    val iUpdateUserReaction: IUpdateUserReaction

    init {
        if (context is IUpdateUserReaction) {
            iUpdateUserReaction = context
        } else {
            throw RuntimeException("$context must be implemented")
        }
    }

/*    private var postList: ArrayList<Post> = ArrayList()
    val posts: ArrayList<Post>
        get() = postList

    fun updateList(list: List<Post>) {
        postList.clear()
        postList.addAll(list)
        notifyDataSetChanged()
    }

    fun getPostList() = postList */

    fun itemRangeInserted(positionStart: Int, itemCount: Int) {
        notifyItemRangeInserted(positionStart, itemCount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = postItems.get(position)
        Log.d(TAG, "post: $post")

        holder.peopleName.text = post.name
        holder.date.text = Utils.formatDate(post.statusTime)
        
//        holder.date.setText(AgoDateParser.getTimeAgo(post.statusTime))

        holder.reactionButton.isEnabled = true

        if (post.privacy == "0") {
            holder.privacyIcon.setImageResource(R.drawable.ic_friends)
        } else if (post.privacy == "1") {
            holder.privacyIcon.setImageResource(R.drawable.ic_only_me)
        } else if (post.privacy == "2") {
            holder.privacyIcon.setImageResource(R.drawable.ic_public)
        }

        val userUri = Uri.parse(post.profileUrl)
        val userImage = if (userUri.authority == null) {
            BASE_URL + post.profileUrl
        } else {
            post.profileUrl
        }
//        Log.d(TAG, "userImage: $userImage")
        if (!userImage!!.isEmpty()) {
            Glide.with(context)
                .load(userImage)
                .into(holder.peopleImage)
        }

        holder.postTitle.text = post.post

        val postUri = Uri.parse(post.statusImage)
        val postImage = if (postUri.authority == null && !post.statusImage!!.isEmpty()) {
             BASE_URL + post.statusImage
        } else {
            post.statusImage
        }
//        Log.d(TAG, "statusImage: $postImage")
//        Log.d(TAG, "${post.postId}: ${post.statusImage}")
        if (!postImage!!.isEmpty()) {
            holder.postImage.visibility = View.VISIBLE
            Glide.with(context)
                .load(postImage)
                .placeholder(R.drawable.progress_animation)
                .error(R.drawable.try_later)
                .into(holder.postImage)
        } else {
            holder.postImage.visibility = View.GONE
        }

        post.apply {
            totalReaction =  likeCount + loveCount + hahaCount + wowCount + angryCount + sadCount + careCount
            Log.d(TAG, "totalReaction: ${post.postId} = $totalReaction")
        }
        when (totalReaction) {
            0 -> {
                holder.reactionCount.visibility = View.INVISIBLE
            }
            1 -> {
                holder.reactionCount.visibility = View.VISIBLE
                holder.reactionCount.setText("$totalReaction Reaction")
            }
            else -> {
                holder.reactionCount.visibility = View.VISIBLE
                holder.reactionCount.setText("$totalReaction Reactions")
            }
        }

        holder.reactionButton.currentReaction = FbReactions.getReaction(post.reactionType)
        Log.d(TAG, "ReactionCurrent: ${post.reactionType}")

        holder.commentSection.setOnClickListener {
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
            val fragmentActivity = context as FragmentActivity
            commentsBottomDialog.show(fragmentActivity.supportFragmentManager, "commentFragmentDialog")
        }

        val totalComment = post.commentCount
        when (totalComment) {
            0 -> holder.commentCount.text = "Comment"
            1 -> holder.commentCount.text = "$totalComment Comment"
            else -> holder.commentCount.text = "$totalComment Comments"
        }


    }

    override fun getItemCount(): Int {
        Log.d(TAG, "getItemCount: ${postItems.size}")
        return postItems.size
    }

    fun updateCommentCount(position: Int) {
        postItems.get(position).apply {
            commentCount += 1
            notifyItemChanged(position, this)
        }
    }

    fun updatePostAfterReaction(position: Int, reaction: Reaction) {
        postItems.get(position).apply {
            likeCount = reaction.likeCount
            loveCount = reaction.loveCount
            careCount = reaction.careCount
            hahaCount = reaction.hahaCount
            wowCount = reaction.wowCount
            sadCount = reaction.sadCount
            angryCount = reaction.angryCount
            reactionType = reaction.reactionType

            postItems.set(position, this)
            notifyItemChanged(position, this)
        }
    }

    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val peopleName: TextView  = itemView.people_name
        val peopleImage: CircleImageView = itemView.people_image
        val date: TextView = itemView.date
        val privacyIcon: ImageView = itemView.privacy_icon
        val postTitle: TextView = itemView.post_title
        val postImage: ImageView = itemView.post_image
        val reactionButton: ReactButton = itemView.reaction
        val reactionCount: TextView = itemView.total_reactionTV
        val commentCount: TextView = itemView.comment_countTV
        val commentSection: LinearLayout = itemView.commentSection

        init {
            reactionButton.setReactClickListener(object : View.OnClickListener {
                override fun onClick(v: View?) {
                    Log.d(TAG, "setReactClickListener called")
                    onReactionChanged(v)
                }
            })

            reactionButton.setReactDismissListener(object : View.OnLongClickListener {
                override fun onLongClick(v: View?): Boolean {
                    Log.d(TAG, "setReactDismissListener called")
                    onReactionChanged(v)
                    return false
                }
            })
        }

        private fun onReactionChanged(v: View?) {
            Log.d(TAG, "onReactionChanged called")
            val previousReactionType = postItems.get(adapterPosition).reactionType
            val newReactionType = (v as ReactButton).currentReaction.reactType
            v.isEnabled = false

            Log.d(TAG, "Reaction: $previousReactionType / $newReactionType")
            if (!previousReactionType!!.contentEquals(newReactionType)) {
                iUpdateUserReaction.updateUserReaction(
                    userId,
                    postItems.get(adapterPosition).postId,
                    postItems.get(adapterPosition).postUserId!!,
                    previousReactionType,
                    newReactionType,
                    adapterPosition
                )
            }
        }

    }

    interface IUpdateUserReaction {
        fun updateUserReaction(
            uId: String,
            postId: Int,
            postOwnerId: String,
            previousReactionType: String,
            newReactionType: String,
            position: Int
        )
    }
}