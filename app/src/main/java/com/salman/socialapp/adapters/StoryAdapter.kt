package com.salman.socialapp.adapters

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.*
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.firebase.auth.FirebaseAuth
import com.salman.socialapp.R
import com.salman.socialapp.model.FirebaseUserInfo
import com.salman.socialapp.model.Story
import com.salman.socialapp.network.BASE_URL
import com.salman.socialapp.ui.activities.AddStoryActivity
import com.salman.socialapp.ui.activities.StoryActivity

class StoryAdapter(
    val context: Context,
    val stories: List<Story> = ArrayList(),
    val currentUser: String,
    val users: List<FirebaseUserInfo>
    ) : RecyclerView.Adapter<StoryAdapter.StoryViewHolder>() {

    private var myStoryAction: ((String) -> Unit)? = null
    private var seenStoryAction: ((String) -> Unit)? = null
    private var userDetailsAction: ((String) -> Unit?)? = null
    val seenStoryCount: MutableLiveData<Int> = MutableLiveData()
    val myStoryCount: MutableLiveData<Int> = MutableLiveData()
    val sCount: LiveData<Int> = seenStoryCount
    val sCount2: LiveData<Int> = myStoryCount
    var user: FirebaseUserInfo? = null
    var hasStory = false
    var showShimmer = true
    val shimmer_item = 5

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryViewHolder {
        return if (viewType == 0) {
            val view: View = LayoutInflater.from(context).inflate(R.layout.item_add_story, parent, false)
            StoryViewHolder(view)
        } else {
            val view: View = LayoutInflater.from(context).inflate(R.layout.item_story, parent, false)
            StoryViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: StoryViewHolder, position: Int) {
        Log.d("StoryAdapter", "onBindViewHolder: $position")

        if (showShimmer) {
            holder.shimmer?.startShimmer()
        } else {
            val story = stories[position]
            val user = users.get(position)
            holder.shimmer?.stopShimmer()
            holder.shimmer?.setShimmer(null)
            holder.storyUserPhoto?.background = null
            holder.storyUsername?.setBackgroundColor(context.resources.getColor(R.color.transparent_black_80))
            holder.addstoryText?.setBackgroundColor(context.resources.getColor(R.color.transparent_black_80))

            val userUri = Uri.parse(user.image)
            val userImage = if (userUri.authority == null) {
                BASE_URL + user.image
            } else {
                user.image
            }

//        fetchUserDetails(holder, position, story.userid)
            if (position == 0) {
                Glide.with(context).load(userImage).into(holder.storyUserPhoto!!)
            } else if (position != 0 && position != -1) {
                Glide.with(context).load(userImage).into(holder.storyUserPhoto!!)
                Glide.with(context).load(userImage).into(holder.storyUserPhotoSeen!!)
                holder.storyUsername!!.text = user.name
            }

            if (position == 0) {
                if (story.storyImage!!.isNotEmpty()) {
                    hasStory = true
                    Glide.with(context).load(story.storyImage).into(holder.storyPhoto!!)
                    holder.storyPhoto!!.visibility = View.VISIBLE
                    holder.addstoryText!!.text = "My Story"
                    holder.storyPlus!!.visibility = View.GONE
                } else {
                    hasStory = false
                    holder.storyPhoto!!.visibility = View.INVISIBLE
                    holder.addstoryText!!.text = "Add Story"
                    holder.storyPlus!!.visibility = View.VISIBLE
                }
            } else if (position != 0 && position != -1) {
                Glide.with(context).load(story.storyImage).into(holder.storyPhoto!!)
                seenStory(holder, story.userid)
            }

            holder.itemView.setOnClickListener {
                if (position == 0) {
                    showAlertDialog(currentUser)
                } else if (position != 0 && position != -1) {
                    val intent = Intent(context, StoryActivity::class.java)
                    intent.putExtra("userId", story.userid)
                    context.startActivity(intent)
                }
            }

        }

    }

    fun setUserDetailsAction(userDetailsAction: ((String) -> Unit?)) {
        this.userDetailsAction = userDetailsAction
    }

    fun setMyStoryAction(myStoryAction: ((String) -> Unit)?) {
        this.myStoryAction = myStoryAction
    }

    fun setSeenStoryAction(seenStoryAction: (String) -> Unit) {
        this.seenStoryAction = seenStoryAction
    }

    private fun fetchUserDetails(holder: StoryViewHolder, position: Int, userid: String?) {
        userDetailsAction?.let {
            it(userid!!)
            Log.d("StoryAdapter", "init: $user")
            if (user != null) {

                val userUri = Uri.parse(user!!.image)
                val userImage = if (userUri.authority == null) {
                    BASE_URL + user!!.image
                } else {
                    user!!.image
                }

                if (position == 0) {
                    Glide.with(context).load(user!!.image).into(holder.storyUserPhoto!!)
                } else if (position != 0 && position != -1) {
                    Glide.with(context).load(user!!.image).into(holder.storyUserPhotoSeen!!)
                    holder.storyUsername!!.text = user!!.name
                }
            }
        }
    }

    private fun showAlertDialog(userid: String?) {
        if (hasStory) {
            val alertDialog = AlertDialog.Builder(context).create()
            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "View Story",
                DialogInterface.OnClickListener { dialogInterface, i ->
                            val intent = Intent(context, StoryActivity::class.java)
                            intent.putExtra("userId", userid)
                            context.startActivity(intent)
                            dialogInterface.dismiss()
                })
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Add Story") { dialogInterface, i ->
                val intent = Intent(context, AddStoryActivity::class.java)
                context.startActivity(intent)
                dialogInterface.dismiss()
            }
            alertDialog.show()
        } else {
            val intent = Intent(context, AddStoryActivity::class.java)
            context.startActivity(intent)
        }
    }

    /*private fun myStory(holder: StoryViewHolder, story: Story, click: Boolean) {

        myStoryAction?.let {
            it(FirebaseAuth.getInstance().currentUser?.uid!!)
        }

        sCount2.observe(context as LifecycleOwner, Observer { i ->
            if (click) {
                if (i > 0) {
                    val alertDialog = AlertDialog.Builder(context).create()
                    alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "View Story",
                        DialogInterface.OnClickListener { dialogInterface, i ->
                           val intent = Intent(context, StoryActivity)
                            context.startActivity(intent)
                            dialogInterface.dismiss()*//*
                    })
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Add Story") { dialogInterface, i ->
                        val intent = Intent(context, AddStoryActivity::class.java)
                        context.startActivity(intent)
                        dialogInterface.dismiss()
                    }
                    alertDialog.show()
                } else {
                    val intent = Intent(context, AddStoryActivity::class.java)
                    context.startActivity(intent)
                }
            } else {
                if (i > 0) {
                    holder.addstoryText.text = "My Story"
                    holder.storyPlus.visibility = View.GONE
                } else {
                    holder.storyPhoto.visibility = View.INVISIBLE
                    holder.addstoryText.text = "Add Story"
                    holder.storyPlus.visibility = View.VISIBLE
                }
            }
            notifyDataSetChanged()
        })
    }*/

    private fun seenStory(holder: StoryViewHolder, userid: String?) {
        seenStoryAction?.let {
            it(userid!!)
        }

/*        count.observeForever {
            if (it > 0) {
                holder.story_photo.visibility = View.VISIBLE
                holder.story_photo_seen.visibility = View.GONE
            } else {
                holder.story_photo.visibility = View.GONE
                holder.story_photo_seen.visibility = View.VISIBLE
            }
        }*/

        sCount.observe(context as LifecycleOwner, Observer {
            if (it > 0) {
                holder.storyUserPhoto!!.visibility = View.GONE
                holder.storyUserPhotoSeen!!.visibility = View.VISIBLE
            } else {
                holder.storyUserPhoto!!.visibility = View.VISIBLE
                holder.storyUserPhotoSeen!!.visibility = View.GONE
            }
//            notifyDataSetChanged()
        })

    }

    override fun getItemCount(): Int {
        return if (showShimmer) shimmer_item else stories.size
    }


    override fun getItemViewType(position: Int): Int {
        if (position == 0){
            return 0
        }
        return 1
    }

    inner class StoryViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
