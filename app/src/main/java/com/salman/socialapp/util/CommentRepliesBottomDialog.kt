package com.salman.socialapp.util

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.salman.socialapp.R
import com.salman.socialapp.adapters.CommentAdapter
import com.salman.socialapp.model.Comment
import com.salman.socialapp.model.PostComment
import com.salman.socialapp.ui.activities.MainActivity
import com.salman.socialapp.ui.activities.ProfileActivity
import com.salman.socialapp.viewmodels.CommentViewModel
import com.salman.socialapp.viewmodels.ViewModelFactory
import kotlinx.android.synthetic.main.bottom_dialog_comment.*
import kotlinx.android.synthetic.main.bottom_dialog_comment.view.*

private const val TAG = "CommentsBottomDialog"
class CommentRepliesBottomDialog(
    val iOnCommentRepliesAdded: IOnCommentRepliesAdded
) : BottomSheetDialogFragment(), IOnCommentDelete {
    lateinit var commentCountTV: TextView
    lateinit var commentRecView: RecyclerView
    lateinit var commentET: EditText
    lateinit var commentSendBtn: RelativeLayout
    lateinit var backPressBtn: ImageView
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
    var adapterPosition = 0
    lateinit var iOnCommentAdded: IOnCommentAdded

    var commentAdapter: CommentAdapter? = null
    var commentItems: MutableList<Comment> = ArrayList()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.mContext = context
        this.iOnCommentAdded = context as IOnCommentAdded
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
            adapterPosition = getInt("adapterPosition")
        }

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
        backPressBtn = view.backBtn
        progressBar = view.progress_bar

        if (openKeyboard) {
            commentET.requestFocus()
            val inputMethodManager =
                mContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.showSoftInput(comment_txt, InputMethodManager.SHOW_IMPLICIT)
        }

        setupRecyclerView()
        getCommentReplies()

        commentCountTV.text = "Replies"
        commentCountTV.textAlignment = View.TEXT_ALIGNMENT_CENTER

        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        dialog.setOnShowListener { dialogInterface ->
            val bottomSheetDialog = dialogInterface as BottomSheetDialog
            bottomSheet = bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.setBackgroundColor(Color.TRANSPARENT)
            bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet!!)
//            BottomSheetBehavior.from(bottomSheet!!).peekHeight = Resources.getSystem().getDisplayMetrics().heightPixels
            if (!openKeyboard) {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            } else {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }

        commentSendBtn.setOnClickListener {
            val commentText = commentET.text.toString()
            if (commentText.isEmpty()) {
                it.isEnabled = false
                mContext.showToast("please enter your comment !")
            } else {
                it.isEnabled = true
                val inputMethodManager =
                    mContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(it.windowToken, 0)
                commentET.setText("")

                // posting comment/reply
                postComment(commentText)

            }
        }

        backPressBtn.visibility = View.VISIBLE
        backPressBtn.setOnClickListener {
            if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED ||
                bottomSheetBehavior.state == BottomSheetBehavior.STATE_COLLAPSED) {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
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
                val postedComment = commentResponse.comments.get(0)
                commentItems.add(0, postedComment)
                commentAdapter?.notifyItemInserted(0)
                commentRecView.smoothScrollToPosition(0)
                val mComment =  Comment(
                    name = commentResponse.comments.get(0).name,
                    profileUrl = commentResponse.comments.get(0).profileUrl,
                    comment = commentResponse.comments.get(0).comment,
                    commentDate = commentResponse.comments.get(0).commentDate
                )
                // update comment recyclerview after adding reply in a comment
                iOnCommentRepliesAdded.oncommentRepliesAdded(adapterPosition, mComment)
                // update post recyclerview after adding comment in a post
                iOnCommentAdded.onCommentAdded(postAdapterPosition)
            }
        })
    }

    private fun getCommentReplies() {
        progressBar.show()
        commentViewModel.getCommentReplies(postId!!, parentId!!)?.observe(this, Observer { commentResponse ->
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

    override fun onCommentDelete(position: Int, cid: String?, postId: String?, commentOn: String?) {
//        progressBar.show()
        Log.d(TAG, "Comment-sub: $position / $cid / $postId / $commentOn")
        val hashMap: HashMap<String, String> = HashMap()
        hashMap.put("cid", cid!!)
        hashMap.put("postId", postId!!)
        hashMap.put("commentOn", commentOn!!)

        commentViewModel.deleteComment(hashMap)?.observe(this, Observer { generalResponse ->
//            progressBar.hide()
            mContext.showToast(generalResponse.message, Toast.LENGTH_SHORT)
            if (generalResponse.status == 200) {
                getCommentReplies()
                // update comment recyclerview after removing reply in a comment
                onCommentRepliesDelete?.onCommentRepliesDelete()
                // update post recyclerview after removing comment in a post
                onCommentDeletePostUpdate?.onCommentDeletePostUpdate(postAdapterPosition)
            }
        })
    }

    companion object {
        private var onCommentDeletePostUpdate: IOnCommentDeletePostUpdate? = null
        private var onCommentRepliesDelete: IOnCommentRepliesDelete? = null
        fun setIOnCommentDeletePostUpdate(onCommentDeletePostUpdate: IOnCommentDeletePostUpdate) {
            this.onCommentDeletePostUpdate = onCommentDeletePostUpdate
        }
        fun setIOnCommentRepliesDelete(onCommentRepliesDelete: IOnCommentRepliesDelete) {
            this.onCommentRepliesDelete = onCommentRepliesDelete
        }
    }

    interface IOnCommentRepliesDelete {
        fun onCommentRepliesDelete()
    }

}