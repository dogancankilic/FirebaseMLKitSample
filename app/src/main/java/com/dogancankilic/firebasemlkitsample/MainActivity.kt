package com.dogancankilic.firebasemlkitsample

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.view.animation.AnimationUtils
import kotlinx.android.synthetic.main.activity_main.*
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import android.content.ContentValues
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.support.v7.app.AlertDialog
import android.support.v7.widget.DividerItemDecoration
import android.widget.Toast
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import java.lang.Exception


import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {

    private lateinit var fileUri: Uri
    private var TAG = "MainActivity"
    private var isOpen = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        setContentView(R.layout.activity_main)
        askPhotoPermission()

        fab.setOnClickListener(View.OnClickListener {
            if (isOpen) {
                fabClose()
                isOpen = false

            } else {
                fabOpen()
                isOpen = true
            }

        })

        rec_view.layoutManager = LinearLayoutManager(this)
        val dividerItemDecoration = DividerItemDecoration(rec_view
                .context, DividerItemDecoration.VERTICAL)
        rec_view.addItemDecoration(dividerItemDecoration)
        rec_view.isNestedScrollingEnabled = true
    }


    private fun fabOpen() {
        fab2.startAnimation( AnimationUtils.loadAnimation(applicationContext, R.anim.fab_open))
        fab1.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.fab_open))
        fab.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.fab_rotate_clock))
        fab2.isClickable=true
        fab1.isClickable=true


    }

    private fun fabClose() {
        fab2.startAnimation( AnimationUtils.loadAnimation(applicationContext, R.anim.fab_close))
        fab1.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.fab_close))
        fab.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.fab_rotate_anticlock))
        fab2.isClickable=false
        fab1.isClickable=false
    }

    //pick a photo from gallery
    private fun pickPhotoFromGallery() {
        val pickImageIntent = Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(pickImageIntent, AppConstants.PICK_PHOTO_REQUEST)
    }

    private fun launchCamera() {
        val values = ContentValues(1)
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpg")
        fileUri = contentResolver
                .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        values)
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if(intent.resolveActivity(packageManager) != null) {
            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                    or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            startActivityForResult(intent, AppConstants.TAKE_PHOTO_REQUEST)
        }
    }

    private fun askPhotoPermission(){
        Dexter.withActivity(this)
                .withPermissions(
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                ).withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                        if(report.areAllPermissionsGranted()){
                            //camera
                            fab2.setOnClickListener {
                                launchCamera()

                            }

                            fab1.setOnClickListener {
                                pickPhotoFromGallery()

                            }

                        }else{
                            Toast.makeText(this@MainActivity, "All permissions need to be granted.", Toast.LENGTH_LONG).show()
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(permissions: List<PermissionRequest>, token: PermissionToken) {
                        AlertDialog.Builder(this@MainActivity)
                                .setTitle(
                                        "Permissions Error!")
                                .setMessage(
                                        "Please allow permissions ")
                                .setNegativeButton(
                                        android.R.string.cancel
                                ) { dialog, _ ->
                                    dialog.dismiss()
                                    token.cancelPermissionRequest()
                                }
                                .setPositiveButton(android.R.string.ok
                                ) { dialog, _ ->
                                    dialog.dismiss()
                                    token.continuePermissionRequest()
                                }
                                .setOnDismissListener {
                                    token.cancelPermissionRequest()
                                }
                                .show()
                    }

                }).check()

    }

    private fun imageRecognition() {
            try {
                var list = ArrayList<Model> ()

                var  image = FirebaseVisionImage.fromFilePath(this,fileUri)
                val labeler = FirebaseVision.getInstance().visionLabelDetector
                labeler.detectInImage(image)
                        .addOnSuccessListener { labels ->
                            for (label in labels) {
                                val text = label.label
                                val confidence =  label.confidence
                                var model = Model(text,confidence)
                                list.add(model)

                                rec_view.adapter = MyAdapter(this,list)
                            }

                        }.addOnFailureListener { e ->
                            // Task failed with an exception
                            // ...
                            Log.d(TAG,e.toString())
                        }
            }catch (e : Exception) {
                Log.d(TAG,e.toString())
            }

    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK
                && requestCode == AppConstants.TAKE_PHOTO_REQUEST) {
            //photo from camera
            image.setImageURI(fileUri)
            fabClose()
            isOpen = false
            rec_view.adapter=null
            progressBar.visibility = View.VISIBLE
            Handler(Looper.getMainLooper()).postDelayed({
                imageRecognition()
                progressBar.visibility = View.GONE
            }, 2000)

        }
        else if(resultCode == Activity.RESULT_OK
                && requestCode == AppConstants.PICK_PHOTO_REQUEST){
            //photo from gallery
            fileUri = data?.data!!
            image.setImageURI(fileUri)
            fabClose()
            isOpen = false
            rec_view.adapter=null
            progressBar.visibility = View.VISIBLE
            Handler(Looper.getMainLooper()).postDelayed({
                imageRecognition()
                progressBar.visibility = View.GONE
            }, 2000)

        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }


}