/*        val storyPhoto: ImageView = itemView.findViewById(R.id.story_photo)
        val storyPlus: ImageView = itemView.findViewById(R.id.story_plus)
        val storyUserPhoto: ImageView = itemView.findViewById(R.id.story_userphoto)
        val storyUserPhotoSeen: ImageView = itemView.findViewById(R.id.story_userphoto_seen)
        val storyUsername: TextView = itemView.findViewById(R.id.story_username)
        val addstoryText: TextView = itemView.findViewById(R.id.addstory_text)*/

        var storyPhoto: ImageView? = null
        var storyPlus: ImageView? = null
        var storyUserPhoto: ImageView? = null
        var storyUserPhotoSeen: ImageView? = null
        var storyUsername: TextView? = null
        var addstoryText: TextView? = null
        var shimmer: ShimmerFrameLayout? = null


        init {
            storyPhoto = itemView.findViewById(R.id.story_photo)
            storyPlus = itemView.findViewById(R.id.story_plus)
            storyUserPhoto = itemView.findViewById(R.id.story_userphoto)
            storyUserPhotoSeen = itemView.findViewById(R.id.story_userphoto_seen)
            storyUsername = itemView.findViewById(R.id.story_username)
            addstoryText = itemView.findViewById(R.id.addstory_text)
            shimmer = itemView.findViewById(R.id.shimmer)

        }

    }
}