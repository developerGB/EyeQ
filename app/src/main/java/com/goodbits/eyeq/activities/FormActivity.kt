package com.goodbits.eyeq.activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.goodbits.eyeq.AppPreference
import com.goodbits.eyeq.R
import kotlinx.android.synthetic.main.activity_form.*

class FormActivity : AppCompatActivity() {

    internal lateinit var session: AppPreference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_form)

        val myDeviceModel = Build.MODEL
        val os = android.os.Build.VERSION.RELEASE

        model.append(" $myDeviceModel")
        os_version.append(" $os")

        session = AppPreference(applicationContext)
        session.isUserLoggedIn()


//        skip.setOnClickListener {
//            session.isFormShown(false)
//            startActivity(Intent(this, MainActivity::class.java))
//            finish()
////            throw RuntimeException("Test Crash") // Force a crash
//        }

        submit.setOnClickListener {
            if (!checkbox.isChecked) {
                Toast.makeText(
                    this@FormActivity, "Please read and agree to the Privacy policy and continue",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                session.isFormShown(true)
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }

    }
}