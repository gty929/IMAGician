package edu.umich.imagician.utils

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import java.io.File
import android.text.Editable
import android.widget.EditText


/**
 * Created by Tianyao Gu on 2022/1/17.
 */
fun Context.toast(message: String, short: Boolean = true) {
    Log.d("toasted", message)
    Toast.makeText(this, message, if (short) Toast.LENGTH_SHORT else Toast.LENGTH_LONG).show()
}

fun ImageView.display(uri: Uri) {
    setImageURI(uri)
    visibility = View.VISIBLE
}

fun Uri.toFile(context: Context): File? {

    if (!(authority == "media" || authority == "com.google.android.apps.photos.contentprovider")) {
        // for on-device media files only
        context.toast("Media file not on device")
        Log.d("Uri.toFile", authority.toString())
        return null
    }

    if (scheme.equals("content")) {
        var cursor: Cursor? = null
        try {
            cursor = context.getContentResolver().query(
                this, arrayOf("_data"),
                null, null, null
            )

            cursor?.run {
                moveToFirst()
                return File(getString(getColumnIndexOrThrow("_data")))
            }
        } finally {
            cursor?.close()
        }
    }
    return null
}

/*
    allocate space in the MediaStore to store the picture/video
     */
fun mediaStoreAlloc(contentResolver: ContentResolver, mediaType: String, filename: String? = null): Uri? {
    val values = ContentValues()
    values.put(MediaStore.MediaColumns.MIME_TYPE, mediaType)
    values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
    filename.let {
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, it);
    }
    return contentResolver.insert(
        if (mediaType.contains("video"))
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        else
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        values)
}


fun initPython(context: Context){
    if (! Python.isStarted()) {
        Python.start(AndroidPlatform(context));
    }
}

fun editToStr(txt: Editable): String? {
    return if (txt.isEmpty()) null else txt.toString()
}
