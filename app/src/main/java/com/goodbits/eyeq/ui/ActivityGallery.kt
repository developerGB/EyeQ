package com.goodbits.eyeq.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.goodbits.eyeq.R
import com.goodbits.eyeq.ui.utils.EyeQUtils
import kotlinx.android.synthetic.main.activity_gallery.*
import java.io.File
import java.util.*


class ActivityGallery : AppCompatActivity() {

    private var imgList = ArrayList<String>()
    private var imageAdapter: ImageAdapter? = null
    private var listFile: Array<File>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery)

        setColumnForGrid()

        btn_back.setOnClickListener {
            onBackPressed()
        }

        getFromSdcard()
        imageAdapter = ImageAdapter(this, imgList)
        gridview.adapter = imageAdapter
    }

    private fun getFromSdcard() {
        imgList.clear()
//        val file = File(Environment.getExternalStorageDirectory(), "Android/media/com.goodbits.eyeq/Image")
        val file = EyeQUtils.getOutputDirectory(this)
        if (file.isDirectory) {
            listFile = file.listFiles()
            for (i in listFile!!.indices) {
                imgList.add(listFile!![i].absolutePath)
                imgList.reverse()
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
                holder = ViewHolder()
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

}

