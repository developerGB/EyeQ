package com.goodbits.eyeq.activities

import android.media.MediaPlayer
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.widget.MediaController
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.goodbits.eyeq.R
import kotlinx.android.synthetic.main.activity_gallery_details.*
import java.io.File


class GalleryDetailsActivity : AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_gallery_details)

        btn_back.setOnClickListener {
            onBackPressed()
        }

        if (intent.hasExtra("file_path")){

            val filePath = intent.getStringExtra("file_path")?:""

            val splits = filePath.split("/")

            if (splits.isNotEmpty())
                txt_header.text = splits[splits.size - 1]

            if (filePath.endsWith(".mp4")){
                showVideoFile(filePath)
            }
            else if (filePath.isNotEmpty()){
                showImageFile(filePath)
            }
            else showFileError()

        }
        else showFileError()

    }

    private fun showImageFile(path : String){

        val file = File(path)

        if (file.exists()) {

            Glide.with(this)
                .load(file)
                .placeholder(R.drawable.eyeq)
                .into(img_preview)
                .onLoadFailed(getDrawable(R.drawable.ic_baseline_broken_image_24))

            img_preview.visibility = View.VISIBLE
            lay_video_view.visibility = View.GONE
        }
        else showFileError()
    }

    private fun showVideoFile(path : String){

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
                val videoARWidth = 16f
                val videoARHeight = 10f

                val videoARRatio = videoARWidth / videoARHeight

                val params = video_view.layoutParams
                params.width = (it.videoWidth * videoARRatio).toInt()
                params.height = (it.videoHeight * videoARRatio).toInt()
                video_view.layoutParams = params

                it.start()
            }

        }
        else showFileError()
    }

    private fun showFileError(){
        Toast.makeText(this, "File not found", Toast.LENGTH_LONG).show()
    }

    override fun onBackPressed() {

        if (lay_video_view.visibility == View.VISIBLE && video_view.isPlaying){
            video_view.stopPlayback()
        }

        super.onBackPressed()
    }
}