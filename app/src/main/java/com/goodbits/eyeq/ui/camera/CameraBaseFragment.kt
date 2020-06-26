package com.goodbits.eyeq.ui.camera

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.SystemClock
import android.util.Log
import android.view.LayoutInflater
import android.view.PixelCopy
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.VideoCapture
import androidx.camera.view.CameraView
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.goodbits.eyeq.R
import com.goodbits.eyeq.ui.ActivityGallery
import com.goodbits.eyeq.ui.utils.EyeQUtils
import kotlinx.android.synthetic.main.fragment_camera.*
import kotlinx.android.synthetic.main.layout_photoquality.*
import kotlinx.android.synthetic.main.layout_settings.*
import kotlinx.android.synthetic.main.layout_videoquality.*
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

const val DEFAULT_ZOOM = 1f

class CameraBaseFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_camera, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        startCamera()

        seek_zoom.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
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
        btn_browse.setOnClickListener {
            val intent = Intent(activity, ActivityGallery::class.java)
            startActivity(intent)
            activity?.finish()
        }

        btn_settings.setOnClickListener {

            menu_container.visibility = View.GONE
            settings_container.visibility = View.VISIBLE

            settings_back.setOnClickListener {
                menu_container.visibility = View.VISIBLE
                settings_container.visibility = View.GONE
            }

            videoquality.setOnClickListener {
                settings_container.visibility = View.GONE
                video_container.visibility = View.VISIBLE

                highquality.setOnClickListener {
                    img_high.setImageDrawable(resources.getDrawable(R.drawable.checkmarkactive))
                    img_med.setImageDrawable(resources.getDrawable(R.drawable.checkmarkinactive))
                    img_low.setImageDrawable(resources.getDrawable(R.drawable.checkmarkinactive))
                    imageQuality(1)
                }
                medquality.setOnClickListener {
                    img_high.setImageDrawable(resources.getDrawable(R.drawable.checkmarkinactive))
                    img_med.setImageDrawable(resources.getDrawable(R.drawable.checkmarkactive))
                    img_low.setImageDrawable(resources.getDrawable(R.drawable.checkmarkinactive))
                    imageQuality(2)
                }
                lowquality.setOnClickListener {
                    img_high.setImageDrawable(resources.getDrawable(R.drawable.checkmarkinactive))
                    img_med.setImageDrawable(resources.getDrawable(R.drawable.checkmarkinactive))
                    img_low.setImageDrawable(resources.getDrawable(R.drawable.checkmarkactive))
                    imageQuality(3)
                }
                video_done.setOnClickListener {
                    menu_container.visibility = View.VISIBLE
                    video_container.visibility = View.GONE
                }
            }

            photoquality.setOnClickListener {
                settings_container.visibility = View.GONE
                pic_container.visibility = View.VISIBLE

                maxres.setOnClickListener {
                    img_highres.setImageDrawable(resources.getDrawable(R.drawable.checkmarkactive))
                    img_medres.setImageDrawable(resources.getDrawable(R.drawable.checkmarkinactive))
                    img_lowres.setImageDrawable(resources.getDrawable(R.drawable.checkmarkinactive))
                    img_vga.setImageDrawable(resources.getDrawable(R.drawable.checkmarkinactive))
                    imageQuality(1)
                }
                q1080p.setOnClickListener {
                    img_highres.setImageDrawable(resources.getDrawable(R.drawable.checkmarkinactive))
                    img_medres.setImageDrawable(resources.getDrawable(R.drawable.checkmarkactive))
                    img_lowres.setImageDrawable(resources.getDrawable(R.drawable.checkmarkinactive))
                    img_vga.setImageDrawable(resources.getDrawable(R.drawable.checkmarkinactive))
                    imageQuality(2)
                }
                q720p.setOnClickListener {
                    img_highres.setImageDrawable(resources.getDrawable(R.drawable.checkmarkinactive))
                    img_medres.setImageDrawable(resources.getDrawable(R.drawable.checkmarkinactive))
                    img_lowres.setImageDrawable(resources.getDrawable(R.drawable.checkmarkactive))
                    img_vga.setImageDrawable(resources.getDrawable(R.drawable.checkmarkinactive))
                    imageQuality(3)
                }
                vga.setOnClickListener {
                    img_highres.setImageDrawable(resources.getDrawable(R.drawable.checkmarkinactive))
                    img_medres.setImageDrawable(resources.getDrawable(R.drawable.checkmarkinactive))
                    img_lowres.setImageDrawable(resources.getDrawable(R.drawable.checkmarkinactive))
                    img_vga.setImageDrawable(resources.getDrawable(R.drawable.checkmarkactive))
                    imageQuality(4)
                }
                pic_done.setOnClickListener {
                    menu_container.visibility = View.VISIBLE
                    pic_container.visibility = View.GONE
                }
            }
        }

    }

