package com.salman.socialapp.util

import android.content.Context
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast

fun ProgressBar.show() {
    visibility = View.VISIBLE
}

fun ProgressBar.hide() {
    visibility = View.GONE
}

fun Context.showToast(message: String? =null, duration: Int = Toast.LENGTH_LONG) {
    Toast.makeText(this, message, duration).show()
}