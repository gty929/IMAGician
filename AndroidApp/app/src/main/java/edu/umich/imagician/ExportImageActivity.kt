package edu.umich.imagician

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.isVisible
import edu.umich.imagician.utils.toast
import java.io.ByteArrayOutputStream

/**
 * Created by Tianyao Gu on 2022/3/8.
 */
class ExportImageActivity: AppCompatActivity() {
    private var watermarkPostJsonStr: String? = null
    private var imageUri: Uri? = null
    private var newImageUri: Uri? = null
    private lateinit var progressBar: ProgressBar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_export_image)
        imageUri = intent.getParcelableExtra("IMAGE_URI")
        watermarkPostJsonStr = intent.extras?.getString("WATERMARK_POST_JSON_STR")
        progressBar = findViewById(R.id.progressBar)
        progressBar.max = 100

        setViewVisibilityByState(true)

        findViewById<ImageView>(R.id.imagePreview).setImageURI(imageUri)
        mockProgressBar()
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
        toast("sending $watermarkPostJsonStr")
        Thread( Runnable {
            for (i in 1..99) {
                try {
                    Thread.sleep(50)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
                handler.post(Runnable {
                    progressBar.progress = i
                    // newImageUri = imageUri // mock
                })
            }
            // yyzjason: get tag from backend [TO BE UPDATED]
            val tag = "yyzjason"

            // yyzjason: LSB encode
            var iv:ImageView = findViewById<ImageView>(R.id.imagePreview)
            val prev_img : Bitmap = iv.drawable.toBitmap()
            val new_img: Bitmap = StenoAlgo().encode(prev_img,tag)
            val bytes = ByteArrayOutputStream()
            new_img.compress(Bitmap.CompressFormat.PNG, 100, bytes)
            val path = MediaStore.Images.Media.insertImage(contentResolver, new_img, null, null)
            newImageUri = Uri.parse(path)

            // yyzjason: checksum of the encoded image
            val checksum: String = StenoAlgo().getChecksum(new_img)

            // send data here (all fields + checksum) [TO BE UPDATED]


            // yyzjason: update the new image
            handler.post(Runnable {
                progressBar.progress = 100
                iv.setImageBitmap(new_img)
                setViewVisibilityByState(false)

            })
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
