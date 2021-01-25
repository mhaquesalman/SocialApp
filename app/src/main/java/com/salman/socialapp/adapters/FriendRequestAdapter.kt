package com.salman.socialapp.adapters

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.salman.socialapp.R
import com.salman.socialapp.model.Request
import com.salman.socialapp.network.BASE_URL
import com.salman.socialapp.ui.activities.ProfileActivity
import com.salman.socialapp.ui.activities.USER_ID
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.item_friend.view.*

const val OPERATION_TYPE_ACCEPT = 3
const val OPERATION_TYPE_CANCEL = 2
class FriendRequestAdapter(val context: Context) : RecyclerView.Adapter<FriendRequestAdapter.FriendsViewHolder>() {

    private var requestList: ArrayList<Request> = ArrayList()
    val listener: OnClickPerformAction
//    lateinit var listener: OnClickPerformAction

   init {
        listener = context as OnClickPerformAction
    }

    fun updateList(list: List<Request>) {
        requestList.clear()
        requestList.addAll(list)
        notifyDataSetChanged()
    }

/*    fun setOnClickPerformAction(listener: OnClickPerformAction) {
       this.listener = listener
    }*/

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_friend, parent, false)
        return FriendsViewHolder(view)
    }

    override fun onBindViewHolder(holder: FriendsViewHolder, position: Int) {
        val request = requestList.get(position)
        val adapterPosition = holder.adapterPosition

        holder.profileTitle.text = request.name

        val userUri = Uri.parse(request.profileUrl)
        val userImage = if (userUri.authority == null) {
            BASE_URL + request.profileUrl
        } else {
            request.profileUrl
        }
        if (!userImage!!.isEmpty()) {
            Glide.with(context)
                .load(userImage)
                .placeholder(R.drawable.default_profile_placeholder)
                .into(holder.profileImage)
        }

        holder.acceptButton.setOnClickListener {
            listener.performAction(
                requestList.indexOf(requestList.get(adapterPosition)),
                requestList.get(adapterPosition).uid!!,
                OPERATION_TYPE_ACCEPT
            )
            holder.cancelButton.isEnabled = false
        }

        holder.cancelButton.setOnClickListener{
            listener.performAction(
                requestList.indexOf(requestList.get(adapterPosition)),
                requestList.get(adapterPosition).uid!!,
                OPERATION_TYPE_CANCEL
            )
            holder.acceptButton.isEnabled = false
        }

        holder.item.setOnClickListener {
            context.startActivity(Intent(context, ProfileActivity::class.java)
                .putExtra(USER_ID, requestList.get(adapterPosition).uid))
        }

    }

    override fun getItemCount(): Int {
        return requestList.size
    }

      inner class FriendsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
          val profileTitle: TextView = itemView.profile_title
          val profileImage: CircleImageView = itemView.profile_image
          val acceptButton: Button = itemView.accept_btn
          val cancelButton: Button = itemView.cancel_btn
          val onlineStatus: TextView = itemView.online_status
          val item: View = itemView

          init {
              onlineStatus.visibility = View.GONE
          }
    }

    interface OnClickPerformAction {
        fun performAction(position: Int, profileId: String, operationType: Int)
    }
}