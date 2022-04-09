package edu.umich.imagician.utils

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.os.Message
import android.provider.MediaStore
import android.text.Editable
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import java.io.File
import java.lang.Exception
import java.security.MessageDigest
import java.util.Arrays.copyOf
import javax.crypto.Cipher
import java.util.Base64.getDecoder
import java.util.Base64.getEncoder
import javax.crypto.spec.SecretKeySpec


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
    Log.d("File", "Converting $this to file")
//    if (!(authority == "media" || authority == "com.google.android.apps.photos.contentprovider")) {
//        // for on-device media files only
//        context.toast("Media file not on device")
//        Log.d("Uri.toFile", authority.toString())
//        return null
//    }

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

object Hasher {
    val md = MessageDigest.getInstance("SHA-256")

    fun hash(s: String): String {
        val bytes = s.toByteArray()
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }
}


fun getCipher(pwd: String?, encryption: Boolean): Cipher {
    val cipher = Cipher.getInstance("AES")
    var new_pwd: String? = null
    if (pwd != null) {
        if (pwd.length >= 16) {
            new_pwd = pwd.substring(0,17)
        } else {
            val padding_num = 16 - pwd.length
            new_pwd = pwd + "0".repeat(padding_num)
        }
    }
    val keySpec = SecretKeySpec(new_pwd?.toByteArray(), "AES")
    if (encryption) {
        cipher.init(Cipher.ENCRYPT_MODE, keySpec)
    } else {
        cipher.init(Cipher.DECRYPT_MODE, keySpec)
    }

    return cipher
}

fun encryptMSG_new(MSG: String?, pwd: String?): String {
    val cipher = getCipher(pwd, true)
    val message = "IMAGician$MSG"
    val encrypt_MSG = cipher.doFinal(message.toByteArray())
    val result = getEncoder().encodeToString(encrypt_MSG)
    return result
}

fun decryptMSG_new(encryptMSG: String?, pwd: String?): String? {
    try {
        val cipher = getCipher(pwd, false)
        val new_MSG = getDecoder().decode(encryptMSG)
        val decrypt_msg = String(cipher.doFinal(new_MSG))
        if (decrypt_msg.substring(0, 9).equals("IMAGician")) {
            return decrypt_msg.substring(9)
        } else {
            return null
        }
    } catch (e: Exception) {
        println("Error while decrypting: $e")

    }
    return null
}