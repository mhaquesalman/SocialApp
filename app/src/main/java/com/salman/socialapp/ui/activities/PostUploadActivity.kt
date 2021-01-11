package com.salman.socialapp.ui.activities

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.esafirm.imagepicker.features.ImagePicker
import com.google.firebase.auth.FirebaseAuth
import com.salman.socialapp.R
import com.salman.socialapp.util.showToast
import com.salman.socialapp.viewmodels.PostUploadViewModel
import com.salman.socialapp.viewmodels.ViewModelFactory
import id.zelory.compressor.Compressor
import kotlinx.android.synthetic.main.activity_post_upload.*
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.IOException

class PostUploadActivity : AppCompatActivity() {

    lateinit var postUploadViewModel: PostUploadViewModel
    var privacyLevel = 0
    var isImageSelected = false
    var compressedImageFile: File? = null
    lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_upload)

        initialization()

        setSupportActionBar(toolbar)
        toolbar.setNavigationIcon(R.drawable.ic_back)
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        spinner_privacy.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                val selectedTextView = view as TextView
                selectedTextView.setTextColor(Color.WHITE)
                selectedTextView.setTypeface(null, Typeface.BOLD)
                privacyLevel = pos
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                privacyLevel = 0
            }
        }

        post_btn.setOnClickListener {
            addPost()
        }

        add_imgBtn.setOnClickListener {
            selectImage()
        }

        imagePreview.setOnClickListener {
            selectImage()
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
            showToast("Status can't be empty !", Toast.LENGTH_SHORT)
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
                isImageSelected = true
                add_imgBtn.visibility = View.GONE
                imagePreview.visibility = View.VISIBLE

                Glide.with(this)
                    .load(selectedImage.path)
                    .error(R.drawable.cover_picture_placeholder)
                    .placeholder(R.drawable.cover_picture_placeholder)
                    .into(imagePreview)
            } catch (e : IOException) {
                add_imgBtn.visibility = View.VISIBLE
                imagePreview.visibility = View.GONE
            }

        }
    }
}