package com.salman.socialapp.adapters

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.salman.socialapp.R
import com.salman.socialapp.model.FirebaseUserInfo
import com.salman.socialapp.network.BASE_URL
import com.salman.socialapp.ui.activities.SendMessageActivity
import com.salman.socialapp.util.showToast
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.item_firebase_user.view.*
import kotlinx.android.synthetic.main.item_story_view.view.*

class StoryWatcherAdapter(
    val context: Context,
    val firebaseUserList: MutableList<FirebaseUserInfo>
) : RecyclerView.Adapter<StoryWatcherAdapter.RecentChatViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentChatViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_story_view, parent, false)
        return RecentChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecentChatViewHolder, position: Int) {
        val user = firebaseUserList[position]

        holder.userName.text = user.name

        val profileImage = if (Uri.parse(user.image).authority == null && !user.image.isEmpty()) {
            BASE_URL.plus(user.image)
        } else {
            user.image
        }

        Glide.with(context)
            .load(profileImage)
            .placeholder(R.drawable.default_profile_placeholder)
            .into(holder.userImage)

    }

    override fun getItemCount(): Int {
        return firebaseUserList.size
    }

    inner class RecentChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userName: TextView = itemView.profile_title
        val userImage: CircleImageView = itemView.profile_image

    }

}