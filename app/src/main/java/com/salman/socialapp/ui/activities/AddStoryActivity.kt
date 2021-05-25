package com.salman.socialapp.ui.activities

import android.app.ProgressDialog
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.webkit.MimeTypeMap
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.salman.socialapp.model.Story
import com.salman.socialapp.util.Utils
import com.salman.socialapp.util.showToast
import com.theartofdev.edmodo.cropper.CropImage
import java.io.File

const val ONE_DAY_INTERVAL = 86400000
class AddStoryActivity : AppCompatActivity() {
    lateinit var storageReference: StorageReference
    var imageUri: Uri? = null
    var imageUrl: String? = null
    var userId: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        storageReference = FirebaseStorage.getInstance().getReference("Story")
        val utils = Utils(this)
        utils.getUserFromSharedPref().also { user ->
            if (user != null) {
                userId = user.uid
            }
        }

        CropImage.activity()
            .setAspectRatio(9, 16)
            .start(this)
    }

    private fun getFileExtension(uri: Uri): String {
        var extension: String?
        val cr = contentResolver
        val mimeTypeMap = MimeTypeMap.getSingleton()
        extension = mimeTypeMap.getExtensionFromMimeType(cr.getType(uri))
        val filePath = File(uri.path).absolutePath
        return if (extension == null) {
            extension = filePath.substring(filePath.lastIndexOf("."))
            extension
        } else {
            extension
        }
    }

    private fun publishStory() {
        val pd = ProgressDialog(this)
        pd.setMessage("Posting Story...")
        pd.show()

        if (imageUri != null) {
            val imageRef = storageReference.
            child( "${System.currentTimeMillis()}.${getFileExtension(imageUri!!)}")

            val storageTask = imageRef.putFile(imageUri!!)
            val urlTask = storageTask.continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let { exception ->
                        throw exception
                    }
                }
                imageRef.downloadUrl
            }.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUri = task.result
                    imageUrl = downloadUri.toString()

                    val reference = FirebaseDatabase.getInstance()
                        .getReference("Story").child(userId!!)

                    val storyId = reference.push().key
                    val timeEnd = System.currentTimeMillis() + ONE_DAY_INTERVAL // 1 day

                    val hashmap = HashMap<String, Any>()
                    hashmap["storyImage"] = imageUrl!!
                    hashmap["timeStart"] = ServerValue.TIMESTAMP
                    hashmap["timeEnd"] = timeEnd
                    hashmap["storyid"] = storyId!!
                    hashmap["userid"] = userId!!

                    reference.child(storyId).setValue(hashmap)
                    pd.dismiss()
                    onStoryAddedAction?.let {  onStoryAdded ->
                        onStoryAdded()
                    }
                    finish()
                } else {
                    showToast("Failed")
                    pd.dismiss()
                }
            }.addOnFailureListener {
                showToast(it.message)
                pd.dismiss()
            }
        } else {
            showToast("No image selected")
            pd.dismiss()
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            val result = CropImage.getActivityResult(data)
            imageUri = result.uri

            publishStory()
        } else {
            showToast("Something wrong")
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

    }

    companion object {
        private var onStoryAddedAction: (() -> Unit)? = null
        fun setOnStoryAddedAction(onStoryAddedAction: () -> Unit) {
            this.onStoryAddedAction = onStoryAddedAction
        }
    }

}