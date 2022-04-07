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
import edu.umich.imagician.utils.ktencode
import edu.umich.imagician.utils.mediaStoreAlloc
import edu.umich.imagician.utils.toast
import kotlinx.coroutines.*
import java.io.ByteArrayOutputStream
import java.lang.Exception
import java.security.SecureRandom
import java.util.concurrent.TimeoutException
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

/**
 * Created by Tianyao Gu on 2022/3/8.
 */
@ExperimentalCoroutinesApi
class ExportImageActivity : AppCompatActivity() {
    //    private var watermarkPostJsonStr: String? = null
    private var imageUri: Uri? = null
    private var newImageUri: Uri? = null
    private var title: String? = null
    private lateinit var progressBar: ProgressBar
    private var hasEncoded = AtomicBoolean()
    private var hasHashed = AtomicBoolean()
    private var hasUploaded = AtomicBoolean()
    private var uploadFailed = AtomicBoolean()
    private var speedRatio = AtomicInteger()

    private val tag = SecureRandom().generateSeed(7).toString()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_export_image)
        imageUri = intent.getParcelableExtra("IMAGE_URI")
//        watermarkPostJsonStr = intent.extras?.getString("WATERMARK_POST_JSON_STR")
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
//        val handler: Handler = Handler()
        try {

            for (i in 1..99) {
                try {
                    val embedFlag = hasEncoded.get()
                    val hashFlag = hasHashed.get()
                    val uploadFlag = hasUploaded.get()
                    if (embedFlag && i < 50 || hashFlag && i < 70 || uploadFlag) {
                        delay(5) // update the progress bar faster
                    } else if (!embedFlag && i >= 50 || !hashFlag && i >= 70) {
                        myDelay(100) // update the progress bar slower
                    } else {
                        myDelay(40) // update the progress bar at normal rate
                    }
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
//                    handler.post(Runnable {
                progressBar.progress = i
//                    })
            }
            while (!hasUploaded.get()) {
                // taking longer than expected
                try {
                    delay(50) // just busy waiting
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
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

    private suspend fun embedWatermark(tag: String) {
//        val handler: Handler = Handler()
        try {

            // yyzjason: LSB encode
//            var iv:ImageView = findViewById<ImageView>(R.id.imagePreview)
//            val prev_img : Bitmap = iv.drawable.toBitmap() // tyg: don't rely on the view
            runOnUiThread {
                toast("embedding watermark with tag $tag")
            }

            val prevImg: Bitmap =
                MediaStore.Images.Media.getBitmap(this.contentResolver, imageUri)
            speedRatio.set(prevImg.width * prevImg.height)
            val newImg: Bitmap?
            withContext(Dispatchers.Default) {
//                newImg = StegnoAlgo.encode(prevImg, tag)
                newImg = ktencode(prevImg, tag)
            }

            if (newImg == null) {
                runOnUiThread {
                    toast("Duplicate tag detected")
                }
                val intent = Intent(this, PopUpWindow::class.java)
                intent.putExtra("popuptitle", "Error")
                intent.putExtra("popuptext", "Tag detected in the image. Embedding aborted.")
                intent.putExtra("popupbtn", "OK")
                intent.putExtra("darkstatusbar", true)
                intent.putExtra("gohome", true)
                startActivity(intent)
            } else {
                val bytes = ByteArrayOutputStream()
                newImg.compress(Bitmap.CompressFormat.PNG, 100, bytes)

                newImageUri = mediaStoreAlloc(contentResolver, "image/png", "$title.png")
                newImageUri?.let { it ->
                    contentResolver.openOutputStream(it)?.let {
                        it.write(bytes.toByteArray())
                        it.close()
                    }
                }
                Log.d("New Image Uri", newImageUri?.toString() ?: "")
//            val path = MediaStore.Images.Media.insertImage(contentResolver, newImg, null, null)
//            newImageUri = Uri.parse(path)

                hasEncoded.set(true)

                // yyzjason: update the new image
//            handler.post(Runnable {
//                iv.setImageBitmap(new_img) // tyg: the user wouldn't notice
//            })

                runOnUiThread {
                    toast("calculating checksum")
                }
                val checksum = withContext(Dispatchers.Default) {
                    StegnoAlgo.getChecksum(newImg)
                }
                WatermarkPost.post.checksum = checksum
                hasHashed.set(true)
                // send data here (all fields + checksum) [TO BE UPDATED]
                runOnUiThread {
                    toast("sending watermark with checksum $checksum", false)
                }
//            val context = this
                Log.i("Export", "main scope")
                val watermarkPost = WatermarkPost.post
                watermarkPost.tag = tag
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
//                hasUploaded.set(true)

            }

//


        } catch (e: TimeoutException) {
            toast("Embed timeout, please use a smaller image", false)
            Log.e("Embed timeout", "timeout error: ", e)
        } catch (e: Exception) {
            Log.e("Embed error", "unknown error", e)
        }
    }


    private fun setViewVisibilityByState(is_processing: Boolean) {
        findViewById<ConstraintLayout>(R.id.ProcessingLayout).isVisible = is_processing
        findViewById<ConstraintLayout>(R.id.SuccessLayout).isVisible = !is_processing
    }

    private suspend fun myDelay(time: Long) {
        val ratio = speedRatio.get()
        if (ratio == 0) {
            delay(time)
        } else {
            delay((ratio.toDouble() * time / (1080 * 1080 * 3)).toLong())
        }

    }
}
