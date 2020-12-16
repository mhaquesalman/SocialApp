package com.salman.socialapp.ui.activities

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import com.salman.socialapp.R
import com.salman.socialapp.network.BASE_URL
import kotlinx.android.synthetic.main.activity_view_image.*

class ViewImageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_image)

        intent?.let { intent ->
            var image = intent.getStringExtra("imageUrl")
            val imageUri = Uri.parse(image)
            if (imageUri.authority == null) {
                image = BASE_URL + image
            }
            Glide.with(this).load(image).into(photoView)
        }
    }
}