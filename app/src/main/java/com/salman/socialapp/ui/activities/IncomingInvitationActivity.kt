package com.salman.socialapp.ui.activities

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.salman.socialapp.R
import com.salman.socialapp.network.RemoteMessageApiService
import com.salman.socialapp.network.RemoteMessgeApiClient
import com.salman.socialapp.util.Constants
import com.salman.socialapp.util.showToast
import kotlinx.android.synthetic.main.activity_incoming_invitation.*
import org.jitsi.meet.sdk.JitsiMeetActivity
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.URL

class IncomingInvitationActivity : AppCompatActivity() {
    private var remoteMessageApiService: RemoteMessageApiService? = null
    private var meetingType: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_incoming_invitation)

        remoteMessageApiService = RemoteMessgeApiClient.getRetrofit()?.create(RemoteMessageApiService::class.java)

        intent?.let {
            getDataFromIntent(it)
        }

    }

    private fun getDataFromIntent(it: Intent) {
        meetingType = it.getStringExtra(Constants.MSG_MEETING_TYPE)
        if (meetingType != null) {
            if (meetingType.equals("video")) {
                imgMeetingType.setImageResource(R.drawable.ic_call_video)
            } else {
                imgMeetingType.setImageResource(R.drawable.ic_call_audio)
            }
        }
        val userName = it.getStringExtra(Constants.KEY_USERNAME)
        if (userName != null) {
            text_firstchar.text = userName.substring(0, 1)
            text_username.text = userName
        }

        val senderToken = it.getStringExtra(Constants.MSG_SENDER_TOKEN)
        img_accept_btn.setOnClickListener {
            if (senderToken != null) {
                sendInvitationResponse(Constants.MSG_INVITATION_ACCEPTED, senderToken)
            }
        }
        img_reject_btn.setOnClickListener {
            if (senderToken != null) {
                sendInvitationResponse(Constants.MSG_INVITATION_REJECTED, senderToken)
            }
        }
    }

    private fun sendInvitationResponse(type: String, receiverToken: String) {
        try {
            val tokens = JSONArray()
            tokens.put(receiverToken)

            val body = JSONObject()
            val data = JSONObject()

            data.apply {
                put(Constants.MSG_INVITATION_RESPONSE, type)
                put(Constants.MSG_TYPE, Constants.MSG_INVITATION_RESPONSE)
            }

            body.apply {
                put(Constants.MSG_DATA, data)
                put(Constants.MSG_REGISTRATION_IDS, tokens)
            }

            sendRemoteMessage(body.toString(), type)

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
                    if (type.equals(Constants.MSG_INVITATION_ACCEPTED)) {
                        showToast("invitation accepted")
                        try {
                            val meetingRoom = intent?.getStringExtra(Constants.MSG_MEETING_ROOM)
                            val serverUrl = URL(Constants.MEET_URL)

                            val builder: JitsiMeetConferenceOptions.Builder = JitsiMeetConferenceOptions.Builder()
                            builder.setServerURL(serverUrl)
                            builder.setRoom(meetingRoom)
                            if (meetingType.equals("audio")) {
                                builder.setVideoMuted(true)
                            }

/*                            val conferenceOptions: JitsiMeetConferenceOptions = JitsiMeetConferenceOptions
                                .Builder()
                                .setServerURL(serverUrl)
                                .setRoom(meetingRoom)
                                .build()*/

                            JitsiMeetActivity.launch(this@IncomingInvitationActivity, builder.build())
                            finish()

                        } catch (e: Exception) {
                            showToast(e.message)
                            finish()
                        }

                    } else if (type.equals(Constants.MSG_INVITATION_REJECTED)) {
                        showToast("invitation rejected")
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
                if (it.equals(Constants.MSG_INVITATION_CANCELLED)) {
                    showToast("invitation cancelled")
                    finish()
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