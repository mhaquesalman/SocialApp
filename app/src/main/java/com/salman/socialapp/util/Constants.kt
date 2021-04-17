package com.salman.socialapp.util

class Constants {

    companion object {
        //Url
        const val MEET_URL = "https://meet.jit.si"

        // messaging feature constants
        const val FROM_USER_ID = "from_user_id"
        const val FROM_USER_NAME = "from_user_name"
        const val FROM_USER_IMAGE = "from_user_image"
        const val MESSAGE = "message"
        const val IS_IMAGE = "is_image"
        const val IS_FROM_NOTIFICATION = "isFromNotification"

        // calling feature constants
        const val KEY_USERNAME = "username"
        const val KEY_TOKEN = "token"
        const val MSG_TYPE = "type"
        const val MSG_INVITATION = "invitation"
        const val MSG_INVITATION_RESPONSE = "invitation_response"
        const val MSG_MEETING_TYPE = "meetingType"
        const val MSG_SENDER_TOKEN = "senderToken"
        const val MSG_DATA = "data"
        const val MSG_AUTHORIZATION = "Authorization"
        const val MSG_CONTENT_TYPE = "Content-Type"
        const val MSG_REGISTRATION_IDS = "registration_ids"
        const val MSG_INVITATION_ACCEPTED = "accepted"
        const val MSG_INVITATION_REJECTED = "rejected"
        const val MSG_INVITATION_CANCELLED = "cancelled"
        const val MSG_MEETING_ROOM = "meetingRoom"

        fun getRemoteMessageHeader(): HashMap<String, String> {
            val headers = HashMap<String, String>()
            headers.put(
                MSG_AUTHORIZATION,
                "key=AAAAUMM29TQ:APA91bGPzJiTC-u1z99CKdNuvtWjYr9vi-pWExpXNxsshV1gywQQMOu0N8VeHatsKjYDmtEsbwOxsqIUGHRWaUvgmRDuTJwNqZ_d83iwtylJYEQqbKlTzykA8QSgOpWMi8lea0VlGVNG"
            )
            headers.put(
                MSG_CONTENT_TYPE,
                "application/json"
            )
            return headers
        }

    }

}