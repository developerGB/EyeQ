package com.goodbits.eyeq.ui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import com.goodbits.eyeq.R
import kotlinx.android.synthetic.main.activity_form.*

class FormActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_form)

        val myDeviceModel = Build.MODEL
        val os = android.os.Build.VERSION.RELEASE

        model.append(" $myDeviceModel")
        os_version.append(" $os")

        skip.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        submit.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

    }
}