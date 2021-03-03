package com.salman.socialapp.util

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.salman.socialapp.R
import com.salman.socialapp.adapters.CommentAdapter
import com.salman.socialapp.model.Comment
import com.salman.socialapp.model.PostComment
import com.salman.socialapp.ui.activities.MainActivity
import com.salman.socialapp.ui.activities.ProfileActivity
import com.salman.socialapp.viewmodels.CommentViewModel
import com.salman.socialapp.viewmodels.ViewModelFactory
import kotlinx.android.synthetic.main.bottom_dialog_comment.view.*

private const val TAG = "CommentsBottomDialog"
class CommentsBottomDialog : BottomSheetDialogFragment(),
    IOnCommentRepliesAdded,
    IOnCommentDelete,
    CommentRepliesBottomDialog.IOnCommentRepliesDelete {
    lateinit var commentCountTV: TextView
    lateinit var commentRecView: RecyclerView
    lateinit var commentET: EditText
    lateinit var commentSendBtn: RelativeLayout
    lateinit var progressBar: ProgressBar
    lateinit var bottomSheetBehavior: BottomSheetBehavior<FrameLayout>
    var bottomSheet: FrameLayout? = null

    lateinit var mContext: Context
    lateinit var commentViewModel: CommentViewModel
    var currentUserId: String? = ""
    var userId: String? = ""
    var postId: String? = null
    var postUserId: String? = null
    var commentOn: String? = null
    var commentUserId: String? = null
    var parentId: String? = null
    var openKeyboard = false
    var commentCount = 0
    var postAdapterPosition = 0
    lateinit var iOnCommentAdded: IOnCommentAdded
    lateinit var iOnCommentRepliesAdded: IOnCommentRepliesAdded

    var commentAdapter: CommentAdapter? = null
    var commentItems: MutableList<Comment> = ArrayList()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.mContext = context
        this.iOnCommentAdded = context as IOnCommentAdded
        this.iOnCommentRepliesAdded = this
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        commentViewModel = ViewModelProvider(this, ViewModelFactory()).get(CommentViewModel::class.java)

        if (activity is MainActivity) {
            this.currentUserId = (activity as MainActivity).currentUserId
            Log.d(TAG, "currentUserId: $currentUserId")
        } else if ( activity is ProfileActivity) {
            this.currentUserId = (activity as ProfileActivity).currentUserId
            Log.d(TAG, "currentUserId: $currentUserId")
        }

        arguments?.apply {
            userId = getString("userId")
            postId = getString("postId")
            postUserId = getString("postUserId")
            commentOn = getString("commentOn")
            commentUserId = getString("commentUserId")
            parentId = getString("parentId")
            commentCount = getInt("commentCount")
            openKeyboard = getBoolean("openKeyboard")
            postAdapterPosition = getInt("postAdapterPosition")
        }

        CommentRepliesBottomDialog.setIOnCommentRepliesDelete(this)

    }

    @SuppressLint("RestrictedApi")
    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style)
        val view = View.inflate(mContext, R.layout.bottom_dialog_comment, null)
        dialog.setContentView(view)

        commentCountTV = view.comments_count
        commentRecView = view.commentRV
        commentET = view.comment_txt
        commentSendBtn = view.comment_send
        progressBar = view.progress_bar

        when (commentCount) {
            0, 1 -> commentCountTV.text = "$commentCount Comment"
            else -> commentCountTV.text = "$commentCount Comments"
        }

        setupRecyclerView()
        getPostComments()

        dialog.setOnShowListener { dialogInterface ->
            val bottomSheetDialog = dialogInterface as BottomSheetDialog
            bottomSheet = bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.setBackgroundColor(Color.TRANSPARENT)
            bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet!!)
