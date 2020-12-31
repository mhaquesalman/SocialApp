package com.salman.socialapp.adapters

import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.salman.socialapp.R
import com.salman.socialapp.model.Post
import com.salman.socialapp.model.User
import com.salman.socialapp.network.BASE_URL
import com.salman.socialapp.util.Utils
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.item_post.view.*
import kotlinx.android.synthetic.main.item_search.view.*

private const val TAG = "PostAdapter"
class PostAdapter(val context: Context, val postItems: MutableList<Post>) : 
    RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    private var postList: ArrayList<Post> = ArrayList()
    val posts: ArrayList<Post>
        get() = postList


    fun updateList(list: List<Post>) {
        postList.clear()
        postList.addAll(list)
        notifyDataSetChanged()
    }

    fun getPostList() = postList

    fun itemRangeInserted(positionStart: Int, itemCount: Int) {
        notifyItemRangeInserted(positionStart, itemCount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = postItems.get(position)

        holder.peopleName.text = post.name
        holder.date.text = Utils.formatDate(post.statusTime)

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
        Log.d(TAG, "userImage: $userImage")
        if (!userImage!!.isEmpty()) {
            Glide.with(context)
                .load(userImage)
                .placeholder(R.drawable.default_profile_placeholder)
                .into(holder.peopleImage)
        }

        holder.postTitle.text = post.post

        val postUri = Uri.parse(post.statusImage)
        val postImage = if (postUri.authority == null) {
             BASE_URL + post.statusImage
        } else {
            post.statusImage
        }

        Log.d(TAG, "postImage: $postImage")
        if (!postImage!!.isEmpty()) {
            holder.postImage.visibility = View.VISIBLE
            Glide.with(context)
                .load(postImage)
                .into(holder.postImage)
        }
    }

    override fun getItemCount(): Int {
        return postItems.size
    }

    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val peopleName: TextView  = itemView.people_name
        val peopleImage: CircleImageView = itemView.people_image
        val date: TextView = itemView.date
        val privacyIcon: ImageView = itemView.privacy_icon
        val postTitle: TextView = itemView.post_title
        val postImage: ImageView = itemView.post_image
    }
}