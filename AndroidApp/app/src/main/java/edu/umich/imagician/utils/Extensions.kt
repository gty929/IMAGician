package edu.umich.imagician.utils

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.security.keystore.KeyProperties
import android.text.Editable
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import kotlinx.coroutines.delay
import java.io.ByteArrayOutputStream
import java.security.MessageDigest
import java.util.Base64.getDecoder
import java.util.Base64.getEncoder
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
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

/*
    allocate space in the MediaStore to store the picture/video
     */
fun mediaStoreAlloc(contentResolver: ContentResolver, mediaType: String, filename: String? = null): Uri? {
    val values = ContentValues()
    values.put(MediaStore.MediaColumns.MIME_TYPE, mediaType)
    values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
    filename.let {
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, it)
    }
    return contentResolver.insert(
        if (mediaType.contains("video"))
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        else
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        values)
}


fun editToStr(txt: Editable): String? {
    return if (txt.isEmpty()) null else txt.toString()
}

fun String.decodeHex(): ByteArray {
    check(length % 2 == 0) { "Must have an even length" }

    return chunked(2)
        .map { it.toInt(16).toByte() }
        .toByteArray()
}
fun ByteArray.toHex(): String = fold("") { str, it -> str + "%02x".format(it) }

object Hasher {
    val md: MessageDigest = MessageDigest.getInstance("SHA-256")

    fun hash(s: String): String {
        val bytes = s.toByteArray()
        val digest = md.digest(bytes)
        return digest.toHex()
    }

    fun hash(bytes: ByteArray): String {
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }

    fun hash(s: ByteArrayOutputStream): String {
        val bytes = s.toByteArray()
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }
}


fun getCipher(pwd: String?, encryption: Boolean): Cipher {
    val KEY_ALGORITHM = KeyProperties.KEY_ALGORITHM_AES
    val KEY_BLOCK_MODE = KeyProperties.BLOCK_MODE_GCM
    val KEY_PADDING = KeyProperties.ENCRYPTION_PADDING_NONE // GCM requires no padding
    val cipher = Cipher.getInstance("$KEY_ALGORITHM/$KEY_BLOCK_MODE/$KEY_PADDING")
    var new_pwd: String? = null
    if (pwd != null) {
        if (pwd.length >= 16) {
            new_pwd = pwd.substring(0,16)
        } else {
            val padding_num = 16 - pwd.length
            new_pwd = pwd + "0".repeat(padding_num)
        }
    }
    val keySpec = SecretKeySpec(new_pwd?.toByteArray(), KEY_ALGORITHM)
    cipher.init(if (encryption) Cipher.ENCRYPT_MODE else Cipher.DECRYPT_MODE, keySpec, GCMParameterSpec(128, "dongcidaci".toByteArray()))

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

suspend fun myDelayBase(time: Long, ratio: Int) {
    if (ratio == 0) {
        delay(time)
    } else {
        delay((ratio.toDouble() * time / (1080 * 1080 * 3)).toLong())
    }

}