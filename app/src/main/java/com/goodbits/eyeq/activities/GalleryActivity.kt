package com.goodbits.eyeq.activities

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.BaseAdapter
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.bumptech.glide.Glide
import com.goodbits.eyeq.R
import com.goodbits.eyeq.bleservice.UartCallback
import com.goodbits.eyeq.ui.utils.EyeQUtils
import kotlinx.android.synthetic.main.activity_gallery.*
import java.io.File
import java.util.*


class GalleryActivity : AppCompatActivity(),UartCallback {

    private var dataList = ArrayList<String>()

    companion object{
        val FILE_DELETED = "com.goodbits.eyeq.gallery.file_deleted"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        this.window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_gallery)

        setColumnForGrid()

        btn_back.setOnClickListener {
            onBackPressed()
        }

        updateGallery()

        gridview.setOnItemClickListener { parent, view, position, id ->

            val intent = Intent (this@GalleryActivity, GalleryDetailsActivity::class.java )
            intent.putExtra("file_path", dataList[position])
            startActivity(intent)
        }

        val filter = IntentFilter()
        filter.addAction(FILE_DELETED)
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(receiver , filter)
    }

    private val receiver = object  : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (!isFinishing)
                when (intent?.action) {
                    FILE_DELETED -> updateGallery()
                }
        }
    }

    private fun updateGallery(){

        getFromSdcard()
        val imageAdapter = ImageAdapter(
            this,
            dataList
        )
        gridview.adapter = imageAdapter

    }

    private fun getFromSdcard() {
        dataList.clear()
//        val file = File(Environment.getExternalStorageDirectory(), "Android/media/com.goodbits.eyeq/Image")
        val file = EyeQUtils.getOutputDirectory(this)
        if (file.isDirectory) {
            var dataFileList = file.listFiles()
            for (i in dataFileList!!.indices) {
                dataList.add(dataFileList!![i].absolutePath)
                dataList.reverse()
            }
        }
    }

    private fun setColumnForGrid() {
        val width: Int = resources.displayMetrics.widthPixels
        val px = resources.getDimension(R.dimen.gird_column_width)
        val numCol = (width / px).toInt()
        gridview.numColumns = numCol
    }

    class ImageAdapter(
        private var context: Context,
        private var arrayListImage: ArrayList<String>
    ) : BaseAdapter() {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

            var myView = convertView
            val holder: ViewHolder

            if (myView == null) {
                val mInflater = (context as Activity).layoutInflater

                myView = mInflater.inflate(R.layout.gallery_item, parent, false)
                holder =
                    ViewHolder()
                holder.imageview = myView!!.findViewById(R.id.thumbImage) as ImageView
                holder.play = myView.findViewById(R.id.play) as ImageView

                //Set A Tag to Identify our view.
                myView.tag = holder

            } else {
                //If Our View in not Null than Just get View using Tag and pass to holder Object.
                holder = myView.tag as ViewHolder
            }

            val file = File(arrayListImage[position])
            if (file.toString().endsWith(".mp4")) {
                holder.play.visibility = View.VISIBLE
            } else {
                holder.play.visibility = View.GONE
            }

            val imgUri = Uri.fromFile(file)
            Glide.with(context)
                .load(imgUri)
                .thumbnail(0.1f)
                .into(holder.imageview)

            //Setting Image to ImageView by position
//            holder.imageview!!.setImageResource(arrayListImage.get(position))
            return myView
        }

        //Auto Generated Method
        override fun getItem(p0: Int): Any {
            return arrayListImage[p0]
        }

        //Auto Generated Method
        override fun getItemId(p0: Int): Long {
            return 0
        }

        //Auto Generated Method
        override fun getCount(): Int {
            return arrayListImage.size
        }


//    class ImageAdapter : BaseAdapter() {
//        private val mInflater: LayoutInflater?
//        override fun getCount(): Int {
//            return img.size
//        }
//
//        override fun getItem(position: Int): Any {
//            return position
//        }
//
//        override fun getItemId(position: Int): Long {
//            return position.toLong()
//        }
//
//        // create a new ImageView for each item referenced by the Adapter
//        override fun getView(
//            position: Int,
//            convertView: View,
//            parent: ViewGroup
//        ): View {
//            var convertView = convertView
//            val holder: ViewHolder
//            holder =
//                convertView.tag as ViewHolder
//            val file = File(img.get(position))
//            if (file.toString().endsWith(".mp4")) {
//                holder.play!!.visibility = View.VISIBLE
//            } else {
//                holder.play!!.visibility = View.GONE
//            }
//            val imgUri = Uri.fromFile(file)
//            Glide.with(this)
//                .load(imgUri)
//                .thumbnail(0.1f)
//                .into(holder.imageview)
//            return convertView
//        }
//
//        init {
//            mInflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater?
//        }
//    }

        internal class ViewHolder {
            lateinit var imageview: ImageView
            lateinit var play: ImageView
        }

    }

    override fun onBackPressed() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    override fun onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver)
        super.onDestroy()
    }

    override fun handleException(action: String?) {
        Log.d("[EyeQ] Gallery screen", "Service Message = $action")
    }

}

