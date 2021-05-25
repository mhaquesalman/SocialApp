package com.salman.socialapp.ui.activities

import android.annotation.SuppressLint
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.salman.socialapp.R
import com.salman.socialapp.adapters.StoryWatcherAdapter
import com.salman.socialapp.model.FirebaseUserInfo
import com.salman.socialapp.model.Story
import com.salman.socialapp.network.BASE_URL
import com.salman.socialapp.util.hide
import com.salman.socialapp.util.show
import com.salman.socialapp.util.showToast
import jp.shts.android.storiesprogressview.StoriesProgressView
import kotlinx.android.synthetic.main.activity_story.*
import kotlinx.android.synthetic.main.story_view_bottom_sheet.*
import kotlinx.android.synthetic.main.story_view_bottom_sheet.view.*

private const val TAG = "StoryWatch"
const val STORY_PREVIEW_DURATION = 5000L
class StoryActivity : AppCompatActivity(), StoriesProgressView.StoriesListener {
    var counter = 0
    var pressTime = 0L
    var limit = 500L
    var storiesProgressView: StoriesProgressView? = null
    var images: ArrayList<String>? = null
    var storyIds: ArrayList<String>? = null
    var ids: ArrayList<String> = ArrayList()
    var storyWatchers: MutableList<FirebaseUserInfo> = ArrayList()
    var userId: String? = null
    var storyRecyclerView: RecyclerView? = null
    var cancelBottomSheetIV: ImageView? = null
    var progress: ProgressBar? = null
    var currentUser: String? = null
    var storyWatcherAdapter: StoryWatcherAdapter? = null
    lateinit var bottomSheet: LinearLayout
    lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>
    lateinit var shimmer: ShimmerFrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_story)

        bottomSheet = findViewById(R.id.bottom_sheet_container)
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        storyRecyclerView = findViewById(R.id.storyRV)
        cancelBottomSheetIV = findViewById(R.id.bottom_sheet_cancel)
        progress = findViewById(R.id.progress)
        shimmer = findViewById(R.id.shimmer_view)

        currentUser = FirebaseAuth.getInstance().currentUser?.uid
        userId = intent.getStringExtra("userId")

        getStories(userId!!)
        userInfo(userId!!)

        reverse.setOnClickListener {
            stories.reverse()
        }
        reverse.setOnTouchListener(onTouchListener)

        skip.setOnClickListener {
            stories.skip()
        }
        skip.setOnTouchListener(onTouchListener)

        if (userId.equals(currentUser)){
            seen_layout.visibility = View.VISIBLE
            seen_layout.setOnClickListener {
//                openBottomSheetDialog()
                openSheet()
                stories.pause()
            }
        } else {
            seen_layout.visibility = View.INVISIBLE
        }

        story_delete.setOnClickListener {
            deleteStory()
        }
    }

    private fun deleteStory() {
        val reference = FirebaseDatabase.getInstance().getReference("Story")
            .child(userId!!).child(storyIds!![counter])
        reference.removeValue().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                showToast("Story deleted!")
                finish()
            }
        }
    }

    private fun openSheet() {
/*        if (bottomSheetBehavior.state != BottomSheetBehavior.STATE_EXPANDED ||
            bottomSheetBehavior.state == BottomSheetBehavior.STATE_HIDDEN) {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }*/
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

        storyRecyclerView?.layoutManager = LinearLayoutManager(this)
        storyRecyclerView?.setHasFixedSize(true)
        getViews()

        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {

            override fun onStateChanged(bottomSheet: View, newState: Int) {

            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                cancelBottomSheetIV?.visibility = View.VISIBLE
                cancelBottomSheetIV?.rotation = slideOffset * 180
            }
        })

        cancelBottomSheetIV?.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }
    }

    private fun openBottomSheetDialog() {
        Log.d(TAG, "openBottomSheetDialog: called")
        val bottomSheetDialog = BottomSheetDialog(this, R.style.BottomSheetDialog)
/*        val view = LayoutInflater.from(this).inflate(R.layout.story_view_bottom_sheet,
            findViewById<LinearLayout>(R.id.bottom_sheet_container))*/
        val view = LayoutInflater.from(this).inflate(R.layout.story_view_bottom_sheet, null)
        storyRecyclerView = view.storyRV
        cancelBottomSheetIV = view.bottom_sheet_cancel
        progress = view.progress

        storyRecyclerView?.layoutManager = LinearLayoutManager(this)

        getViews()

        cancelBottomSheetIV!!.setOnClickListener {
            bottomSheetDialog.dismiss()
        }
        bottomSheetDialog.setContentView(view)
        bottomSheetDialog.behavior.peekHeight = 1000
        bottomSheetDialog.show()
    }

    override fun onNext() {
        Glide.with(this@StoryActivity).load(images!![++counter]).into(image)

        addView(storyIds!!.get(counter))
        seenNumber(storyIds!!.get(counter))
    }

    override fun onPrev() {
        if ((counter - 1) < 0) return
        Glide.with(this@StoryActivity).load(images!![--counter]).into(image)
        seenNumber(storyIds!!.get(counter))
    }

    override fun onComplete() {
        finish()
    }

    override fun onResume() {
        super.onResume()
        stories.resume()
    }

    override fun onPause() {
        super.onPause()
        stories.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        stories.destroy()
    }

    private fun getStories(userId: String) {
        images = ArrayList()
        storyIds = ArrayList()
        val reference = FirebaseDatabase.getInstance().getReference("Story").child(userId)
        reference.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                images!!.clear()
                storyIds!!.clear()
                for (snapshot in dataSnapshot.children) {
                    val story = snapshot.getValue(Story::class.java)
                    val timeCurrent = System.currentTimeMillis()
                    if (timeCurrent > story!!.timeStart && timeCurrent < story.timeEnd) {
                        images!!.add(story.storyImage!!)
                        storyIds!!.add(story.storyid!!)
                    }
                }

                stories.apply {
                    setStoriesCount(images!!.size)
                    setStoryDuration(STORY_PREVIEW_DURATION)
                    setStoriesListener(this@StoryActivity)
                    startStories(counter)
                }

                Glide.with(this@StoryActivity).load(images!![counter]).into(image)

                addView(storyIds!![counter])
                seenNumber(storyIds!![counter])

            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun userInfo(userId: String) {
        val reference = FirebaseDatabase.getInstance().getReference("fusers").child(userId)
        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val firebaseUserInfo = dataSnapshot.getValue(FirebaseUserInfo::class.java)
                val userUri = Uri.parse(firebaseUserInfo!!.image)
                val userImage = if (userUri.authority == null) {
                    BASE_URL + firebaseUserInfo.image
                } else {
                    firebaseUserInfo.image
                }
                Glide.with(this@StoryActivity).load(userImage).into(story_userphoto)
                story_username.text = firebaseUserInfo.name
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun addView(storyId: String) {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        FirebaseDatabase.getInstance().getReference("Story").child(userId!!)
            .child(storyId).child("views").child(firebaseUser?.uid!!).setValue(true)
    }


    private fun seenNumber(storyId: String) {
        val reference = FirebaseDatabase.getInstance().getReference("Story").child(userId!!)
            .child(storyId).child("views")
        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                seen_number.text = dataSnapshot.childrenCount.toString()
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })

    }

    private fun getViews() {
        Log.d(TAG, "getViews: called")
//        progress!!.show()
        shimmer.startShimmer()
        val reference = FirebaseDatabase.getInstance().getReference("Story")
            .child(userId!!).child(storyIds!!.get(counter)).child("views")
        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                ids.clear()
                for (snapshot in dataSnapshot.children) {
                    ids.add(snapshot.key!!)
                }
                Log.d(TAG, "onDataChange: ids: ${ids}")
                showViews()
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun showViews() {
        Log.d(TAG, "showViews: called")
        val dataRef = FirebaseDatabase.getInstance().getReference("fusers")
        dataRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                progress!!.hide()
                storyWatchers.clear()
                for (id in ids) {
                    for (snapshot in dataSnapshot.children) {
                        val firebaseUserInfo = snapshot.getValue(FirebaseUserInfo::class.java)
                        if (firebaseUserInfo!!.id.equals(id) && !firebaseUserInfo.id.equals(currentUser)) {
                            storyWatchers.add(firebaseUserInfo)
                        }
                    }
                }
                Log.d(TAG, "onDataChange: storywatchers: ${storyWatchers}")
                shimmer.stopShimmer()
                shimmer.visibility = View.GONE
                storyWatcherAdapter = StoryWatcherAdapter(this@StoryActivity, storyWatchers)
                storyRecyclerView?.visibility = View.VISIBLE
                storyRecyclerView?.adapter = storyWatcherAdapter

            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

/*    private val onTouchListener = View.OnTouchListener { view: View, motionEvent: MotionEvent ->
        when (motionEvent.action) {
            MotionEvent.ACTION_DOWN -> {
                pressTime = System.currentTimeMillis()
                stories.pause()
                true
            }
            MotionEvent.ACTION_UP -> {
                val now = System.currentTimeMillis()
                stories.resume()
                limit < now - pressTime
            }
        }
        view.performClick()
        view.onTouchEvent(motionEvent)
    }*/

    val onTouchListener = object : View.OnTouchListener {
        override fun onTouch(v: View?, event: MotionEvent?): Boolean {
            when (event?.action) {
                MotionEvent.ACTION_DOWN -> {
                    pressTime = System.currentTimeMillis()
                    stories.pause()
                    return false
                }
                MotionEvent.ACTION_UP -> {
                    val now = System.currentTimeMillis()
                    stories.resume()
                    return limit < now - pressTime
                }
            }
            return false
        }
    }

}