//            BottomSheetBehavior.from(bottomSheet!!).peekHeight = Resources.getSystem().getDisplayMetrics().heightPixels
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED

        }

        commentET.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) { /**/ }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { /**/ }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s!!.length >= 1) {
                    commentSendBtn.alpha = 1f
                    commentSendBtn.isEnabled = true
                } else {
                    commentSendBtn.alpha = 0.4f
                    commentSendBtn.isEnabled = false
                }
            }
        })

        commentSendBtn.setOnClickListener {
            val commentText = commentET.text.toString()
            if (commentText.isEmpty()) {
                mContext.showToast("please type your comment !", Toast.LENGTH_SHORT)

            } else {
                val inputMethodManager =
                    mContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(it.windowToken, 0)
                commentET.setText("")
                // post comment
                postComment(commentText)

            }
        }
    }

    private fun setupRecyclerView() {
        val linearLayoutManager = LinearLayoutManager(mContext)
        commentRecView.layoutManager = linearLayoutManager
    }

    private fun postComment(comment: String) {
        val postComment = PostComment(
            comment,
            userId,
            postId,
            postUserId,
            commentOn,
            commentUserId,
            parentId
        )

        commentViewModel.postComment(postComment)?.observe(this, Observer { commentResponse ->
            mContext.showToast(commentResponse.message)
            if (commentResponse.status == 200) {
                commentCount++
                if (commentCount == 1) {
                    commentCountTV.text = "$commentCount Comment"
                } else {
                    commentCountTV.text = "$commentCount Comments"
                }
                val postedComment = commentResponse.comments.get(0)
                commentItems.add(0, postedComment)
                commentAdapter?.notifyItemInserted(0)
                commentRecView.smoothScrollToPosition(0)
                // update post recyclerview after adding comment in a post
                iOnCommentAdded.onCommentAdded(postAdapterPosition)
            }
        })
    }

    private fun getPostComments() {
        progressBar.show()
        commentViewModel.getPostComments(postId!!, postUserId!!)?.observe(this, Observer { commentResponse ->
            progressBar.hide()
            if (commentResponse.status == 200) {
                commentItems.clear()
                commentItems.addAll(commentResponse.comments)
                commentAdapter = CommentAdapter(mContext, commentItems, iOnCommentRepliesAdded, postAdapterPosition, userId!!)
                commentRecView.adapter = commentAdapter
                commentAdapter?.setIOnCommentDelete(this)
            } else {
                mContext.showToast(commentResponse.message)
            }

        })
    }

    override fun oncommentRepliesAdded(mAdapterPosition: Int, comment: Comment) {
        val mComment = commentItems.get(mAdapterPosition)
        commentCount++
        if (commentCount == 1) {
            commentCountTV.setText("$commentCount Comment")
        } else {
            commentCountTV.setText("$commentCount Comments")
        }
        mComment.totalCommentReplies = mComment.totalCommentReplies + 1

        if (mComment.comments.size >= 1) {
            mComment.comments.set(0, comment)
        } else {
            val updatedComments = mComment.comments
            updatedComments.add(0, comment)
            mComment.comments = updatedComments
        }
        commentAdapter?.notifyItemChanged(mAdapterPosition, mComment.comments.get(0))
    }

    override fun onCommentDelete(position: Int, cid: String?, postId: String?, commentOn: String?) {
//        progressBar.show()
        Log.d(TAG, "Comment: $position / $cid / $postId / $commentOn")
        val hashMap: HashMap<String, String> = HashMap()
        hashMap.put("cid", cid!!)
        hashMap.put("postId", postId!!)
        hashMap.put("commentOn", commentOn!!)

        commentViewModel.deleteComment(hashMap)?.observe(this, Observer { generalResponse ->
//            progressBar.hide()
            mContext.showToast(generalResponse.message, Toast.LENGTH_SHORT)
            if (generalResponse.status == 200) {
                getPostComments()
                // update post recyclerview after removing comment in a post
                onCommentDeletePostUpdate?.onCommentDeletePostUpdate(postAdapterPosition)
            }
        })
    }

    companion object {
        private var onCommentDeletePostUpdate: IOnCommentDeletePostUpdate? = null
        fun setIOnCommentDeletePostUpdate(onCommentDeletePostUpdate: IOnCommentDeletePostUpdate) {
            this.onCommentDeletePostUpdate = onCommentDeletePostUpdate
        }
    }

    override fun onCommentRepliesDelete() {
        getPostComments()
    }
}