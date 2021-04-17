package com.salman.socialapp.ui.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.salman.socialapp.R
import com.salman.socialapp.adapters.MessageAdapter
import com.salman.socialapp.model.Chat
import com.salman.socialapp.network.BASE_URL
import com.salman.socialapp.util.Converter
import com.salman.socialapp.util.hide
import com.salman.socialapp.util.show
import com.salman.socialapp.util.showToast
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.activity_send_message.*
import java.io.InputStream

private const val TAG = "SendMessageActivity"
class SendMessageActivity : AppCompatActivity() {
    var currentUserId: String? = ""
    var userId: String? = ""
    var userName: String? = null
    var profileUrl: String? = null
    var token: String? = ""
    lateinit var chatList: MutableList<Chat>
    var firebaseUser: FirebaseUser? = null
    var reference: DatabaseReference? = null
    var seenEventListener: ValueEventListener? = null
    lateinit var messageAdapter: MessageAdapter
    var uri: Uri? = null
    var imageSent = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_send_message)

        // toolbar
        val mToolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(mToolbar)
        supportActionBar?.setTitle("")
        mToolbar.setNavigationIcon(R.drawable.ic_back)
        mToolbar.setNavigationOnClickListener {
            super.onBackPressed()
        }

        // intent
        intent?.let {

            getDataFromIntent(it)

            showUserData()
        }

        initialization()

        readMessages(currentUserId, userId)

        onSendBtnClicked()

        onImageButtonClicked()

        ItemTouchHelper(callback).attachToRecyclerView(recycler_view)

        text_send.addTextChangedListener(msgTextWatcher)

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.send_message_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.audio_call_menu -> {
                setupAudioCalling()
            }
            R.id.video_call_menu -> {
                setupVideoCalling()
            }
        }
        return true
    }

    private fun setupAudioCalling() {
        val mIntent = Intent(this, OutgoingInvitationActivity::class.java)
        mIntent.putExtra("userId", userId)
        mIntent.putExtra("username", userName)
        mIntent.putExtra("profileUrl", profileUrl)
        mIntent.putExtra("token", token)
        mIntent.putExtra("type", "audio")
        startActivity(mIntent)
    }

    private fun setupVideoCalling() {
        val mIntent = Intent(this, OutgoingInvitationActivity::class.java)
        mIntent.putExtra("userId", userId)
        mIntent.putExtra("username", userName)
        mIntent.putExtra("profileUrl", profileUrl)
        mIntent.putExtra("token", token)
        mIntent.putExtra("type", "video")
        startActivity(mIntent)
    }

    private fun getDataFromIntent(it: Intent) {
        if (it.hasExtra("userId") &&
            it.hasExtra("name") &&
            it.hasExtra("profileUrl") &&
            it.hasExtra("token")
        ) {
            userId = it.getStringExtra("userId")
            userName = it.getStringExtra("name")
            profileUrl = it.getStringExtra("profileUrl")
            token = it.getStringExtra("token")
            Log.d(TAG, "data from app")
        }
        else if (it.hasExtra("from_user_id") &&
            it.hasExtra("from_user_name") &&
            it.hasExtra("from_user_image")
        ) {
            userId = it.getStringExtra("from_user_id")
            userName = it.getStringExtra("from_user_name")
            profileUrl = it.getStringExtra("from_user_image")
            Log.d(TAG, "data from background")
        }
        else if (it.hasExtra("fromUserId") &&
            it.hasExtra("fromUserName") &&
            it.hasExtra("fromUserImage")
        ) {
            userId = it.getStringExtra("fromUserId")
            userName = it.getStringExtra("fromUserName")
            profileUrl = it.getStringExtra("fromUserImage")
            Log.d(TAG, "data from foreground")
        }
        else {
            Log.d(TAG, "data from nowhere")
        }
    }

    private fun onImageButtonClicked() {
        img_send.setOnClickListener {
            CropImage.startPickImageActivity(this)
        }
    }

    private fun onSendBtnClicked() {
        btn_send.setOnClickListener {
            val message = text_send.text.toString()
            if (!message.isEmpty()) {
                imageSent = false
                sendMessage(currentUserId, userId, message)
                text_send.setText("")
            } else {
                showToast("empty message can't be sent")
            }
        }
    }

    private fun showUserData() {
        val nameString = userName
        val nameArray: List<String> = nameString!!.split(" ")
        val firstName = nameArray[0]
        nameTV.text = firstName

        val userImage = if (Uri.parse(profileUrl).authority == null && !profileUrl!!.isEmpty()) {
            BASE_URL.plus(profileUrl)
        } else {
            profileUrl
        }
        Glide.with(this)
            .load(userImage)
            .into(profileIV)
    }

    private fun initialization() {
        chatList = ArrayList()
        // userid
        firebaseUser = FirebaseAuth.getInstance().currentUser
        currentUserId = firebaseUser?.uid

        // recyclerview
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.stackFromEnd = true
        recycler_view.setHasFixedSize(true)
        recycler_view.layoutManager = linearLayoutManager
    }

    private fun sendMessage(sender: String?, receiver: String?, message: String) {
        val chatRef = FirebaseDatabase.getInstance().getReference("chats")
        val id = chatRef.push().key
        val chat = Chat(id, sender, receiver, message, imageSent, false)
        chatRef.child(id!!).setValue(chat).addOnCompleteListener {
            recycler_view.scrollToPosition(chatList.size - 1)
        }

        // adding users to chat fragment, recent chats with friends
        val chatListRef = FirebaseDatabase.getInstance()
            .getReference("chatList")
            .child(currentUserId!!)
            .child(userId!!)

        chatListRef.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (!dataSnapshot.exists()) {
                    chatListRef.child("id").setValue(userId)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                showToast(error.message)
            }
        })

    }

    private fun readMessages(currentUserId: String?, userId: String?) {
        progress.show()
        val chatRef = FirebaseDatabase.getInstance().getReference("chats")
        chatRef.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                progress.hide()
                chatList.clear()
                for (snapshot in dataSnapshot.children) {
                    val chat = snapshot.getValue(Chat::class.java)
                    chat?.let {
                        if (it.receiver.equals(currentUserId) && it.sender.equals(userId) ||
                                it.receiver.equals(userId) && it.sender.equals(currentUserId)) {
                            chatList.add(it)
                        }
                    }
                }
                messageAdapter = MessageAdapter(this@SendMessageActivity, chatList, currentUserId!!, profileUrl!!)
                recycler_view.adapter = messageAdapter
//                messageAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                showToast(error.message)
            }
        })

    }

    private fun seenMessage(userId: String?) {
       reference = FirebaseDatabase.getInstance().getReference("chats")

        seenEventListener = reference!!.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (snapshot in dataSnapshot.children) {
                    val chat = snapshot.getValue(Chat::class.java)

                    if (chat?.receiver.equals(currentUserId) && chat?.sender.equals(userId)) {

                        val hashMap: HashMap<String, Any> = HashMap()
                        hashMap.put("seen", true)
                        snapshot.ref.updateChildren(hashMap)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                showToast(error.message)
            }
        })
    }

    private fun checkStatus(status: String) {
        reference = FirebaseDatabase.getInstance().getReference("fusers").child(currentUserId!!)

        val hashMap: HashMap<String, Any> = HashMap()
        hashMap.put("status", status)
        reference!!.updateChildren(hashMap)

    }

    private fun deleteChatFromFirebase(mChat: Chat) {
        val ref = FirebaseDatabase.getInstance().getReference("chats")
        ref.child(mChat.id!!).removeValue()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val imageUri = CropImage.getPickImageResultUri(this, data)
            if (CropImage.isReadExternalStoragePermissionsRequired(this, imageUri)) {
                uri = imageUri
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 0)
                }
            } else {
                startCropImage(imageUri)
            }
        } else if (resultCode == Activity.RESULT_CANCELED) {
            showToast("Something wrong !")
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result: CropImage.ActivityResult = CropImage.getActivityResult(data)
            if (resultCode == Activity.RESULT_OK) {
                sendImageMessage(result.uri)
            } else if (resultCode == Activity.RESULT_CANCELED) {
                showToast("Something wrong !")
            }
        }
    }

    private fun sendImageMessage(uri: Uri?) {
        if (uri != null) {
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            val image: Bitmap = BitmapFactory.decodeStream(inputStream)
            val message = Converter.BtimapToStringBase64(image)
            Log.d(TAG, "ImageMessage: " + message)
            Log.d(TAG, "ImageMessageLength: " + message.length)

            imageSent = true
            sendMessage(currentUserId, userId, message)
        }
    }

    private fun startCropImage(imageUri: Uri?) {
        CropImage.activity(imageUri)
            .setGuidelines(CropImageView.Guidelines.ON)
            .setMultiTouchEnabled(true)
            .start(this)
    }

    override fun onResume() {
        super.onResume()
        checkStatus("online")
        seenMessage(userId)
    }

    override fun onPause() {
        super.onPause()
        reference?.removeEventListener(seenEventListener!!)
        checkStatus("offline")

    }

    private val callback: ItemTouchHelper.SimpleCallback = object : ItemTouchHelper.SimpleCallback(
        0,
        ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            return false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val mChat = messageAdapter.getChatAt(viewHolder.adapterPosition)
            deleteChatFromFirebase(mChat)
        }
    }

    private val msgTextWatcher: TextWatcher = object : TextWatcher {

        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            val msgText = text_send.text.toString().trim()
            if (msgText.length >= 1) {
                img_send.visibility = View.INVISIBLE
                btn_send.visibility = View.VISIBLE
            } else {
                btn_send.visibility = View.INVISIBLE
                img_send.visibility = View.VISIBLE
            }
        }

        override fun afterTextChanged(p0: Editable?) {
        }
    }

}