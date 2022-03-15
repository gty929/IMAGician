package edu.umich.imagician

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import edu.umich.imagician.databinding.ActivitySendRequestBinding
import edu.umich.imagician.utils.toast
import java.time.Instant
import java.time.format.DateTimeFormatter

class SendRequestActivity : AppCompatActivity() {
    private lateinit var view: ActivitySendRequestBinding
    private lateinit var watermarkPost: WatermarkPost
    private lateinit var watermarkRequest: WatermarkRequest
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        view = ActivitySendRequestBinding.inflate(layoutInflater)
        setContentView(view.root)
        showMessageBox()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.contact_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // send request to author
        return when (item.itemId) {
            R.id.contactMenu -> {
                watermarkRequest.message = view.editTextTextMultiLine.text.toString()
                val pattern = "yyyy-MM-dd hh:mm";
                watermarkRequest.timestamp = DateTimeFormatter.ofPattern(pattern).format(Instant.now())

                submitRequest()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showMessageBox() {
        watermarkPost = WatermarkPost.post
        view.cname.text = watermarkPost.username ?: "Anonymous"
        view.rname.text = LoginManager.info.username
        watermarkRequest = WatermarkRequest(
            watermarkPost = this.watermarkPost,
            sender = LoginManager.info.username,
        )
    }

    private fun submitRequest() {
        ItemStore.httpCall(watermarkRequest) { code ->
            if (code == 200) {
                toast("Successfully submit request!")
                // return to parent activity
                finishActivity(0)
            }
        }
    }

}