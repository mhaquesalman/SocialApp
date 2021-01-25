package com.salman.socialapp.adapters

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.salman.socialapp.R
import com.salman.socialapp.model.Notification
import com.salman.socialapp.network.BASE_URL
import com.salman.socialapp.ui.activities.PostDetailActivity
import com.salman.socialapp.ui.activities.ProfileActivity
import com.salman.socialapp.util.AgoDateParser
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.item_notification.view.*
import java.text.ParseException

class NotificationAdapter(
    val context: Context,
    val notificationList: List<Notification>
) : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_notification, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notification = notificationList.get(position)

        when (notification.type) {
            "post-reaction" -> holder.notficationTitle.setText(notification.name + " reacted on your post")
            "post-comment" -> holder.notficationTitle.setText(notification.name + " commented on your post")
            "comment-reply" -> holder.notficationTitle.setText(notification.name + " replied on your comment")
            "friend-request" -> holder.notficationTitle.setText(notification.name + " send you friend request")
            "request-accepted" -> holder.notficationTitle.setText(notification.name + " accpeted your friend request")
            else -> holder.notficationTitle.setText("Unknown")
        }

        if (notification.type.equals("friend-request") || notification.type.equals("request-accpeted")) {
            holder.notficationBody.visibility = View.GONE
            holder.itemView.setOnClickListener {
                val mIntent = Intent(context, ProfileActivity::class.java)
                    .putExtra("uid", notification.notificationFrom)
                context.startActivity(mIntent)
            }
        } else {
            if (notification.post != null && !notification.post.isEmpty()) {
                holder.notficationBody.visibility = View.VISIBLE
                holder.notficationBody.text = notification.post
            }

            holder.itemView.setOnClickListener {
                val mIntent = Intent(context, PostDetailActivity::class.java)
                    .putExtra("loadFromApi", true)
                    .putExtra("postId", notification.postId)
                context.startActivity(mIntent)
            }
        }

        val userUri = Uri.parse(notification.profileUrl)
        val userImage = if (userUri.authority == null && !notification.profileUrl!!.isEmpty()) {
            BASE_URL + notification.profileUrl
        } else {
            notification.profileUrl
        }
        if (!userImage!!.isEmpty()) {
            Glide.with(context)
                .load(userImage)
                .into(holder.profileImage)
        }

        try {
            holder.notficationDate.text = AgoDateParser.getTimeAgo(notification.notificationTime)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
    }

    override fun getItemCount(): Int {
        return notificationList.size
    }

    inner class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profileImage: CircleImageView = itemView.profile_image
        val notficationTitle: TextView = itemView.title
        val notficationBody: TextView = itemView.body
        val notficationDate: TextView = itemView.date
    }
}