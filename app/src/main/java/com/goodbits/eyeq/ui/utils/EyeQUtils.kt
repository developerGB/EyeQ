package com.goodbits.eyeq.ui.utils

import android.content.Context
import android.os.Environment
import android.util.Log
import com.goodbits.eyeq.R
import java.io.File

class EyeQUtils {
    companion object{
        fun getOutputDirectory(context: Context): File {
//            val mediaDir = context!!.externalMediaDirs.firstOrNull()?.let {
//                File(it, folder).apply { mkdirs() }
//            }
//            return if (mediaDir != null && mediaDir.exists())
//                mediaDir else context!!.filesDir

            val file = File(
                Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), context.getString(R.string.app_name))

            if (!file.exists())
                if (!file.mkdirs()) {
                    Log.e("EyeQ", "Failed to create directory")
                }
            return file
        }

        fun getSnapshotOutputDirectory(context: Context): File {
//            val mediaDir = context!!.externalMediaDirs.firstOrNull()?.let {
//                File(it, folder).apply { mkdirs() }
//            }
//            return if (mediaDir != null && mediaDir.exists())
//                mediaDir else context!!.filesDir

            val file = File(
                Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), context.getString(R.string.app_name) + "/snapshot")

            if (!file.exists())
                if (!file.mkdirs()) {
                    Log.e("EyeQ", "Failed to create directory")
                }
            return file
        }

    }
}