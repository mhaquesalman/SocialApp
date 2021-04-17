package com.salman.socialapp.ui.activities

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.salman.socialapp.R
import com.salman.socialapp.model.FirebaseUserInfo
import com.salman.socialapp.model.UserInfo
import com.salman.socialapp.network.RemoteMessageApiService
import com.salman.socialapp.network.RemoteMessgeApiClient
import com.salman.socialapp.util.Constants
import com.salman.socialapp.util.Utils
import com.salman.socialapp.util.showToast
import kotlinx.android.synthetic.main.activity_outgoing_invitation.*
import org.jitsi.meet.sdk.JitsiMeetActivity
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber
import java.lang.StringBuilder
import java.lang.reflect.Type
import java.net.URL
import java.util.*
import kotlin.collections.ArrayList

private const val TAG = "OutgoingActivity"
class OutgoingInvitationActivity : AppCompatActivity() {
    private var senderToken: String? = ""
    private var receiverToken: String? = null
    private var meetingType: String? = null
    private var remoteMessageApiService: RemoteMessageApiService? = null
    private var userInfo: UserInfo? = null
    private var senderUsername: String? = null
    private var receiverUsername: String? = null
    private var senderUserId: String? = null
    private var meetingRoom: String? = null
    private var isMultiple: Boolean = false
    private var firebaseUserInfo: FirebaseUserInfo? = null
    private var receivers: MutableList<FirebaseUserInfo>? = null
    private var rejectionCount = 0
    private var totalReceivers = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_outgoing_invitation)

        remoteMessageApiService = RemoteMessgeApiClient.getRetrofit()?.create(RemoteMessageApiService::class.java)

        getDataFromSharedPref()

        intent?.let {
            getDataFromIntent(it)
        }

    }

    private fun getDataFromIntent(it: Intent) {
        when {
            it.hasExtra("user") && it.hasExtra("type") -> {
                firebaseUserInfo = it.getParcelableExtra("user")
                meetingType = it.getStringExtra("type")
                firebaseUserInfo?.let { user ->
                    receiverToken = user.token
                    receiverUsername = user.name
                }
                Log.d(TAG, "getDataFromIntent: $firebaseUserInfo")
            }
            it.hasExtra("isMultiple") && it.hasExtra("selectedUsers") && it.hasExtra("type") -> {
                isMultiple = it.getBooleanExtra("isMultiple", false)
                val type = object : TypeToken<MutableList<FirebaseUserInfo>>() {}.type
                val selectedUsers = it.getStringExtra("selectedUsers")
                receivers = Gson().fromJson<MutableList<FirebaseUserInfo>>(selectedUsers, type)
                meetingType = it.getStringExtra("type")
                Log.d(TAG, "getDataFromIntent: $isMultiple / $selectedUsers")

            }
            it.hasExtra("type") && it.hasExtra("token") && it.hasExtra("username") -> {
                meetingType = it.getStringExtra("type")
                receiverToken = it.getStringExtra("token")
                receiverUsername = it.getStringExtra("username")
                Log.d(TAG, "getDataFromIntent: $receiverUsername")
            }
            else -> {
                Log.d(TAG, "getDataFromIntent: from nowhere")
            }
        }
            if (meetingType.equals("video")) {
                imgMeetingType.setImageResource(R.drawable.ic_call_video)
            } else {
                imgMeetingType.setImageResource(R.drawable.ic_call_audio)
            }

            if (receiverUsername != null) {
                text_firstchar.text = receiverUsername!!.substring(0, 1)
                text_username.text = receiverUsername
            }

            if (meetingType != null && receiverToken != null) {
                totalReceivers = 1
                setupMeeting(meetingType!!, receiverToken!!, null)
            } else if (meetingType != null && receivers != null) {
                totalReceivers = receivers!!.size
                setupMeeting(meetingType!!, null, receivers!!.toList())
            }


        img_cancel_btn.setOnClickListener {
            if (receiverToken != null && !isMultiple) {
                cancelInvitation(receiverToken!!,null)
            } else if (receivers != null && isMultiple) {
                cancelInvitation(null, receivers!!.toList())
            }
        }
    }

    private fun getDataFromSharedPref() {
        Utils(this).apply {
            userInfo = getUserFromSharedPref()
            userInfo?.let {
                senderUsername = it.name
                senderToken = it.userToken
                senderUserId = it.uid
            }
        }
    }

    private fun setupMeeting(meetingType: String, receiverToken: String?, receivers:List<FirebaseUserInfo>?) {

        /*
        * Body {
        * "data": {
        * "type": "invitation",
        * "meetingType": "video
        * },
        * "registration_ids": ["receiver_token"]
        * }
        * */

        try {
            val tokens = JSONArray()
            if (receiverToken != null)
                tokens.put(receiverToken)

            if (receivers != null && receivers.size > 0) {
                val userNames = StringBuilder()
                for (i in receivers.indices) {
                    tokens.put(receivers[i].token)
                    userNames.append(receivers[i].name).append("\n")
                }
                text_firstchar.visibility = View.VISIBLE
                text_username.setText(userNames.toString())
            }

            val body = JSONObject()
            val data = JSONObject()

            meetingRoom = senderUserId + "_" + UUID.randomUUID().toString().substring(0, 3)

            data.apply {
                put(Constants.MSG_TYPE, Constants.MSG_INVITATION)
                put(Constants.MSG_MEETING_TYPE, meetingType)
                put(Constants.KEY_USERNAME, senderUsername)
                put(Constants.MSG_SENDER_TOKEN, senderToken)
                put(Constants.MSG_MEETING_ROOM, meetingRoom)
            }

            body.apply {
                put(Constants.MSG_DATA, data)
                put(Constants.MSG_REGISTRATION_IDS, tokens)
            }

            sendRemoteMessage(body.toString(), Constants.MSG_INVITATION)

        } catch (e: Exception) {
            showToast(e.message)
        }
    }

    private fun cancelInvitation(receiverToken: String?, receivers: List<FirebaseUserInfo>?) {
        try {
            val tokens = JSONArray()
            if (receiverToken != null)
                tokens.put(receiverToken)

            if (receivers != null && receivers.size > 0) {
                for (user in receivers) {
                    tokens.put(user.token)
                }
            }

            val body = JSONObject()
            val data = JSONObject()

            data.apply {
                put(Constants.MSG_INVITATION_RESPONSE, Constants.MSG_INVITATION_CANCELLED)
                put(Constants.MSG_TYPE, Constants.MSG_INVITATION_RESPONSE)
            }

            body.apply {
                put(Constants.MSG_DATA, data)
                put(Constants.MSG_REGISTRATION_IDS, tokens)
            }

            sendRemoteMessage(body.toString(), Constants.MSG_INVITATION_RESPONSE)

        } catch (e: Exception) {
            showToast(e.message)
        }
    }

    private fun sendRemoteMessage(messageBody: String, type: String) {
        remoteMessageApiService?.sendRemoteMessage(
            Constants.getRemoteMessageHeader(),
            messageBody
        )?.enqueue(object : Callback<String> {

            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.isSuccessful) {
                    if (type.equals(Constants.MSG_INVITATION)) {
                        showToast("Invitation Sent")
                    } else if (type.equals(Constants.MSG_INVITATION_RESPONSE)) {
                        showToast("Invitation Cancelled")
                        finish()
                    }
                } else {
                    showToast(response.message())
                    finish()
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                showToast(t.message)
                finish()
            }
        })
    }

    private val invitationResponseReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            val type = intent?.getStringExtra(Constants.MSG_INVITATION_RESPONSE)
            type?.let {
                if (it.equals(Constants.MSG_INVITATION_ACCEPTED)) {
                    showToast("invitation accepted")

                    try {
                        val serverUrl = URL(Constants.MEET_URL)

                        val builder: JitsiMeetConferenceOptions.Builder = JitsiMeetConferenceOptions.Builder()
                        builder.setServerURL(serverUrl)
                        builder.setRoom(meetingRoom)
                        if (meetingType.equals("audio")) {
                            builder.setVideoMuted(true)
                        }

/*                        val conferenceOptions: JitsiMeetConferenceOptions = JitsiMeetConferenceOptions
                            .Builder()
                            .setServerURL(serverUrl)
                            .setRoom(meetingRoom)
                            .build()*/

                        JitsiMeetActivity.launch(this@OutgoingInvitationActivity, builder.build())
                        finish()
                    } catch (e: Exception) {
                        showToast(e.message)
                        finish()
                    }

                } else if (it.equals(Constants.MSG_INVITATION_REJECTED)) {
                    rejectionCount += 1
                    if (rejectionCount == totalReceivers) {
                        showToast("invitation rejected")
                        finish()
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()

        LocalBroadcastManager.getInstance(applicationContext).registerReceiver(
            invitationResponseReceiver,
            IntentFilter(Constants.MSG_INVITATION_RESPONSE)
        )
    }

    override fun onStop() {
        super.onStop()

        LocalBroadcastManager.getInstance(applicationContext).unregisterReceiver(invitationResponseReceiver)
    }

}