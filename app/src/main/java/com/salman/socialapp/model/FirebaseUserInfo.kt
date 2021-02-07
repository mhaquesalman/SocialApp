package com.salman.socialapp.model

class FirebaseUserInfo {

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

    constructor(id: String, token: String, status: String, name: String, image: String) {
        _id = id
        _token = token
        _status = status
        _name = name
        _image = image
    }

    constructor()

    override fun toString(): String {
        return "{FirebaseUserInfo: " +
                "id= $id, " +
                "token= $token, " +
                "status= $status" +
                "name= $name " +
                "image= $image}"
    }
}