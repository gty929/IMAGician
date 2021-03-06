package edu.umich.imagician

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import edu.umich.imagician.utils.*
import kotlinx.coroutines.*
import java.io.ByteArrayOutputStream
import java.security.SecureRandom
import java.util.concurrent.TimeoutException
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

/**
 * Created by Tianyao Gu on 2022/3/8.
 */
@ExperimentalCoroutinesApi
class ExportImageActivity : AppCompatActivity() {
    private var imageUri: Uri? = null
    private var newImageUri: Uri? = null
    private var title: String? = null
    private lateinit var progressBar: ProgressBar
    private val hasEncoded = AtomicBoolean()
    private val hasHashed = AtomicBoolean()
    private val hasUploaded = AtomicBoolean()
    private val uploadFailed = AtomicBoolean()
    private val speedRatio = AtomicInteger()

    private val tag = SecureRandom().nextLong().ushr(8)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_export_image)
        imageUri = intent.getParcelableExtra("IMAGE_URI")
        progressBar = findViewById(R.id.progressBar)

        // progressing state
        setViewVisibilityByState(true)

        // set top bar
        findViewById<ImageView>(R.id.imagePreview).setImageURI(imageUri)
        title = WatermarkPost.post.title
        findViewById<TextView>(R.id.imageTitle).text = title
        MainScope().launch {

            launch { mockProgressBar() }
            launch { embedWatermark(tag) }

        }

    }

    fun onClickReturnHome(view: View?) {
        startActivity(Intent(this, MainActivity::class.java))
    }

    fun onClickShare(view: View?) {
        val shareIntent = Intent(Intent.ACTION_SEND)
        // yyzjason: changed to png(lossless)
        shareIntent.type = "image/png"
        shareIntent.putExtra(Intent.EXTRA_STREAM, newImageUri)
        startActivity(Intent.createChooser(shareIntent, "Share image using"))
    }

    private suspend fun mockProgressBar() {

        try {

            for (i in 1..99) {

                val embedFlag = hasEncoded.get()
                val hashFlag = hasHashed.get()
                val uploadFlag = hasUploaded.get()
                if (embedFlag && i < 75 || hashFlag && i < 85 || uploadFlag) {
                    delay(5) // update the progress bar faster
                } else if (!embedFlag && i >= 75 || !hashFlag && i >= 85) {
                    myDelay(100) // update the progress bar slower
                } else {
                    myDelay(50) // update the progress bar at normal rate
                }
                progressBar.progress = i
            }
            while (!hasUploaded.get()) {
                // taking longer than expected
                delay(50) // just busy waiting
            }
            progressBar.progress = 100
            if (uploadFailed.get()) {
                val intent = Intent(this, PopUpWindow::class.java)
                intent.putExtra("popuptitle", "Embedding Failed")
                intent.putExtra(
                    "popuptext",
                    "An issue occurred during embedding. Please try again. "
                )
                intent.putExtra("popupbtn", "OK")
                intent.putExtra("darkstatusbar", true)
                intent.putExtra("gohome", true)
                startActivity(intent)
            }
            setViewVisibilityByState(false)


        } catch (e: TimeoutException) {
            toast("Embed timeout, please use a smaller image", false)
            Log.e("Embed timeout", "timeout error: ", e)
        } catch (e: Exception) {
            Log.e("Embed error", "unknown error", e)
        }
    }

    private suspend fun embedWatermark(tag: Long) {
        try {

            val prevImg: Bitmap =
                MediaStore.Images.Media.getBitmap(this.contentResolver, imageUri)
            speedRatio.set(prevImg.width * prevImg.height)
            val context = this
            Log.d("ExportActivity", "Check existing tag start")
            if (withContext(Dispatchers.Default) { ktdecode64(prevImg) } != null) { // duplicate
                val intent =
                    Intent(context, PopUpWindow::class.java)
                intent.putExtra("popuptitle", "Error")
                intent.putExtra("popuptext", "Tag detected in the image. Embedding aborted.")
                intent.putExtra("popupbtn", "OK")
                intent.putExtra("darkstatusbar", true)
                intent.putExtra("gohome", true)
                startActivity(intent)
            } else {
                Log.d("ExportActivity", "Encode tag start")
                val newImg = withContext(Dispatchers.Default) {
                    ktencode64(prevImg, tag)
                }
                if (newImg == null) {
                    val intent =
                        Intent(this, PopUpWindow::class.java)
                    intent.putExtra("popuptitle", "Error")
                    intent.putExtra(
                        "popuptext",
                        "There is an error when embedding watermark, please try again."
                    )
                    intent.putExtra("popupbtn", "OK")
                    intent.putExtra("darkstatusbar", true)
                    intent.putExtra("gohome", true)
                    startActivity(intent)
                } else {
                    val bytes = ByteArrayOutputStream()
                    val checksum = withContext(Dispatchers.Default) {
                        Log.d("ExportActivity","Encode tag finished and compress tagged image to png start")
                        newImg.compress(Bitmap.CompressFormat.PNG, 100, bytes)
                        Log.d("ExportActivity","Compress tagged image to png finished, and start hashing")
                        Hasher.hash(bytes)
                    }
                    Log.d("ExportActivity", "Hash finished")
                    withContext(Dispatchers.IO) {
                        newImageUri = mediaStoreAlloc(contentResolver, "image/png", "$title.png")
                        newImageUri?.let { it ->
                            contentResolver.openOutputStream(it)?.let {
                                it.write(bytes.toByteArray())
                                it.close()
                            }
                        }
                        Log.d("New Image Uri", newImageUri?.toString() ?: "")
                    }

                    hasEncoded.set(true)

                    WatermarkPost.post.checksum = checksum
                    hasHashed.set(true)

                    Log.i("Export", "main scope")
                    val watermarkPost = WatermarkPost.post
                    watermarkPost.tag = tag.toString()
                    watermarkPost.mode = Sendable.Mode.FULL // query by tag


                    Log.i("Export", "start httpCall")
                    withContext(Dispatchers.IO) {
                        ItemStore.httpCall(watermarkPost) { code ->
                            if (code != 200) {
                                toast("Upload fails $code")
                                uploadFailed.set(true)
                            } else {
                                uploadFailed.set(false)
                            }
                            hasUploaded.set(true)
                        }
                    }

                }
            }

        } catch (e: Exception) {
            Log.e("Embed error", "unknown error", e)
            val intent =
                Intent(parent, PopUpWindow::class.java)
            intent.putExtra("popuptitle", "Error")
            intent.putExtra("popuptext", "There is an error when embedding watermark, please try again.")
            intent.putExtra("popupbtn", "OK")
            intent.putExtra("darkstatusbar", true)
            intent.putExtra("gohome", true)
            startActivity(intent)
        }
    }


    private fun setViewVisibilityByState(is_processing: Boolean) {
        findViewById<ConstraintLayout>(R.id.ProcessingLayout).isVisible = is_processing
        findViewById<ConstraintLayout>(R.id.SuccessLayout).isVisible = !is_processing
    }

    private suspend fun myDelay(time: Long) {
        val ratio = speedRatio.get()
        myDelayBase(time, ratio)
    }
}
