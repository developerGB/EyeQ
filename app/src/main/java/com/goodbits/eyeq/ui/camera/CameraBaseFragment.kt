package com.goodbits.eyeq.ui.camera

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.VideoCapture
import androidx.camera.view.CameraView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.goodbits.eyeq.R
import kotlinx.android.synthetic.main.fragment_camera.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

const val DEFAULT_ZOOM = 1f

class CameraBaseFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_camera,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        startCamera()

        seek_zoom.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser)
                    setZoom(progress.toFloat() + 1)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }
        })

        btn_take_pic.setOnClickListener {
            takePhoto()
        }

        btn_record.setOnClickListener {
            startVideoRecord()
        }

    }

//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        setContentView(R.layout.fragment_camera)
//
//    }
    
    private fun setZoom(zm:Float){
        cam.zoomRatio = zm
        txt_seek.text = "${zm}x"
    }

    private fun startCamera() {

        if (ActivityCompat.checkSelfPermission(
                context!!,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e(TAG,"Camera permission missing")
            return
        }

        cam.bindToLifecycle(this)

        cam.isPinchToZoomEnabled = false

        setZoom(DEFAULT_ZOOM)
    }

    private fun takePhoto(){

        if (!cam.isRecording) {
            if (cam.captureMode != CameraView.CaptureMode.IMAGE) {
                cam.captureMode = CameraView.CaptureMode.IMAGE

                if (ActivityCompat.checkSelfPermission(
                        context!!,
                        Manifest.permission.CAMERA
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    Log.e(TAG,"Camera permission missing")
                    return
                }
                cam.bindToLifecycle(this)
            }

            // Create timestamped output file to hold the image
            val photoFile = File(
                getOutputDirectory(getString(R.string.image)),
                SimpleDateFormat(
                    FILENAME_FORMAT, Locale.US
                ).format(System.currentTimeMillis()) + ".jpg"
            )

            // Setup image capture listener which is triggered after photo has
            // been taken
            cam.takePicture(
                photoFile,
                ContextCompat.getMainExecutor(context),
                object : ImageCapture.OnImageSavedCallback {
                    override fun onError(exc: ImageCaptureException) {
                        Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                    }

                    override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                        val savedUri = Uri.fromFile(photoFile)
                        val msg = "Photo capture succeeded: $savedUri"
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        Log.d(TAG, msg)
                    }
                })
        }
    }

    private fun startVideoRecord(){

        if (!cam.isRecording){

            if (cam.captureMode != CameraView.CaptureMode.VIDEO) {
                cam.captureMode = CameraView.CaptureMode.VIDEO

                if (ActivityCompat.checkSelfPermission(
                        context!!,
                        Manifest.permission.CAMERA
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    Log.e(TAG,"Camera permission missing")
                    return
                }
                cam.bindToLifecycle(this)
            }

            // Create timestamped output file to hold the image
            val videoFile = File(
                getOutputDirectory(getString(R.string.video)),
                SimpleDateFormat(
                    FILENAME_FORMAT, Locale.US
                ).format(System.currentTimeMillis()) + ".mp4")

            cam.startRecording(videoFile, ContextCompat.getMainExecutor(context), object : VideoCapture.OnVideoSavedCallback{
                override fun onError(videoCaptureError: Int, message: String, cause: Throwable?) {
                    Log.e(TAG, "Photo capture failed: ${message}")
                }

                override fun onVideoSaved(file: File) {
                    val savedUri = Uri.fromFile(file)
                    val msg = "Photo capture succeeded: $savedUri"
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    Log.d(TAG, msg)
                }
            })

            img_record.setImageDrawable(resources.getDrawable(R.drawable.recordred))
            txt_record.text = "Stop"
        }
        else {
            cam.stopRecording()
            img_record.setImageDrawable(resources.getDrawable(R.drawable.recordwhite))
            txt_record.text = "Record"
        }
    }

    private fun getOutputDirectory(folder: String): File {
        val mediaDir = context!!.externalMediaDirs.firstOrNull()?.let {
            File(it, folder).apply { mkdirs() } }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else context!!.filesDir
    }

    companion object {
        private const val TAG = "Camera"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
    }


}