package com.salman.socialapp.adapters

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.salman.socialapp.R
import com.salman.socialapp.model.Chat
import com.salman.socialapp.network.BASE_URL
import com.salman.socialapp.util.Converter
import de.hdodenhof.circleimageview.CircleImageView

class MessageAdapter(
    val context: Context,
    val chatList: List<Chat>,
    val currentUserId: String,
    val userImage: String
) : RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    companion object {
        const val MSG_TYPE_RIGHT = 0
        const val MSG_TYPE_LEFT = 1
    }

    override fun getItemViewType(position: Int): Int {
        if (chatList[position].sender.equals(currentUserId)) {
            return MSG_TYPE_RIGHT
        } else {
            return MSG_TYPE_LEFT
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        if (viewType == MSG_TYPE_RIGHT) {
            val view = LayoutInflater.from(context).inflate(R.layout.item_chat_right, parent, false)
            return MessageViewHolder(view)
        } else {
            val view = LayoutInflater.from(context).inflate(R.layout.item_chat_left, parent, false)
            return MessageViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
       val chat = chatList[position]

        if (chat.image) {
            holder.hideTextMsg()
            holder.showImageMsg()
            holder.messageImage.setImageBitmap(Converter.StringBase64ToBitmap(chat.message!!))
        } else {
            holder.hideImageMsg()
            holder.showTextMsg()
            holder.messageText.text = chat.message
        }

        val profileImage = if (Uri.parse(userImage).authority == null && !userImage.isEmpty()) {
            BASE_URL.plus(userImage)
        } else {
            userImage
        }
        Glide.with(context).load(profileImage).into(holder.userProfileImage)


        if (position == (chatList.size - 1)) {
            if (chat.image) {
                if (chat.seen && chatList[position].sender.equals(currentUserId)) {
                    holder.seenImgStatus.text = "Seen"
                } else if (chatList[position].sender.equals(currentUserId)) {
                    holder.seenImgStatus.text = "Delivered"
                } else {
                    holder.seenImgStatus.visibility = View.GONE
                }
            } else {
                if (chat.seen && chatList[position].sender.equals(currentUserId)) {
                    holder.seenStatus.text = "Seen"
                } else if (chatList[position].sender.equals(currentUserId)) {
                    holder.seenStatus.text = "Delivered"
                } else {
                    holder.seenStatus.visibility = View.GONE
                }
            }
        } else {
            holder.seenStatus.visibility = View.GONE
            holder.seenImgStatus.visibility = View.GONE
        }

    }

    override fun getItemCount(): Int {
        return chatList.size
    }

    fun getChatAt(position: Int): Chat {
        return chatList[position]
    }

    private fun imageStringToUri(message: String?): Uri? {
        val imageUri = Uri.parse(message)
        return imageUri
    }

    fun isImageSent(img: Boolean) {

    }

    inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageText: TextView = itemView.findViewById(R.id.show_message)
        val userProfileImage: CircleImageView = itemView.findViewById(R.id.profile_image)
        val seenStatus: TextView = itemView.findViewById(R.id.seen_status)
        val messageImage: ImageView = itemView.findViewById(R.id.chat_img)
        val seenImgStatus: TextView = itemView.findViewById(R.id.seen_img_status)

        fun showTextMsg() {
            messageText.visibility = View.VISIBLE
            seenStatus.visibility = View.VISIBLE
        }

        fun showImageMsg() {
            messageImage.visibility = View.VISIBLE
            seenImgStatus.visibility = View.VISIBLE
        }

        fun hideTextMsg() {
            messageText.visibility = View.GONE
            seenStatus.visibility = View.GONE
        }

        fun hideImageMsg() {
            messageImage.visibility = View.GONE
            seenImgStatus.visibility = View.GONE
        }
    }
}