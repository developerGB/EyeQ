package com.goodbits.eyeq.activities

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.goodbits.eyeq.R
import com.goodbits.eyeq.bleservice.UartCallback
import com.goodbits.eyeq.onBackPressed
import com.goodbits.eyeq.ui.camera.CameraBaseFragment

class MainActivity : AppCompatActivity(),UartCallback  {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        this.window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_main)


        // Request camera permissions
        if (allPermissionsGranted()) {
            bindUI()
        } else {
            ActivityCompat.requestPermissions(
                this,
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }

    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray
    ) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                bindUI()
            } else {
                Toast.makeText(
                    this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    private fun bindUI() {

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, CameraBaseFragment()).commitAllowingStateLoss()

//        startActivity(Intent(this, CameraBaseFragment::class.java))
    }

    private var doubleBackToExitPressedOnce = false

    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed()
            return
        }

        this.doubleBackToExitPressedOnce = true
        Toast.makeText(
            this, "Please click BACK again to exit App",
            Toast.LENGTH_LONG
        ).show()

        Handler().postDelayed(Runnable { doubleBackToExitPressedOnce = false }, 2000)
    }

    override fun handleException(action: String?) {
        Log.d("[EyeQ] Home screen", "Service Message = $action")
    }


}