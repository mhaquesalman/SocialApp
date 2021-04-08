package com.salman.socialapp.util

import android.os.AsyncTask
import java.net.InetSocketAddress
import java.net.Socket

class InternetConnection : AsyncTask<Void, Void, Boolean> {

//    var consumer: Consumer? = null
    var action: ((Boolean) -> Unit)? = null

/*    constructor(consumer: Consumer) {
        this.consumer = consumer
        execute()
    }*/

    constructor(action: (Boolean) -> Unit) {
        this.action = action
        execute()
    }

    override fun doInBackground(vararg args: Void?): Boolean {
        return try {
            val socket = Socket()
            val socketAddress = InetSocketAddress(ADDRESS, PORT)
            socket.connect(socketAddress, TIMEOUT)
            socket.close()
            true
        } catch (e: Exception) {
            false
        }
    }

    override fun onPostExecute(result: Boolean) {
        super.onPostExecute(result)
//        consumer?.accept(result)
        action?.let {
            it(result)
        }
    }


/*    interface Consumer {
        fun accept(accept: Boolean)
    }*/

}