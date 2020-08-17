package com.goodbits.eyeq.activities

import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.MediaController
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ShareCompat
import androidx.core.content.FileProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.bumptech.glide.Glide
import com.goodbits.eyeq.R
import kotlinx.android.synthetic.main.activity_gallery_details.*
import java.io.File


class GalleryDetailsActivity : AppCompatActivity() {

    var filePath: String = ""
    var fileType: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        this.window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_gallery_details)

        btn_back.setOnClickListener {
            onBackPressed()
        }

        if (intent.hasExtra("file_path")) {

            filePath = intent.getStringExtra("file_path") ?: ""
            Log.d("filepath", filePath)

            val splits = filePath.split("/")

            if (splits.isNotEmpty()) {
                txt_header.text = splits[splits.size - 1]

                btn_delete.setOnClickListener {
                    deleteFile(File(filePath), splits[splits.size - 1])
                }

                if (filePath.endsWith(".mp4")) {
                    fileType = "video/*"
                    showVideoFile(filePath)
                } else if (filePath.isNotEmpty()) {
//                    setUpImageViewPinchToZoom()
                    fileType = "image/*"
                    showImageFile(filePath)
                } else showFileError()
            } else showFileError()
        } else {

            btn_delete.setOnClickListener {
                showFileError()
            }

            showFileError()
        }

        btn_share.setOnClickListener {

            val mediaUri: Uri = FileProvider.getUriForFile(
                this,
                this.applicationContext.packageName + ".provider",
                File(filePath)
            )

            Log.d("mediaUri", mediaUri.toString())

            val share = ShareCompat.IntentBuilder.from(this)
                .setStream(mediaUri) // uri from FileProvider
                .setType("text/html")
                .intent
                .setAction(Intent.ACTION_SEND) //Change if needed
                .setDataAndType(mediaUri, fileType)
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

            startActivity(Intent.createChooser(share, "Share"))

        }
    }

    private fun showImageFile(path: String) {

        val file = File(path)

        if (file.exists()) {

            Glide.with(this)
                .load(file)
                .placeholder(R.drawable.eyeq)
                .into(img_preview)
                .onLoadFailed(getDrawable(R.drawable.ic_baseline_broken_image_24))

            img_preview.visibility = View.VISIBLE
            lay_video_view.visibility = View.GONE
        } else showFileError()
    }

    private fun showVideoFile(path: String) {

        val file = File(path)

        if (file.exists()) {

            img_preview.visibility = View.GONE
            lay_video_view.visibility = View.VISIBLE

            val mediaController = MediaController(this)
            mediaController.setAnchorView(video_view)

//            val uri: Uri = Uri.parse(path)

            video_view.setMediaController(mediaController)
            video_view.setVideoPath(path)
            video_view.requestFocus()

            video_view.setOnPreparedListener {
                it.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT)
                it.setScreenOnWhilePlaying(true)

                //resizing video player
                val videoARWidth = 3f
                val videoARHeight = 2f

                val videoARRatio = videoARWidth / videoARHeight

                val videoARWidthRatio = video_view.width * videoARRatio
                val videoARHeightRatio = video_view.height * videoARRatio

                val params = video_view.layoutParams
                params.width = (it.videoWidth * videoARWidthRatio).toInt()
                params.height = (it.videoHeight * videoARHeightRatio).toInt()
                video_view.layoutParams = params

                it.start()
            }

        } else showFileError()
    }

    private fun showFileError() {
        Toast.makeText(this, "File not found", Toast.LENGTH_LONG).show()
    }

    override fun onBackPressed() {

        if (lay_video_view.visibility == View.VISIBLE && video_view.isPlaying) {
            video_view.stopPlayback()
        }

        super.onBackPressed()
    }

    private fun deleteFile(file: File, name: String) {

        AlertDialog.Builder(this)
            .setTitle("Alert")
            .setMessage("Are you sure!\nDo you want to delete $name ?")
            .setPositiveButton("Yes") { dialog, which ->

                if (file.exists()) {
                    file.delete()
                    LocalBroadcastManager.getInstance(this)
                        .sendBroadcast(Intent(GalleryActivity.FILE_DELETED))
                    Toast.makeText(this, "File Deleted", Toast.LENGTH_LONG).show()
                    finish()
                } else
                    showFileError()

            }
            .setNegativeButton("No") { dialog, which ->

            }
            .show()
    }

    //pinch to zoom for normal image view.
//    private fun setUpImageViewPinchToZoom(){
//
//        val listener = object : ScaleGestureDetector.SimpleOnScaleGestureListener(){
//            var mScaleFactor = 1.0f
//            override fun onScale(detector: ScaleGestureDetector?): Boolean {
//                mScaleFactor *= detector?.scaleFactor ?: 1.0F
//                mScaleFactor = 0.1f.coerceAtLeast(mScaleFactor.coerceAtMost(10.0f));
//                img_preview.scaleX = mScaleFactor
//                img_preview.scaleY = mScaleFactor
//                return true
//            }
//        }
//
//        val scaleGestureDetector = ScaleGestureDetector(this, listener)
//
//        img_preview.setOnTouchListener { _, event ->
//            scaleGestureDetector.onTouchEvent(event)
//            return@setOnTouchListener true
//        }
//
//    }
}