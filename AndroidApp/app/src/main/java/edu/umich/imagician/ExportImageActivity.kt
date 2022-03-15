package edu.umich.imagician

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import com.google.gson.Gson
import edu.umich.imagician.utils.mediaStoreAlloc
import edu.umich.imagician.utils.toast
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.security.SecureRandom
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Created by Tianyao Gu on 2022/3/8.
 */
@ExperimentalCoroutinesApi
class ExportImageActivity: AppCompatActivity() {
//    private var watermarkPostJsonStr: String? = null
    private var imageUri: Uri? = null
    private var newImageUri: Uri? = null
    private var filename: String? = null
    private lateinit var progressBar: ProgressBar
    private var hasEncoded = AtomicBoolean()
    private var hasHashed = AtomicBoolean()
    private var hasUploaded = AtomicBoolean()
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
//        filename = intent.extras?.getString("FILENAME_STR")
        filename = WatermarkPost.post.filename
        findViewById<TextView>(R.id.imageFilename).text = filename
        mockProgressBar()

        embedWatermark(tag)
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
    private fun mockProgressBar() {
        val handler: Handler = Handler()

        Thread( Runnable {
            for (i in 1..99) {
                try {
                    val embedFlag = hasEncoded.get()
                    val hashFlag = hasHashed.get()
                    val uploadFlag = hasUploaded.get()
                    if (embedFlag && i < 50 || hashFlag && i < 70 || uploadFlag) {
                        Thread.sleep(5) // update the progress bar faster
                    } else if (!embedFlag && i >= 50 || !hashFlag && i >= 70) {
                        Thread.sleep(100) // update the progress bar slower
                    } else {
                        Thread.sleep(40) // update the progress bar at normal rate
                    }
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
                handler.post(Runnable {
                    progressBar.progress = i
                })
            }
            while (!hasUploaded.get()) {
                // taking longer than expected
                try {
                    Thread.sleep(50) // just busy waiting
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
            handler.post(Runnable {
                progressBar.progress = 100
                setViewVisibilityByState(false)
            })
        }).start()
    }

    private fun embedWatermark(tag: String) {
        val handler: Handler = Handler()
        Thread( Runnable {
            // yyzjason: LSB encode
//            var iv:ImageView = findViewById<ImageView>(R.id.imagePreview)
//            val prev_img : Bitmap = iv.drawable.toBitmap() // tyg: don't rely on the view
            runOnUiThread {
                toast("embedding watermark with tag $tag")
            }
            val prevImg: Bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, imageUri)
            val newImg: Bitmap = StegnoAlgo.encode(prevImg,tag)
            val bytes = ByteArrayOutputStream()
            newImg.compress(Bitmap.CompressFormat.PNG, 100, bytes)

            newImageUri = mediaStoreAlloc(contentResolver, "image/png", "$filename$tag.png")
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
            val checksum = StegnoAlgo.getChecksum(newImg)
            WatermarkPost.post.checksum = checksum
            hasHashed.set(true)
            // send data here (all fields + checksum) [TO BE UPDATED]
            runOnUiThread {
                toast("sending watermark with checksum $checksum", false)
            }
//            val context = this
            Log.i("Export","main scope")
            MainScope().launch {
                val watermarkPost = WatermarkPost.post
                watermarkPost.tag = tag
                watermarkPost.mode = Sendable.Mode.FULL // query by tag
                Log.i("Export","start httpCall")
                ItemStore.httpCall(watermarkPost) { code ->
                    if (code != 200) toast("Upload fails $code")
                    hasUploaded.set(true)
                }
//                hasUploaded.set(true)
            }
//            try {
//                Thread.sleep(2000) // mock network delay
//            } catch (e: InterruptedException) {
//                e.printStackTrace()
//            }

        }).start()
    }


    private fun setViewVisibilityByState(is_processing: Boolean) {
        findViewById<ConstraintLayout>(R.id.ProcessingLayout).isVisible = is_processing
        findViewById<ConstraintLayout>(R.id.SuccessLayout).isVisible = !is_processing
//        progressBar.isVisible = is_processing
//        findViewById<TextView>(R.id.imageProcessing).isVisible = is_processing
//        findViewById<TextView>(R.id.embedSucesss).isVisible = !is_processing
//        findViewById<TextView>(R.id.watermarkEmbedded).isVisible = !is_processing
//        findViewById<TextView>(R.id.toAlbumButton).isVisible = !is_processing
//        findViewById<TextView>(R.id.returnHomeButton).isVisible = !is_processing
//        findViewById<TextView>(R.id.returnHomeText).isVisible = !is_processing
    }

}