//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        setContentView(R.layout.fragment_camera)
//
//    }

    private fun setZoom(zm: Float) {
        cam.zoomRatio = zm
        txt_seek.text = "${zm}x"
    }

    private fun startCamera() {

        if (ActivityCompat.checkSelfPermission(
                context!!,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e(TAG, "Camera permission missing")
            return
        }

        cam.bindToLifecycle(this)

        cam.isPinchToZoomEnabled = false

        setZoom(DEFAULT_ZOOM)
    }

    private fun takePhoto() {

        // Create timestamped output file to hold the image
        val photoFile = File(
            EyeQUtils.getOutputDirectory(context!!),
            SimpleDateFormat(
                FILENAME_FORMAT, Locale.US
            ).format(System.currentTimeMillis()) + ".jpg"
        )

        if (!cam.isRecording) {
            if (cam.captureMode != CameraView.CaptureMode.IMAGE) {
                cam.captureMode = CameraView.CaptureMode.IMAGE

                if (ActivityCompat.checkSelfPermission(
                        context!!,
                        Manifest.permission.CAMERA
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    Log.e(TAG, "Camera permission missing")
                    return
                }
                cam.bindToLifecycle(this)
            }

            img_photo.setImageDrawable(resources.getDrawable(R.drawable.cameragreen,activity?.theme))

            // Create timestamped output file to hold the image
            val photoFile = File(
                EyeQUtils.getOutputDirectory(context!!),
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
                        img_photo.setImageDrawable(resources.getDrawable(R.drawable.camerawhite,activity?.theme))
                    }

                    override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                        val savedUri = Uri.fromFile(photoFile)
                        val msg = "Photo capture succeeded: $savedUri"
                        Toast.makeText(context, "Photo saved", Toast.LENGTH_SHORT).show()
                        Log.d(TAG, msg)
                        img_photo.setImageDrawable(resources.getDrawable(R.drawable.camerawhite,activity?.theme))
                    }
                })
        }
        else{

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                img_photo.setImageDrawable(
                    resources.getDrawable(
                        R.drawable.cameragreen,
                        activity?.theme
                    )
                )

                //hiding views
                left_menu.visibility = View.GONE
                right_menu.visibility = View.GONE
                zoom.visibility = View.GONE
                chronometer.visibility = View.GONE

                Handler().postDelayed({

                    // Create a bitmap the size of the scene view.
                    val bitmap = Bitmap.createBitmap(
                        cam.getWidth(), cam.getHeight(),
                        Bitmap.Config.ARGB_8888
                    )

                    val locationOfViewInWindow = IntArray(2)
                    cam.getLocationInWindow(locationOfViewInWindow)

                    // Create a handler thread to offload the processing of the image.

                    // Create a handler thread to offload the processing of the image.
                    val handlerThread = HandlerThread("PixelCopier")
                    handlerThread.start()
                    // Make the request to copy.
                    // Make the request to copy.
                    PixelCopy.request(activity?.window!!, Rect(locationOfViewInWindow[0],
                        locationOfViewInWindow[1],
                        locationOfViewInWindow[0] + cam.width,
                        locationOfViewInWindow[1] + cam.height), bitmap, { copyResult ->
                        if (copyResult == PixelCopy.SUCCESS) {
                            Log.e(TAG, bitmap.toString())
                            storeImage(bitmap)
                        } else {

                            activity?.runOnUiThread{
                                Toast.makeText(
                                    context!!,
                                    "Photo capture failed", Toast.LENGTH_LONG
                                ).show()

                                Log.e(TAG, "Photo capture failed: $copyResult")
                            }
                        }
                        handlerThread.quitSafely()
                    }, Handler(handlerThread.getLooper()))
                }, 10)
            }
            else{
                Toast.makeText(context,"Sorry snapshot mode is not supported on your device", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun storeImage(bmp: Bitmap): Boolean {

        // Create timestamped output file to hold the image
        val photoFile = File(
            EyeQUtils.getOutputDirectory(context!!),
            SimpleDateFormat(
                FILENAME_FORMAT, Locale.US
            ).format(System.currentTimeMillis()) + ".jpg"
        )

        try {

            val bos = ByteArrayOutputStream()
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, bos)
            val bitmapdata = bos.toByteArray()
            val fis = ByteArrayInputStream(bitmapdata)
            val fos = FileOutputStream(photoFile)

            val buf = ByteArray(1024)
            var len: Int
            while (fis.read(buf).also { len = it } > 0) {
                fos.write(buf, 0, len)
            }
            fis.close()
            fos.close()

            activity?.runOnUiThread {

                img_photo.setImageDrawable(resources.getDrawable(R.drawable.camerawhite,activity?.theme))

                //hiding views
                left_menu.visibility = View.VISIBLE
                right_menu.visibility = View.VISIBLE
                zoom.visibility = View.VISIBLE
                chronometer.visibility = View.VISIBLE

                Toast.makeText(context, "Photo saved", Toast.LENGTH_SHORT).show()

                val savedUri = Uri.fromFile(photoFile)
                Log.d(TAG, "Photo capture succeeded: $savedUri")
            }
        } catch (e: FileNotFoundException) {
            Log.w(TAG, "Error saving image file: " + e.message)
            return false
        } catch (e: IOException) {
            Log.w(TAG, "Error saving image file: " + e.message)
            return false
        }
        return true
    }

    private fun startVideoRecord() {

        if (!cam.isRecording) {

            if (cam.captureMode != CameraView.CaptureMode.VIDEO) {
                cam.captureMode = CameraView.CaptureMode.VIDEO

                if (ActivityCompat.checkSelfPermission(
                        context!!,
                        Manifest.permission.CAMERA
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    Log.e(TAG, "Camera permission missing")
                    return
                }
                cam.bindToLifecycle(this)
            }

            // Create timestamped output file to hold the image
            val videoFile = File(
                EyeQUtils.getOutputDirectory(context!!),
                SimpleDateFormat(
                    FILENAME_FORMAT, Locale.US
                ).format(System.currentTimeMillis()) + ".mp4"
            )

            cam.startRecording(
                videoFile,
                ContextCompat.getMainExecutor(context),
                object : VideoCapture.OnVideoSavedCallback {
                    override fun onError(
                        videoCaptureError: Int,
                        message: String,
                        cause: Throwable?
                    ) {
                        Log.e(TAG, "video capture failed: ${message}")
                    }

                    override fun onVideoSaved(file: File) {
                        val savedUri = Uri.fromFile(file)
                        val msg = "Video capture succeeded: $savedUri"
                        Toast.makeText(context, "Video saved", Toast.LENGTH_SHORT).show()
                        Log.d(TAG, msg)
                    }
                })

            img_record.setImageDrawable(resources.getDrawable(R.drawable.recordred))
            txt_record.text = "Stop"

            chronometer!!.base = SystemClock.elapsedRealtime()
            chronometer.visibility = View.VISIBLE
            chronometer.start()


        } else {
            cam.stopRecording()
            chronometer.stop()
            chronometer.visibility = View.GONE
            img_record.setImageDrawable(resources.getDrawable(R.drawable.recordwhite))
            txt_record.text = "Record"
        }
    }

    companion object {
        private const val TAG = "Camera"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
    }

    private fun imageQuality(id: Int) {


    }

}