package edu.umich.imagician

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import edu.umich.imagician.utils.Hasher
import edu.umich.imagician.utils.ktdecode64
import edu.umich.imagician.utils.myDelayBase
import edu.umich.imagician.utils.toast
import kotlinx.coroutines.*
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

/**
 * Created by Tianyao Gu on 2022/3/8.
 */
class ExamineActivity : AppCompatActivity() {
    private var imageUri: Uri? = null
    private lateinit var progressBar: ProgressBar
    private val hasDecoded = AtomicBoolean()
    private val hasRetrieved = AtomicBoolean()
    private val hasChecked = AtomicBoolean()
    private val tagFound = AtomicBoolean()
    private val speedRatio = AtomicInteger()

    private var isModified = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_examine)
        imageUri = intent.getParcelableExtra("IMAGE_URI")
        if (imageUri == null || contentResolver.getType(imageUri!!)?.startsWith("image") != true) {
            val intent =
                Intent(this, PopUpWindow::class.java)
            intent.putExtra("popuptitle", "Error")
            intent.putExtra("popuptext", "The file doesn't exist or is not an image")
            intent.putExtra("popupbtn", "OK")
            intent.putExtra("darkstatusbar", true)
            intent.putExtra("gohome", true)
            startActivity(intent)
        }
        findViewById<ImageView>(R.id.imagePreview).setImageURI(imageUri)
        progressBar = findViewById(R.id.progressBar)
        MainScope().launch {
            launch { mockProgressBar() }
            launch { extractWatermark() }
        }

    }

    private suspend fun mockProgressBar() {
        try {
            for (i in 1..99) {

                val embedFlag = hasDecoded.get()
                val retrieveFlag = hasRetrieved.get()
                val checkFlag = hasChecked.get()
                if (embedFlag && i < 50 || checkFlag && i < 60 || retrieveFlag) {
                    myDelay(4) // update the progress bar faster
                } else if (!embedFlag && i >= 50 || !checkFlag && i >= 60) {
                    myDelay(40) // update the progress bar slower
                } else {
                    myDelay(20) // update the progress bar at normal rate
                }

                progressBar.progress = i
            }
            while (!hasRetrieved.get()) {
                // taking longer than expected
                delay(50) // just busy waiting
            }
            progressBar.progress = 100
            onExamineFinish()
        } catch (e: Exception) {
            Log.e("Examine error", "unknown error", e)
            val intent =
                Intent(this, PopUpWindow::class.java)
            intent.putExtra("popuptitle", "Error")
            intent.putExtra(
                "popuptext",
                "There is an error when examining watermark, please try again."
            )
            intent.putExtra("popupbtn", "OK")
            intent.putExtra("darkstatusbar", true)
            intent.putExtra("gohome", true)
            startActivity(intent)
        }
    }

    private suspend fun extractWatermark() {
        val img: Bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, imageUri)
        speedRatio.set(img.width * img.height)
        val tag = withContext(Dispatchers.Default) {
            ktdecode64(img)
        }
        hasDecoded.set(true)
        val context = this
        val checksum = withContext(Dispatchers.Default) {

            context.contentResolver.openInputStream(imageUri!!)?.buffered()?.use { Hasher.hash(it.readBytes()) }
        }
        hasChecked.set(true)

        WatermarkPost.post = WatermarkPost(tag = tag.toString(), mode = Sendable.Mode.EMPTY)
        withContext(Dispatchers.IO) {
            ItemStore.httpCall(WatermarkPost.post) { code ->
                if (code != 200) {
                    runOnUiThread {
                        toast("Retrieve fails $code")
                    }
                    tagFound.set(false)
                } else {
                    tagFound.set(true)
                }
                hasRetrieved.set(true)


                if (tagFound.get()) {

                    // check the checksum
                    // checksum of the post is the correct one (unmodified)
                    isModified = (checksum != WatermarkPost.post.checksum)
                }


            }
        }
    }

    private fun onExamineFinish() {
        if (!tagFound.get()) {
            val intent = Intent(this, PopUpWindow::class.java)
            intent.putExtra("popuptitle", "Extraction Failed")
            intent.putExtra("popuptext", "Watermark not found in this image!")
            intent.putExtra("popupbtn", "OK")
            intent.putExtra("darkstatusbar", true)
            intent.putExtra("gohome", true)
            startActivity(intent)
        } else {
            val intent = Intent(this, DisplayInfoActivity::class.java)
            intent.putExtra("isModified", isModified)
            intent.putExtra("IMAGE_URI", imageUri)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }
    }

    private suspend fun myDelay(time: Long) {
        val ratio = speedRatio.get()
        myDelayBase(time, ratio)
    }
}