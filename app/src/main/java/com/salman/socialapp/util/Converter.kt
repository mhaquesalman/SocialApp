package com.salman.socialapp.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream

class Converter {

    companion object {

        fun fromBitmapToString(bitmap: Bitmap): String {
            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 25, byteArrayOutputStream)
            val byteArray = byteArrayOutputStream.toByteArray()
            val byteArrayToString = String(byteArray, Charsets.UTF_8)
            return byteArrayToString
        }

        fun fromStringToBitmap(string: String): Bitmap {
            val stringTobyteArray = string.toByteArray(Charsets.UTF_8)
            val byteArrayToBitmap =
                BitmapFactory.decodeByteArray(stringTobyteArray, 0, stringTobyteArray.size)
            return byteArrayToBitmap
        }
    }
}