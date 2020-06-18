package com.goodbits.eyeq.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.goodbits.eyeq.R
import kotlinx.android.synthetic.main.activity_gallery.*

class ActivityGallery : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery)

        btn_back.setOnClickListener {
            finish()
        }
    }
}