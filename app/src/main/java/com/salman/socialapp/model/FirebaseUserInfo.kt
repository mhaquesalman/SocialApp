package com.salman.socialapp.model

import android.os.Parcel
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize


class FirebaseUserInfo() : Parcelable {

    private var _id: String = ""
    var id: String
        get() = _id
        set(value) {
            _id = value
        }

    private var _token: String = ""
    var token: String
        get() =_token
        set(value) {
            _token = value
        }

    private var _status: String = ""
    var status: String
        get() {
            return _status
        }
        set(value) {
            _status = value
        }

    private var _name: String = ""
    var name: String
        get() {
            return _name
        }
        set(value) {
            _name = value
        }

    private var _image: String = ""
    var image: String
        get() {
            return _image
        }
        set(value) {
            _image = value
        }

    constructor(parcel: Parcel) : this() {
        _id = parcel.readString()!!
        _token = parcel.readString()!!
        _status = parcel.readString()!!
        _name = parcel.readString()!!
        _image = parcel.readString()!!
    }

    constructor(id: String, token: String, status: String, name: String, image: String) : this() {
        _id = id
        _token = token
        _status = status
        _name = name
        _image = image
    }

    override fun toString(): String {
        return "{FirebaseUserInfo: " +
                "id= $id, " +
                "token= $token, " +
                "status= $status" +
                "name= $name " +
                "image= $image}"
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(_id)
        parcel.writeString(_token)
        parcel.writeString(_status)
        parcel.writeString(_name)
        parcel.writeString(_image)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<FirebaseUserInfo> {
        override fun createFromParcel(parcel: Parcel): FirebaseUserInfo {
            return FirebaseUserInfo(parcel)
        }

        override fun newArray(size: Int): Array<FirebaseUserInfo?> {
            return arrayOfNulls(size)
        }
    }
}