package com.salman.socialapp.local

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.amrdeveloper.reactbutton.FbReactions
import com.amrdeveloper.reactbutton.ReactButton
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.salman.socialapp.R
import com.salman.socialapp.model.Post
import com.salman.socialapp.model.Reaction
import com.salman.socialapp.network.BASE_URL
import com.salman.socialapp.ui.activities.PostDetailActivity
import com.salman.socialapp.util.CommentsBottomDialog
import com.salman.socialapp.util.Utils
import com.salman.socialapp.util.showToast
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.click_post_bottom_sheet.view.*
import kotlinx.android.synthetic.main.item_post.view.*

private const val TAG = "ProfilePostAdapter"

class ProfilePostAdapter(
    val context: Context,
    val postItems: MutableList<ProfilePost>,
    val userId: String = "",
    val isPostEditable: Boolean = false
) : RecyclerView.Adapter<ProfilePostAdapter.PostViewHolder>() {

    var totalReaction = 0

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
                .error(R.drawable.try_later)
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
            totalReaction =
                likeCount + loveCount + hahaCount + wowCount + angryCount + sadCount + careCount
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
            context.showToast("No Internet Connection !", Toast.LENGTH_SHORT)
        }

        val totalComment = post.commentCount
        when (totalComment) {
            0 -> holder.commentCount.text = "Comment"
            1 -> holder.commentCount.text = "$totalComment Comment"
            else -> holder.commentCount.text = "$totalComment Comments"
        }

        holder.itemView.setOnClickListener {
            val mIntent = Intent(context, PostDetailActivity::class.java).apply {
                putExtra("post", Gson().toJson(post))
                putExtra("loadFromApi", false)
                putExtra("position", holder.adapterPosition)
            }
            context.startActivity(mIntent)
        }

    }

    override fun getItemCount(): Int {
        Log.d(TAG, "getItemCount: ${postItems.size}")
        return postItems.size
    }

    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val peopleName: TextView = itemView.people_name
        val peopleImage: CircleImageView = itemView.people_image
        val date: TextView = itemView.date
        val privacyIcon: ImageView = itemView.privacy_icon
        val postTitle: TextView = itemView.post_title
        val postImage: ImageView = itemView.post_image
        val reactionButton: ReactButton = itemView.reaction
        val reactionCount: TextView = itemView.total_reactionTV
        val commentCount: TextView = itemView.comment_countTV
        val commentSection: LinearLayout = itemView.commentSection
        val moreButton: ImageView = itemView.more_btn

        init {
            reactionButton.setReactClickListener { v ->
                context.showToast("No Internet Connection !", Toast.LENGTH_SHORT)
            }

            reactionButton.setReactDismissListener { v ->
                context.showToast("No Internet Connection !", Toast.LENGTH_SHORT)
                false
            }

            if (isPostEditable) {
                moreButton.visibility = View.VISIBLE
                moreButton.setOnClickListener {
                    context.showToast("No Internet Connection !", Toast.LENGTH_SHORT)
                }
            } else {
                moreButton.visibility = View.INVISIBLE
            }
        }

    }
}