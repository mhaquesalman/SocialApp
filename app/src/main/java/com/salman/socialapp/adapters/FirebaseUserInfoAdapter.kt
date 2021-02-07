package com.salman.socialapp.adapters

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.view.marginTop
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.salman.socialapp.R
import com.salman.socialapp.model.FirebaseUserInfo
import com.salman.socialapp.network.BASE_URL
import com.salman.socialapp.ui.activities.SendMessageActivity
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.item_firebase_user.view.*

class FirebaseUserInfoAdapter(
    val context: Context,
    val recentChatUserList: MutableList<FirebaseUserInfo>,
    val onlineStatus: Boolean = false
) : RecyclerView.Adapter<FirebaseUserInfoAdapter.RecentChatViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentChatViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_firebase_user, parent, false)
        return RecentChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecentChatViewHolder, position: Int) {
        val user = recentChatUserList.get(position)

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


            if (onlineStatus) {
                if (user.status.equals("online")) {
                    holder.onlineStatus.visibility = View.VISIBLE
                    holder.onlineStatus.text = "online"
                    holder.onlineStatus.setTextColor(context.resources.getColor(R.color.green))
                }
                else {
                    holder.onlineStatus.visibility = View.VISIBLE
                    holder.onlineStatus.text = "offline"
                    holder.onlineStatus.setTextColor(context.resources.getColor(R.color.red))
                }
            } else {
                holder.onlineStatus.visibility = View.GONE

/*                val params: RelativeLayout.LayoutParams =
                    RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.MATCH_PARENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT
                    )
                params.setMargins(0, 15, 0, 0)
                holder.userName.layoutParams = params*/
            }



        holder.item.setOnClickListener {
            val mIntent = Intent(context, SendMessageActivity::class.java)
            mIntent.also {
                it.putExtra("userId", user.id)
                it.putExtra("name", user.name)
                it.putExtra("profileUrl", user.image)
            }
            context.startActivity(mIntent)
        }
    }

    override fun getItemCount(): Int {
        return recentChatUserList.size
    }

    inner class RecentChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userName: TextView = itemView.user_name
        val userImage: CircleImageView = itemView.user_image
        val onlineStatus: TextView = itemView.online_status
        val item: View = itemView
    }

}