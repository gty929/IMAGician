package edu.umich.imagician

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import edu.umich.imagician.utils.initPython
import edu.umich.imagician.utils.toast
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Created by Tianyao Gu on 2022/3/8.
 */
class ExamineActivity: AppCompatActivity() {
    private var imageUri: Uri? = null
    private lateinit var progressBar: ProgressBar
    private var hasDecoded = AtomicBoolean()
    private var hasRetrieved = AtomicBoolean()
    private var hasChecked = AtomicBoolean()

    private var isModified = false
    private var isAuthorized = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_examine)
        imageUri = intent.getParcelableExtra("IMAGE_URI")
        findViewById<ImageView>(R.id.imagePreview).setImageURI(imageUri)
        progressBar = findViewById(R.id.progressBar)
        mockProgressBar()
        extractWatermark()

    }

    private fun mockProgressBar() {
        val handler: Handler = Handler()

        Thread( Runnable {
            for (i in 1..99) {
                try {
                    val embedFlag = hasDecoded.get()
                    val retrieveFlag = hasRetrieved.get()
                    val checkFlag = hasChecked.get()
                    if (embedFlag && i < 50 || retrieveFlag && i < 80 || checkFlag) {
                        Thread.sleep(5) // update the progress bar faster
                    } else if (!embedFlag && i >= 50 || !retrieveFlag && i >= 80) {
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
            while (!hasChecked.get()) {
                // taking longer than expected
                try {
                    Thread.sleep(50) // just busy waiting
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
            handler.post(Runnable {
                progressBar.progress = 100
                onExamineFinish()
            })
        }).start()
    }

    private fun extractWatermark() {
        val handler: Handler = Handler()
        Thread( Runnable {
            val img: Bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, imageUri);
            runOnUiThread {
                toast("decoding watermark")
            }
            val tag = StegnoAlgo.decode(img)

            // yyzjason: checksum of the encoded image
            hasDecoded.set(true)

            // yyzjason: update the new image
            runOnUiThread {
                toast("retrieving data with tag = $tag", false)
            }
            try {
                Thread.sleep(2000) // mock network delay
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
            hasRetrieved.set(true)

            runOnUiThread {
                toast("checking integrity")
            }
            val checksum = StegnoAlgo.getChecksum(img)
            // check the checksum
            hasChecked.set(true)
            runOnUiThread {
                toast("checksum = $checksum", false)
            }
        }).start()
    }

    private fun onExamineFinish() {
        val intent = Intent(this, DisplayInfoActivity::class.java)
        intent.putExtra("isModified", isModified)
        intent.putExtra("isAuthorized", isAuthorized)
        startActivity(intent)
        overridePendingTransition(0, 0)
    }

}