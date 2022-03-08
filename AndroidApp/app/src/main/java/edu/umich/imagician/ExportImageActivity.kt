package edu.umich.imagician

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import edu.umich.imagician.utils.toast

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
        shareIntent.type = "image/jpg"
        shareIntent.putExtra(Intent.EXTRA_STREAM, newImageUri)
        startActivity(Intent.createChooser(shareIntent, "Share image using"))
    }
    private fun mockProgressBar() {
        val handler: Handler = Handler()
        toast("sending $watermarkPostJsonStr")
        Thread( Runnable {
            // run lsb here
            // send data here
                for (i in 1..100) {
                    try {
                        Thread.sleep(50)
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }
                    handler.post(Runnable {
                        progressBar.progress = i
                        newImageUri = imageUri // mock
                    })

                }
            handler.post(Runnable {setViewVisibilityByState(false)})
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