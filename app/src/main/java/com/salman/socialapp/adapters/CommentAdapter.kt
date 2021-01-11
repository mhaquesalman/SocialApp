package com.salman.socialapp.adapters

import android.content.Context
import android.net.Uri
import android.opengl.Visibility
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.salman.socialapp.R
import com.salman.socialapp.model.Comment
import com.salman.socialapp.network.BASE_URL
import com.salman.socialapp.util.AgoDateParser
import com.salman.socialapp.util.CommentRepliesBottomDialog
import com.salman.socialapp.util.CommentsBottomDialog
import com.salman.socialapp.util.IOnCommentRepliesAdded
import kotlinx.android.synthetic.main.item_comment.view.*
import java.text.ParseException

class CommentAdapter(val context: Context, val commentItems: MutableList<Comment>,
val iOnCommentRepliesAdded: IOnCommentRepliesAdded, val postAdapterPosition: Int, val userId: String) :
    RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_comment, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = commentItems.get(position)

        // parent comment section
        holder.commentProfileName.text = comment.name
        holder.commentBody.text = comment.comment
        val profileImageUri = Uri.parse(comment.profileUrl)
        val profileImage = if (profileImageUri.authority == null) {
            BASE_URL + comment.profileUrl
        } else {
            comment.profileUrl
        }
        if (!profileImage!!.isEmpty()) {
            Glide.with(context)
                .load(profileImage)
                .into(holder.commentProfileImage)
        }

        holder.commentDate.setText(AgoDateParser.getTimeAgo(comment.commentDate))

        if (comment.commentOn.equals("post")) {
            holder.commentReply.visibility = View.VISIBLE
        } else {
            holder.commentReply.visibility = View.GONE
        }

        holder.commentReply.setOnClickListener {
            openCommentReplyBottomDialog(true, comment, postAdapterPosition, position, userId)
        }

        // sub comment section
        val totalCommentReplies = comment.totalCommentReplies
        if (totalCommentReplies >= 1) {
            holder.subCommentSection.visibility = View.VISIBLE
            if (totalCommentReplies > 1) {
                holder.moreComments.visibility = View.VISIBLE
            } else {
                holder.moreComments.visibility = View.GONE
            }
            if (totalCommentReplies == 2) {
                holder.moreComments.text = "view 1 more comment"
            } else {
                holder.moreComments.text = "view $totalCommentReplies more comments"
            }

            holder.moreComments.setOnClickListener {
                openCommentReplyBottomDialog(false, comment, postAdapterPosition, position, userId)
            }

            if (comment.comments.size >= 1) {
                val latestComment = comment.comments.get(0)
                holder.subCommentProfileName.text = latestComment.name
                holder.subCommentBody.text = latestComment.comment
                val subProfileImageUri = Uri.parse(latestComment.profileUrl)
                val subProfileImage = if (subProfileImageUri.authority == null) {
                    BASE_URL + latestComment.profileUrl
                } else {
                    latestComment.profileUrl
                }

                if (!subProfileImage!!.isEmpty()) {
                    Glide.with(context)
                        .load(subProfileImage)
                        .into(holder.subCommentProfileImge)
                }

                holder.subCommentDate.setText(AgoDateParser.getTimeAgo(comment.commentDate))

            }
        } else {
            holder.subCommentSection.visibility = View.GONE
        }

    }

    override fun getItemCount(): Int {
        return commentItems.size
    }

    private fun openCommentReplyBottomDialog(
        openKeyboard: Boolean,
        comment: Comment,
        postAdapterPosition: Int,
        mAdapterPosition: Int,
        userId: String) {
        val bundle = Bundle().apply {
            putString("userId", userId)
            putString("postId", comment.commentPostId)
            putString("postUserId", comment.postUserId)
            putString("commentOn", "comment")
            putString("commentUserId", comment.commentBy)
            putString("parentId", comment.cid)
            putInt("commentCount", -1)
            putBoolean("openKeyboard", openKeyboard)
            putInt("postAdapterPosition", postAdapterPosition)
            putInt("adapterPosition", mAdapterPosition)
        }
        val commentRepliesBottomDialog = CommentRepliesBottomDialog(iOnCommentRepliesAdded)
        commentRepliesBottomDialog.arguments = bundle
        val fragmentActivity = context as FragmentActivity
        commentRepliesBottomDialog.show(fragmentActivity.supportFragmentManager, "commentReplyFragmentDialog")
    }

    inner class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val commentProfileImage: ImageView = itemView.comment_profile_image
        val commentProfileName: TextView = itemView.comment_profile_name
        val commentBody: TextView = itemView.comment_body
        val commentDate: TextView = itemView.comment_date
        val commentReply: TextView = itemView.comment_reply

        // sub comment section
        val moreComments: TextView = itemView.more_comments
        val subCommentSection: LinearLayout = itemView.subComment_section
        val subCommentProfileImge: ImageView = itemView.subComment_profile_image
        val subCommentProfileName: TextView = itemView.subComment_profile_name
        val subCommentBody: TextView = itemView.subComment_body
        val subCommentDate: TextView = itemView.subComment_date

    }
}