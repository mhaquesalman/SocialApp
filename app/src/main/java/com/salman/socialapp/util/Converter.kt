package com.salman.socialapp.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import java.io.ByteArrayOutputStream

class Converter {

    companion object {

        fun BitmapToString(bitmap: Bitmap): String {
            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream)
            val byteArray = byteArrayOutputStream.toByteArray()
            val string = String(byteArrayOutputStream.toByteArray(), Charsets.UTF_8)
            return string
        }

        fun StringToBitmap(string: String): Bitmap {
            val byteArray = string.toByteArray(Charsets.UTF_8)
            val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
            return bitmap
        }

        fun BtimapToStringBase64(bitmap: Bitmap): String {
            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 25, byteArrayOutputStream)
            val byteArray = byteArrayOutputStream.toByteArray()
            val base64String = Base64.encodeToString(byteArray, Base64.DEFAULT)
            return base64String
        }

        fun StringBase64ToBitmap(string: String): Bitmap {
            val byteArray = Base64.decode(string, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
            return bitmap
        }

        fun BitmapToByteArray(bitmap: Bitmap): ByteArray {
            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream)
            val byteArray = byteArrayOutputStream.toByteArray()
            return byteArray
        }

        fun ByteArrayToBitmap(byteArray: ByteArray): Bitmap {
            val bitmap = BitmapFactory.decodeByteArray(byteArray, 0 , byteArray.size)
            return bitmap
        }
    }
}