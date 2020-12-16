package com.salman.socialapp.ui.activities

import android.app.ActivityOptions
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Pair
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.esafirm.imagepicker.features.ImagePicker
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.salman.socialapp.R
import com.salman.socialapp.network.BASE_URL
import com.salman.socialapp.viewmodels.ProfileViewModel
import com.salman.socialapp.viewmodels.ViewModelFactory
import id.zelory.compressor.Compressor
import kotlinx.android.synthetic.main.activity_profile.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.IOException

class ProfileActivity : AppCompatActivity() {

/*
* 0 = profile is still loading
* 1 = two people are friends
* 2 = we have sent friend request to that user
* 3 = we have received friend request from that user
* 4 = we are not connected
* 5 = own profile
 */

    private var userId: String? = ""
    private var profileUrl: String? = ""
    private var coverUrl: String? = ""
    private var currentState = 0
    private var isCoverImage = false
    private var isImageSelected = false
    private var isNameChanged = false
    private var enteredName: String? = null
    var compressedImageFile: File? = null
    lateinit var profileViewModel: ProfileViewModel
    lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        progressDialog = ProgressDialog(this)
        progressDialog.setCancelable(false)
        progressDialog.setTitle("Loading")
        progressDialog.setMessage("Please wait...")

        profileViewModel = ViewModelProvider(this, ViewModelFactory()).get(ProfileViewModel::class.java)

        setSupportActionBar(toolbar)
        toolbar.setNavigationIcon(R.drawable.ic_back)
        toolbar.setNavigationOnClickListener {
            super.onBackPressed()
        }

        if (intent != null)
        userId = intent.getStringExtra(USER_ID)

        if (userId.equals(FirebaseAuth.getInstance().uid)) {
            currentState = 5
        } else {
            action_btn.text = "Loading"
            action_btn.isEnabled = false
        }

        fetchProfileInfo()
        clickToSeeImage()

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
            val activityOptions =
                ActivityOptions.makeSceneTransitionAnimation(this, pair)
            startActivity(intent, activityOptions.toBundle())
        } else {
            startActivity(intent)
        }
    }

    private fun fetchProfileInfo() {
        progressDialog.show()
        val params = HashMap<String, String>()
        params.put("userId", FirebaseAuth.getInstance().uid!!)
        if (currentState == 5) {
            params.put("current_state", currentState.toString())
        } else {
            params.put("profileId", userId!!)
        }

        profileViewModel.fetchProfileInfo(params)?.observe(this, Observer { profileResponse ->
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
                    Glide.with(this).load(profileUrl).into(profile_image)
                }
                if (!coverUrl!!.isEmpty()) {
                    val coverUri = Uri.parse(coverUrl)
                    // https://www.fb.com
                    // www.fb.com (this part is called authority)
                    if (coverUri.authority == null) {
                        coverUrl = BASE_URL + coverUrl
                    }
                    Glide.with(this).load(coverUrl).into(profile_cover)
                }
                when(currentState) {
                    0 -> {
                        action_btn.text = "Loading"
                        action_btn.isEnabled = false
                    }
                    1 -> action_btn.text = "Friends"
                    2 -> action_btn.text = "Cancel Request"
                    3 -> action_btn.text = "Accept Request"
                    4 -> action_btn.text = "Send Request"
                    5 -> action_btn.text = "Edit Profile"
                }
                action_btn.isEnabled = true
                loadProfileOptionButton()
            } else {
                Toast.makeText(this, profileResponse.message, Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun loadProfileOptionButton() {
        action_btn.setOnClickListener {
            action_btn.isEnabled = false
            if (currentState == 5) {
                val options: Array<CharSequence> = arrayOf("Change Cover Image", "Change Profile Image", "Change User Name")
                val builder = AlertDialog.Builder(this).apply {
                    setTitle("Choose Options")
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
                }
                val dialog = builder.create()
                dialog.setOnDismissListener {
                    action_btn.isEnabled = true
                }
                dialog.show()
            }
        }
    }

    private fun selectImage() {
        ImagePicker.create(this).single().start()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (ImagePicker.shouldHandle(requestCode, resultCode, data)) {
            val selectedImage = ImagePicker.getFirstImageOrNull(data)
            try {
                compressedImageFile = Compressor(this).setQuality(75).compressToFile(File(selectedImage.path))
                uploadImageWithName(compressedImageFile)
            } catch (e : IOException) {
                Toast.makeText(this, "Image Picker Failed !", Toast.LENGTH_SHORT).show()
            }

        }
    }

    private fun uploadImageWithName(compressedImageFile: File?) {
        progressDialog.show()
        val builder = MultipartBody.Builder().apply {
            setType(MultipartBody.FORM)
            addFormDataPart("uid", FirebaseAuth.getInstance().uid.toString())

            compressedImageFile?.let { imageFile ->
                addFormDataPart("isCoverImage", isCoverImage.toString())

                val createRequestBody = RequestBody.create("multipart/form-data".toMediaTypeOrNull(), imageFile)

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
            Snackbar.make(rootView, uploadResponse.message+"", Snackbar.LENGTH_SHORT).show()
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
                    }
                }
                if (isNameChanged) {
                    collapsing_toolbar.title = uploadResponse.extra
                }
            }
        })

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
                .setNegativeButton("Cancel", DialogInterface.OnClickListener { dialogInterface, i ->  })
                .create()
        dialog.show()
    }

}