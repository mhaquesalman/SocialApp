package com.salman.socialapp.ui.activities

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.esafirm.imagepicker.features.ImagePicker
import com.esafirm.imagepicker.model.Image
import com.google.firebase.auth.FirebaseAuth
import com.salman.socialapp.R
import com.salman.socialapp.network.BASE_URL
import com.salman.socialapp.util.showToast
import com.salman.socialapp.viewmodels.PostUploadViewModel
import com.salman.socialapp.viewmodels.ViewModelFactory
import id.zelory.compressor.Compressor
import kotlinx.android.synthetic.main.activity_post_upload.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.io.IOException

private const val TAG = "PostUploadActivity"
class PostUploadActivity : AppCompatActivity() {

    lateinit var postUploadViewModel: PostUploadViewModel
    var privacyLevel = 0
    var isImageSelected = false
    var compressedImageFile: File? = null
    var editPost = false
    var profileUrl: String? = ""
    var postId = 0
    var postTitle: String? = ""
    var postImage: String? = ""
    var postPrivacy = 0
    lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_upload)

        initialization()

        setSupportActionBar(toolbar)
        toolbar.setNavigationIcon(R.drawable.ic_back)
        toolbar.setNavigationOnClickListener {
            super.onBackPressed()
        }


        // getting post data from intent
        intent?.let {
//            val mExtras = it.extras
            editPost = it.getBooleanExtra("editPost", false)
            profileUrl = it.getStringExtra("profileUrl")
            if (it.hasExtra("postId") && it.hasExtra("postTitle") &&
                it.hasExtra("postImage") && it.hasExtra("postPrivacy")) {
                postId = it.getIntExtra("postId", 0)
                postTitle = it.getStringExtra("postTitle")
                postImage = it.getStringExtra("postImage")
                postPrivacy = it.getIntExtra("postPrivacy", 0)
            }
        }

        if (!profileUrl!!.isEmpty()) {
            if (profileUrl!!.contains("../")) {
                Glide.with(this).load(BASE_URL + profileUrl).into(profile_image)
            } else {
                Glide.with(this).load(profileUrl).into(profile_image)
            }
        }

        if (editPost) {
            post_btn.setText("update")
            // display existing data
            showToBeUpdatedData()
        }

        spinner_privacy.setSelection(postPrivacy)
        spinner_privacy.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedTextView = view as TextView
                selectedTextView.setTextColor(Color.WHITE)
                selectedTextView.setTypeface(null, Typeface.BOLD)
                privacyLevel = position
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                privacyLevel = 0
            }
        }

        post_btn.setOnClickListener {
            if (!editPost) {
                addPost()
            } else {
                updatePost()
            }
        }

        add_imgBtn.setOnClickListener {
            selectImage()
        }

        imagePreview.setOnClickListener {
            selectImage()
        }
    }

    private fun showToBeUpdatedData() {
        input_txt.setText(postTitle)

        if (!postImage!!.isEmpty()) {
            add_imgBtn.visibility = View.GONE
            imagePreview.visibility = View.VISIBLE
            update_imgBtn.visibility = View.VISIBLE
            delete_imgBtn.visibility = View.VISIBLE
            if (Uri.parse(postImage).authority == null && postImage!!.contains("../")) {
                Glide.with(this).load(BASE_URL + postImage).into(imagePreview)
            } else {
                Glide.with(this).load(postImage).into(imagePreview)
            }

            delete_imgBtn.setOnClickListener {
                postImage = ""
                isImageSelected = false
                it.visibility = View.GONE
                imagePreview.visibility = View.GONE
                add_imgBtn.visibility = View.VISIBLE
//                imagePreview.setImageResource(R.drawable.cover_picture_placeholder)
            }

            update_imgBtn.setOnClickListener {
                selectImage()
            }
        } else {
            add_imgBtn.visibility = View.VISIBLE
            imagePreview.visibility = View.GONE
            update_imgBtn.visibility = View.GONE
            delete_imgBtn.visibility = View.GONE

            add_imgBtn.setOnClickListener {
                selectImage()
            }
        }
    }

    private fun initialization() {
        progressDialog = ProgressDialog(this)
        progressDialog.setCancelable(false)
        progressDialog.setTitle("Loading...")
        progressDialog.setMessage("Uploading post...please wait")

        postUploadViewModel = ViewModelProvider(this, ViewModelFactory()).get(PostUploadViewModel::class.java)
    }

    private fun addPost() {
        val status = input_txt.text.toString()
        val userId = FirebaseAuth.getInstance().uid.toString()
        if (!status.isEmpty() || isImageSelected) {
            progressDialog.show()
            val builder = MultipartBody.Builder().apply {
                setType(MultipartBody.FORM)
                addFormDataPart("post", status)
                addFormDataPart("postUserId", userId)
                addFormDataPart("privacy", privacyLevel.toString())
                if (isImageSelected && compressedImageFile != null) {

                    val createRequestBody =
                        RequestBody.create("multipart/form-data".toMediaTypeOrNull(), compressedImageFile!!)

//                    alternative to RequestBody.create() as create() method is depricated
//                    val createRequestBody =
//                        compressedImageFile!!.asRequestBody("multipart/form-data".toMediaTypeOrNull())

                    addFormDataPart("file", compressedImageFile?.name, createRequestBody)
                }
            }
            val multipartBody = builder.build()
            postUploadViewModel.uploadPost(multipartBody, false)?.observe(this, Observer { postUploadResponse ->
                progressDialog.hide()
                showToast(postUploadResponse.message)
                if (postUploadResponse.status == 200) {
//                    startActivity(Intent(this, MainActivity::class.java))
//                    finish()
                    onBackPressed()
                }
            })
        } else {
            showToast("Post can't be empty !", Toast.LENGTH_SHORT)
        }
    }

    private fun updatePost() {
        val status = input_txt.text.toString()
        if (!status.isEmpty() || isImageSelected || !postImage!!.isEmpty()) {
            progressDialog.show()
            val builder = MultipartBody.Builder().apply {
                setType(MultipartBody.FORM)
                addFormDataPart("postId", postId.toString())
                addFormDataPart("post", status)
                addFormDataPart("privacy", privacyLevel.toString())
                if (isImageSelected && compressedImageFile != null) {

                    val createRequestBody =
                        RequestBody.create("multipart/form-data".toMediaTypeOrNull(), compressedImageFile!!)

//                    alternative to RequestBody.create() as create() method is depricated
//                    val createRequestBody =
//                        compressedImageFile!!.asRequestBody("multipart/form-data".toMediaTypeOrNull())

                    addFormDataPart("file", compressedImageFile?.name, createRequestBody)

                } else if (!isImageSelected && postImage.equals("") || postImage!!.length == 0) {
                    addFormDataPart("postImage", postImage!!)
                } else if (!isImageSelected && !postImage!!.isEmpty()) {
                    addFormDataPart("postImage", postImage!!)
                }
            }
            val multipartBody = builder.build()
            postUploadViewModel.updatePost(multipartBody)?.observe(this, Observer { postUpdateResponse ->
                showToast(postUpdateResponse.message)
                if (postUpdateResponse.status == 200) {
                    onBackPressed()
                }
            })
        } else {
            showToast("Status can't be empty !", Toast.LENGTH_SHORT)
        }
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
                compressedImageFile = Compressor(this).setQuality(80).compressToFile(File(selectedImage.path))
                isImageSelected = true
                add_imgBtn.visibility = View.GONE
                update_imgBtn.visibility = View.GONE
                delete_imgBtn.visibility = View.GONE
                imagePreview.visibility = View.VISIBLE

                Glide.with(this)
                    .load(selectedImage.path)
                    .error(R.drawable.cover_picture_placeholder)
                    .placeholder(R.drawable.cover_picture_placeholder)
                    .into(imagePreview)
            } catch (e: IOException) {
                add_imgBtn.visibility = if (update_imgBtn.isVisible) View.GONE else View.VISIBLE
                update_imgBtn.visibility = if (add_imgBtn.isVisible) View.GONE else View.VISIBLE
                imagePreview.visibility = View.GONE
                Toast.makeText(this, "Image Picker Failed !", Toast.LENGTH_SHORT).show()
            }
        }
    }
}