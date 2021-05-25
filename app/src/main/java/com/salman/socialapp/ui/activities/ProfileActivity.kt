package com.salman.socialapp.ui.activities

import android.app.ActivityOptions
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.Pair
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.esafirm.imagepicker.features.ImagePicker
import com.esafirm.imagepicker.model.Image
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.salman.socialapp.R
import com.salman.socialapp.adapters.PostAdapter
import com.salman.socialapp.local.ProfilePost
import com.salman.socialapp.local.ProfilePostAdapter
import com.salman.socialapp.local.viewmodel.LocalViewModel
import com.salman.socialapp.model.PerformAction
import com.salman.socialapp.model.PerformReaction
import com.salman.socialapp.model.Post
import com.salman.socialapp.model.Profile
import com.salman.socialapp.network.BASE_URL
import com.salman.socialapp.util.*
import com.salman.socialapp.viewmodels.ProfileViewModel
import com.salman.socialapp.viewmodels.ViewModelFactory
import dagger.hilt.android.AndroidEntryPoint
import id.zelory.compressor.Compressor
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

private const val TAG = "ProfileActivityClass"

@AndroidEntryPoint
class ProfileActivity : AppCompatActivity(),
    PostAdapter.IUpdateUserReaction,
    IOnCommentAdded, PostAdapter.IPostMoreAction {

/*
* 0 = profile is still loading
* 1 = two people are friends (unfriend)
* 2 = we have sent friend request to that user (cancel request)
* 3 = we have received friend request from that user (accept/cancel request)
* 4 = we are not connected (send request)
* 5 = own profile (edit profile)
 */

    private var userId: String? = ""
    private var profileUrl: String? = ""
    private var coverUrl: String? = ""
    private var currentState = 0
    private var isCoverImage = false
    private var isImageSelected = false
    private var isNameChanged = false
    private var enteredName: String? = null
    private var compressedImageFile: File? = null
    var currentUserId: String? = ""
    var limit = 5
    var offset = 0
    var isPostEditable = false
    var isFirstLoading = true
    var isDataAvailable = true
    var isInternetConnectionEstablished = true
    var loadAfterUpdate = false
    lateinit var postItems: MutableList<Post>
    private lateinit var profileViewModel: ProfileViewModel

//        lateinit var localViewModel: LocalViewModel
    private val localViewModel by viewModels<LocalViewModel>()
    private var postAdapter: PostAdapter? = null
    private lateinit var profilePostAdapter: ProfilePostAdapter
    private lateinit var progressDialog: ProgressDialog
    // Custom CoroutineScope created by user
    private val scope = CoroutineScope(Dispatchers.IO + CoroutineName("MyScope"))
    private val scope2 = CoroutineScope(Dispatchers.IO + CoroutineName("MyScope2"))


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        setSupportActionBar(toolbar)
        toolbar.setNavigationIcon(R.drawable.ic_back)
        toolbar.setNavigationOnClickListener {
            super.onBackPressed()
        }

        // get user from sharedRef
        getUserFromSharedPref()

        intent?.let {
            getDataFromIntent(it)
        }

        // initialize fields
        init()

        // internet connected successfully
        checkInternetConnection()

        // view profile & cover picture
        clickToSeeImage()

        // refresh posts fetch from the net
        refreshPosts()

        // scroll for loading more posts
        scrollToMorePosts()

    }

    private fun getDataFromIntent(it: Intent) {
        userId = it.getStringExtra(USER_ID)

        if (userId.equals(FirebaseAuth.getInstance().uid)) {
            currentState = 5
            isPostEditable = true
        } else {
            action_btn.text = "Loading"
            action_btn.isEnabled = false
            isPostEditable = false
        }
    }


    private fun scrollToMorePosts() {
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (isInternetConnectionEstablished) {
                    if (isLastItemReached() && isDataAvailable) {
                        offset += limit
                        loadProfilePosts()
                    }
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()

/*        CoroutineScope(Dispatchers.Main).launch {
            delay(500)
            Log.d(TAG, "onResume: $isInternetConnectionEstablished")
            if (isInternetConnectionEstablished) {
                loadProfilePosts()
            } else {
                getProfilePostListFromLocalDb()
            }
        }*/

/*        Handler().postDelayed({
            Log.d(TAG, "onResume: $isInternetConnectionEstablished")
            if (isInternetConnectionEstablished) {
                loadProfilePosts()
            } else {
                getProfilePostListFromLocalDb()
            }
        }, 500)*/

    }

    private fun refreshPosts() {
        swipe.setOnRefreshListener {
            if (isInternetConnectionEstablished) {
                offset = 0
                isFirstLoading = true
                loadProfilePosts()
            } else {
                showToast("Try connecting to the internet..")
                swipe.isRefreshing = false
            }
        }
    }

    private fun checkInternetConnection() {

/*        InternetConnection(object : InternetConnection.Consumer {
            override fun accept(accept: Boolean) {
                // If internet connection establish successfully
                isInternetConnectionEstablished = accept
                Log.d(TAG, "accept: $isInternetConnectionEstablished")
                // check intetnet
                if (isInternetConnectionEstablished) {
                    // fetching user's profile data from internet
                    fetchProfileInfo()
                } else {
                    // fetching user's profile data from local db
                    val handler = Handler()
                    thread {
                        getProfileDataFromLocalDb()
                        handler.postDelayed({
                            showToast("No Internet Connection !")
                        }, 1000)
                    }
                }
            }
        })*/

        InternetConnection { accept ->
            Log.d(TAG, "accept: $accept")
            // If internet connection establish successfully
            isInternetConnectionEstablished = accept
            // check intetnet
            if (isInternetConnectionEstablished) {
                // fetching user's profile data from internet
                fetchProfileInfo()
                loadProfilePosts()
            } else {
                // fetching user's profile data from local db
/*                val handler = Handler()
                thread {
                    getProfileDataFromLocalDb()
                    handler.postDelayed({
                        showToast("No Internet Connection !")
                    }, 1000)
                }*/

                showToast("No Internet Connection")
                getProfileDataFromLocalDb()
                getProfilePostListFromLocalDb()
            }
        }

    }

    override fun onStop() {
        super.onStop()
/*        offset = 0
        postItems.clear()
        isFirstLoading = true*/
        progressDialog.dismiss()
    }

    private fun init() {
/*        progressDialog = ProgressDialog(this)
        progressDialog.setCancelable(false)
        progressDialog.setTitle("Loading")
        progressDialog.setMessage("Please wait...")*/

        progressDialog = ProgressDialog(this).also {
            it.setCancelable(false)
            // set transparent background for custom progress dialog
            it.window?.setBackgroundDrawableResource(android.R.color.transparent)
        }

        profileViewModel = ViewModelProvider(this, ViewModelFactory()).get(ProfileViewModel::class.java)
//        localViewModel = ViewModelProvider(this).get(LocalViewModel::class.java)

        postItems = ArrayList()
        postAdapter = PostAdapter(this, postItems, currentUserId!!, isPostEditable)
        recyclerView.adapter = postAdapter
        postAdapter?.setOnPostMoreAction(this)
        recyclerView.layoutManager = LinearLayoutManager(this)

        PostUploadActivity.setPostLoadingListener { isUpdated ->
            if (isUpdated) {
                offset = 0
                isFirstLoading = true
                postItems.clear()
                postAdapter?.notifyDataSetChanged()
                loadProfilePosts()
            }
        }

    }

    private fun getUserFromSharedPref() {
        Utils(this).apply {
            val userInfo = getUserFromSharedPref()
            Log.d(TAG, "UserInfo: $userInfo")
            if (userInfo != null)
                currentUserId = userInfo.uid
        }
    }

    private fun isLastItemReached(): Boolean {
        val layoutManager: LinearLayoutManager = recyclerView.layoutManager as LinearLayoutManager
        val position = layoutManager.findLastCompletelyVisibleItemPosition()
        val numberOfItems = postAdapter!!.itemCount
        Log.d(TAG, "position: $position | numberOfItems: $numberOfItems")
        return (position != -1 && position >= numberOfItems - 1)
    }

    private fun showProgressDiaglog() {
        progressDialog.show()
        progressDialog.setContentView(R.layout.custom_progress_dialog)
    }

    private fun clickToSeeImage() {
        profile_image.setOnClickListener {
            viewFullImage(profile_image, profileUrl)
        }

        profile_cover.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View?) {
                viewFullImage(profile_cover, coverUrl)
            }
        })
    }

    private fun viewFullImage(imageview: ImageView, imageUrl: String?) {
        val intent = Intent(this, ViewImageActivity::class.java)
            .putExtra("imageUrl", imageUrl)

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
//          var pairs: Array<Pair<View, String>> = arrayOf()
//          pairs[0] = Pair<View, String>(imageview, imageUrl)
            val pair = Pair<View, String>(imageview, imageUrl)
            val activityOptions = ActivityOptions.makeSceneTransitionAnimation(this, pair)
            startActivity(intent, activityOptions.toBundle())
        } else {
            startActivity(intent)
        }
    }

    private fun fetchProfileInfo() {
        showProgressDiaglog()
        val params = HashMap<String, String>()
        params.put("userId", currentUserId!!)
        if (currentState == 5) {
            params.put("current_state", currentState.toString())
        } else {
            params.put("profileId", userId!!)
        }

        profileViewModel.fetchProfileInfo(params)?.observe(this, Observer { profileResponse ->
            Log.d(TAG, "fetchProfileInfo: $profileResponse")
            progressDialog.hide()
            if (profileResponse.status == 200) {
                collapsing_toolbar.title = profileResponse.profile?.name
                profileUrl = profileResponse.profile?.profileUrl
                coverUrl = profileResponse.profile?.coverUrl
                profileResponse.profile?.state?.let {
                    currentState = it.toInt()
                }

                if (!profileUrl!!.isEmpty()) {
                    val profileUri = Uri.parse(profileUrl)
                    // https://www.fb.com
                    // www.fb.com (this part is called authority)
                    if (profileUri.authority == null) {
                        profileUrl = BASE_URL + profileUrl
                    }
                    Glide.with(this).load(profileUrl).placeholder(R.drawable.loading_image).into(profile_image)
                } else {
                    profileUrl = R.drawable.default_profile_placeholder.toString()
                }
                if (!coverUrl!!.isEmpty()) {
                    val coverUri = Uri.parse(coverUrl)
                    // https://www.fb.com
                    // www.fb.com (this part is called authority)
                    if (coverUri.authority == null) {
                        coverUrl = BASE_URL + coverUrl
                    }
                    Glide.with(this).load(coverUrl).placeholder(R.drawable.loading_image).into(profile_cover)
                } else {
                    coverUrl = R.drawable.cover_picture_placeholder.toString()
                }
                when (currentState) {
                    0 -> {
                        action_btn.text = "Loading"
                        action_btn.isEnabled = false
                    }
                    1 -> action_btn.text = "Friends"
                    2 -> action_btn.text = "Cancel Request"
                    3 -> action_btn.text = "Accept or Cancel"
                    4 -> action_btn.text = "Send Request"
                    5 -> action_btn.text = "Edit Profile"
                }
                action_btn.isEnabled = true
                loadProfileOptionButton()
//                loadProfilePosts()
            } else {
                Toast.makeText(this, profileResponse.message, Toast.LENGTH_LONG).show()
            }
            deleteProfileFromLocalDb()
            saveProfileToLocalDb(profileResponse.profile!!)
        })
    }

    private fun deleteProfileFromLocalDb() {
        Log.d(TAG, "deleteProfileFromLocalDb: called")
        lifecycleScope.launch {
            localViewModel.deleteProfileFromLocalDb()
        }
    }

    private fun saveProfileToLocalDb(profile: Profile) {
        Log.d(TAG, "saveProfileToLocalDb: $profile")
        lifecycleScope.launch {
            localViewModel.saveProfileToLocalDb(profile)
        }
    }

    private fun getProfileDataFromLocalDb() {
        scope.launch {
            val profileFromLocalDb = localViewModel.getProfileFromLocalDb()
            Log.d(TAG, "getProfileDataFromLocalDb: $profileFromLocalDb")
            
            withContext(Dispatchers.Main) {

                collapsing_toolbar.title = profileFromLocalDb.name
                profileUrl = profileFromLocalDb.profileUrl
                coverUrl = profileFromLocalDb.coverUrl
                currentState = profileFromLocalDb.state!!.toInt()

                if (!profileUrl!!.isEmpty()) {
                    val profileUri = Uri.parse(profileUrl)
                    // https://www.fb.com
                    // www.fb.com (this part is called authority)
                    if (profileUri.authority == null) {
                        profileUrl = BASE_URL + profileUrl
                    }
                    Glide.with(this@ProfileActivity).load(profileUrl).into(profile_image)
                } else {
                    profileUrl = R.drawable.default_profile_placeholder.toString()
                }

                if (!coverUrl!!.isEmpty()) {
                    val coverUri = Uri.parse(coverUrl)
                    // https://www.fb.com
                    // www.fb.com (this part is called authority)
                    if (coverUri.authority == null) {
                        coverUrl = BASE_URL + coverUrl
                    }
                    Glide.with(this@ProfileActivity).load(coverUrl).into(profile_cover)
                } else {
                    coverUrl = R.drawable.cover_picture_placeholder.toString()
                }

                when (currentState) {
                    0 -> {
                        action_btn.text = "Loading"
                        action_btn.isEnabled = false
                    }
                    1 -> action_btn.text = "Friends"
                    2 -> action_btn.text = "Cancel Request"
                    3 -> action_btn.text = "Accept or Cancel"
                    4 -> action_btn.text = "Send Request"
                    5 -> action_btn.text = "Edit Profile"
                }
                action_btn.isEnabled = false
            }
        }
    }

    private fun loadProfilePosts() {
        progressbar.show()
        val params = HashMap<String, String>()
        params.put("uid", userId!!)
        params.put("limit", limit.toString())
        params.put("offset", offset.toString())
        params.put("current_state", currentState.toString())

        profileViewModel.loadProfilePosts(params)?.observe(this, Observer { postResponse ->
            Log.d(TAG, "loadProfilePosts: $postResponse")
            progressbar.hide()
            if (postResponse.status == 200) {

                if (swipe.isRefreshing) {
//                    postAdapter.posts.clear()
                    postItems.clear()
                    postAdapter?.notifyDataSetChanged()
                    swipe.isRefreshing = false
                }
                postItems.addAll(postResponse.posts)
//                postAdapter?.updateList(postResponse.posts)

                if (isFirstLoading) {
                    postAdapter?.notifyDataSetChanged()
                } else {
                    postAdapter?.itemRangeInserted(postItems.size, postResponse.posts.size)
                }
                if (postResponse.posts.size == 0) {
                    offset -= limit
                    isDataAvailable = false
                } else {
                    isDataAvailable = true
                }
                isFirstLoading = false

            } else {
                if (swipe.isRefreshing) {
                    swipe.isRefreshing = false
                }
                showToast(postResponse.message)
            }
            if (isDataAvailable) {
                deleteProfilePostListTolocalDb()
                saveProfilePostListToLocalDb(postItems)
            }
        })
    }

    private fun saveProfilePostListToLocalDb(postItems: MutableList<Post>) {
        val profilePosts = postItems.map {
            ProfilePost(it.postId, it.postUserId, it.post, it.statusImage, it.statusTime, it.privacy, it.uid, it.name,
                it.profileUrl, it.loveCount, it.careCount, it.wowCount, it.sadCount, it.angryCount, it.likeCount,
                it.hahaCount, it.commentCount, it.reactionType
            )
        }
        Log.d(TAG, "saveProfilePostListToLocalDb: ${profilePosts.size}")
        lifecycleScope.launch {
            localViewModel.saveProfilePostListToLocalDb(profilePosts)
        }
    }

    private fun deleteProfilePostListTolocalDb() {
        Log.d(TAG, "deleteProfilePostListTolocalDb: called")
        lifecycleScope.launch {
            localViewModel.deleteProfilePostListFromLocalDb()
        }
    }

    private fun getProfilePostListFromLocalDb() {

        scope2.launch {
            val profilePosts = localViewModel.getProfilePostListFromLocalDb()
            Log.d(TAG, "getProfilePostListFromLocalDb: ${profilePosts.size}")

            withContext(Dispatchers.Main) {
                profilePostAdapter = ProfilePostAdapter(
                    this@ProfileActivity,
                    profilePosts.toMutableList(),
                    currentUserId!!,
                    isPostEditable
                )
                recyclerView.adapter = profilePostAdapter
            }
        }

/*        val handler = Handler()
        thread {
            val profilePosts = localViewModel.getProfilePostListFromLocalDb()
            Log.d(TAG, "getProfilePostListFromLocalDb: ${profilePosts.size}")
            handler.post {
                profilePostAdapter = ProfilePostAdapter(
                    this@ProfileActivity,
                    profilePosts.toMutableList(),
                    currentUserId!!,
                    isPostEditable
                )
                recyclerView.adapter = profilePostAdapter
            }
        }*/

    }

    private fun loadProfileOptionButton() {
        action_btn.setOnClickListener {
            action_btn.isEnabled = false
            if (currentState == 5) {
                val options: Array<CharSequence> =
                    arrayOf("Change Cover Image", "Change Profile Image", "Change User Name")
                val builder = AlertDialog.Builder(this).apply {
                    setTitle("Choose Options")
                    setCancelable(false)
                    setItems(options, DialogInterface.OnClickListener { dialogInterface, position ->
                        if (position == 0) {
                            isCoverImage = true
                            selectImage()
                        } else if (position == 1) {
                            isCoverImage = false
                            selectImage()
                        } else {
                            showNameChangeDialog()
                        }
                    })
                    setNegativeButton("Cancel", null)
                }
                val dialog = builder.create()
                dialog.setOnDismissListener {
                    action_btn.isEnabled = true
                }
                dialog.show()
            } else if (currentState == 4) {
                val options: Array<CharSequence> = arrayOf("Send Friend Request")
                val builder = AlertDialog.Builder(this).apply {
                    setTitle("Choose Options")
                    setCancelable(false)
                    setItems(options, DialogInterface.OnClickListener { dialogInterface, position ->
                        if (position == 0) {
                            performFriendAction()
                        }
                    })
                    setNegativeButton(
                        "Cancel",
                        DialogInterface.OnClickListener { dialogInterface, i ->
                        })
                }
                val dialog = builder.create()
                dialog.setOnDismissListener {
                    action_btn.isEnabled = true
                }
                dialog.show()
            } else if (currentState == 3) {
                val options: Array<CharSequence> =
                    arrayOf("Accept Friend Request", "Cancel Friend Request")
                val builder = AlertDialog.Builder(this).apply {
                    setTitle("Choose Options")
                    setCancelable(false)
                    setItems(options, DialogInterface.OnClickListener { dialogInterface, position ->
                        if (position == 0) {
                            performFriendAction()
                        } else {
                            currentState = 2
                            performFriendAction()
                        }
                    })
                    setNegativeButton(
                        "Cancel",
                        DialogInterface.OnClickListener { dialogInterface, i ->

                        })
                }
                val dialog = builder.create()
                dialog.setOnDismissListener {
                    action_btn.isEnabled = true
                }
                dialog.show()
            } else if (currentState == 2) {
                val options: Array<CharSequence> = arrayOf("Cancel Friend Request")
                val builder = AlertDialog.Builder(this).apply {
                    setTitle("Choose Options")
                    setItems(options, DialogInterface.OnClickListener { dialogInterface, position ->
                        if (position == 0) {
                            performFriendAction()
                        }
                    })
                    setNegativeButton(
                        "Cancel",
                        DialogInterface.OnClickListener { dialogInterface, i ->
                            dialogInterface.dismiss()
                        })
                }
                val dialog = builder.create()
                dialog.setOnDismissListener {
                    action_btn.isEnabled = true
                }
                dialog.show()
            } else if (currentState == 1) {
                val options: Array<CharSequence> = arrayOf("Unfriend")
                val builder = AlertDialog.Builder(this).apply {
                    setTitle("Choose Options")
                    setItems(options, DialogInterface.OnClickListener { dialogInterface, position ->
                        if (position == 0) {
                            performFriendAction()
                        }
                    })
                    setNegativeButton(
                        "Cancel",
                        DialogInterface.OnClickListener { dialogInterface, i ->
                            dialogInterface.dismiss()
                        })
                }
                val dialog = builder.create()
                dialog.setOnDismissListener {
                    action_btn.isEnabled = true
                }
                dialog.show()
            }
        }
    }

    private fun performFriendAction() {
        progressDialog.show()
        val performAction =
            PerformAction(currentState.toString(), FirebaseAuth.getInstance().uid, userId)
        profileViewModel.performFriendAction(performAction)
            ?.observe(this, Observer { generalResponse ->
                progressDialog.hide()
//            Toast.makeText(this, generalResponse.message, Toast.LENGTH_LONG).show()
                Snackbar.make(rootView, generalResponse.message + "", Snackbar.LENGTH_LONG).show()
                if (generalResponse.status == 200) {
                    action_btn.isEnabled = true
                    if (currentState == 4) {
                        currentState = 2
                        action_btn.text = "Cancel Request"
                    } else if (currentState == 3) {
                        currentState = 1
                        action_btn.text = "Friends"
                    } else if (currentState == 2) {
                        currentState = 4
                        action_btn.text = "Send Request"
                    } else if (currentState == 1) {
                        currentState = 4
                        action_btn.text = "Send Request"
                    } else {
                        action_btn.isEnabled = false
                        action_btn.text = "Error"
                    }
                }
            })
    }

    private fun selectImage() {
        ImagePicker.create(this).single().start()
//        to display image in folder view
//        ImagePicker.create(this).single().folderMode(true).start()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (ImagePicker.shouldHandle(requestCode, resultCode, data)) {
            val selectedImage: Image = ImagePicker.getFirstImageOrNull(data)
            try {
                compressedImageFile =
                    Compressor(this).setQuality(50).compressToFile(File(selectedImage.path))
                uploadImageWithName(compressedImageFile)
            } catch (e: IOException) {
                Toast.makeText(this, "Image Picker Failed !", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun uploadImageWithName(compressedImageFile: File?) {
        progressDialog.show()
        val builder = MultipartBody.Builder().apply {
            setType(MultipartBody.FORM)
            addFormDataPart("uid", currentUserId!!)

            compressedImageFile?.let { imageFile ->
                addFormDataPart("isCoverImage", isCoverImage.toString())

                val createRequestBody =
                    RequestBody.create("multipart/form-data".toMediaTypeOrNull(), imageFile)

                // alternate to RequestBody.create() method
//              val createRequestBody = imageFile.asRequestBody("multipart/form-data".toMediaTypeOrNull())

                addFormDataPart("file", imageFile.name, createRequestBody)
                isImageSelected = true
            }

            enteredName?.let { name ->
                if (name.trim().length > 0) {
                    addFormDataPart("name", name)
                    isNameChanged = true
                }
            }
        }
        val multipartBody = builder.build()

        profileViewModel.uploadPost(multipartBody, true)?.observe(this, Observer { uploadResponse ->
            progressDialog.hide()
//          Toast.makeText(this, uploadResponse.message, Toast.LENGTH_LONG).show()
            Snackbar.make(rootView, uploadResponse.message.toString(), Snackbar.LENGTH_LONG).show()
            if (uploadResponse.status == 200) {
                if (isImageSelected) {
                    if (isCoverImage) {
                        Glide.with(this)
                            .load(BASE_URL + uploadResponse.extra)
                            .into(profile_cover)
                    } else {
                        Glide.with(this)
                            .load(BASE_URL + uploadResponse.extra)
                            .into(profile_image)
                        updateUserInFirebase(uploadResponse.extra, 0)
                    }
                }
                if (isNameChanged) {
                    collapsing_toolbar.title = uploadResponse.extra
                    updateUserInFirebase(uploadResponse.extra, 1)
                }
            }
        })
    }

    private fun updateUserInFirebase(data: String?, i: Int) {
        val userRef: DatabaseReference = FirebaseDatabase.getInstance()
            .getReference("fusers")
            .child(currentUserId!!)
        val hashMap: HashMap<String, Any> = HashMap()
        when (i) {
            0 -> {
                hashMap["image"] = data!!
                userRef.updateChildren(hashMap)
            }
            1 -> {
                hashMap["name"] = data!!
                userRef.updateChildren(hashMap)
            }
        }
    }

    private fun showNameChangeDialog() {
        val nameEditText = EditText(this)
        nameEditText.hint = "Enter your name..."
        val dialog: AlertDialog = AlertDialog.Builder(this)
            .setTitle("Change name")
            .setView(nameEditText)
            .setPositiveButton("Change", DialogInterface.OnClickListener { dialogInterface, i ->
                val nameTxt = nameEditText.text.toString()
                if (!nameTxt.isEmpty()) {
                    enteredName = nameTxt
                    uploadImageWithName(null)
                } else {
                    nameEditText.error = "field empty"
                    nameEditText.requestFocus()
                }
            })
            .setNegativeButton("Cancel", DialogInterface.OnClickListener { dialogInterface, i -> })
            .create()
        dialog.show()
    }

    override fun updateUserReaction(
        uId: String,
        postId: Int,
        postOwnerId: String,
        previousReactionType: String,
        newReactionType: String,
        position: Int
    ) {
        val performReaction = PerformReaction(
            uId,
            postId.toString(),
            postOwnerId,
            previousReactionType,
            newReactionType
        )
        profileViewModel.performReaction(performReaction)
            ?.observe(this, Observer { reactionResponse ->
                if (reactionResponse.status == 200) {
                    postAdapter?.updatePostAfterReaction(position, reactionResponse.reaction!!)
                } else {
                    showToast(reactionResponse.message)
                }
            })
    }

    override fun onCommentAdded(position: Int) {
        postAdapter?.increaseCommentCount(position)
    }

    override fun updatePost(post: Post) {
        val mIntent = Intent(this, PostUploadActivity::class.java).apply {
            putExtra("profileUrl", post.profileUrl)
            putExtra("postId", post.postId)
            putExtra("postTitle", post.post)
            putExtra("postImage", post.statusImage)
            putExtra("postPrivacy", post.privacy?.toInt())
            putExtra("editPost", true)
        }
        startActivity(mIntent)
    }

    override fun deletePost(postId: Int, mAdapterPosition: Int) {
        val snackBar =
            Snackbar.make(rootView, "Confirm delete ? Or swipe to cancel", Snackbar.LENGTH_LONG)
        snackBar.setAction(
            "YES"
        ) {
            //                Log.d(TAG, "deletePost: $postId | $mAdapterPosition")
            profileViewModel.deletePost(postId.toString())?.observe(this, Observer {
                showToast(it.message)
                if (it.status == 200) {
                    postAdapter?.notifyItemRemoved(mAdapterPosition)
                }
            })
        }
        snackBar.setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE)
        snackBar.show()

    }

}