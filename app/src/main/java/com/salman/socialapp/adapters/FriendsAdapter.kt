package com.salman.socialapp.adapters

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.salman.socialapp.R
import com.salman.socialapp.model.Friend
import com.salman.socialapp.network.BASE_URL
import com.salman.socialapp.ui.activities.ProfileActivity
import com.salman.socialapp.ui.activities.USER_ID
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.item_friend.view.*

class FriendsAdapter(val context: Context) : RecyclerView.Adapter<FriendsAdapter.FriendsViewHolder>() {

    private var friendList: ArrayList<Friend> = ArrayList()

    fun updateList(list: List<Friend>) {
        friendList.clear()
        friendList.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_friend, parent, false)
        return FriendsViewHolder(view)
    }

    override fun onBindViewHolder(holder: FriendsViewHolder, position: Int) {
        val friend = friendList.get(position)

        holder.profileTitle.text = friend.name

        val userUri = Uri.parse(friend.profileUrl)
        val userImage = if (userUri.authority == null) {
            BASE_URL + friend.profileUrl
        } else {
            friend.profileUrl
        }
        if (!userImage!!.isEmpty()) {
            Glide.with(context)
                .load(userImage)
                .placeholder(R.drawable.default_profile_placeholder)
                .into(holder.profileImage)
        }

        holder.item.setOnClickListener {
            context.startActivity(Intent(context, ProfileActivity::class.java)
                .putExtra(USER_ID, friendList.get(holder.adapterPosition).uid))
        }
    }

    override fun getItemCount(): Int {
        return friendList.size
    }

    inner class FriendsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profileTitle: TextView = itemView.profile_title
        val profileImage: CircleImageView = itemView.profile_image
        val acceptButton: Button = itemView.accept_btn
        val cancelButton: Button = itemView.cancel_btn
        val item: View = itemView

        init {
            acceptButton.visibility = View.INVISIBLE
            cancelButton.visibility = View.INVISIBLE
        }
    }

}