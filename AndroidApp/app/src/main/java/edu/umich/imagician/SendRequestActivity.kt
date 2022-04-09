package edu.umich.imagician

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
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
    private var imageUri: Uri?= null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        view = ActivitySendRequestBinding.inflate(layoutInflater)
        imageUri = intent.getParcelableExtra("IMAGE_URI")
        view.imageShow.setImageURI(imageUri)
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
                Log.i("Send req", "message ${watermarkRequest.message}")
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
        view.textView8.text = watermarkPost.title
        watermarkRequest = WatermarkRequest(
            watermarkPost = this.watermarkPost,
            sender = LoginManager.info.username,
        )
        Log.d("imgtag", watermarkRequest.watermarkPost?.tag ?: "") // why cannot add the line?
    }

    private fun submitRequest() {
        watermarkRequest.mode = Sendable.Mode.FULL
        ItemStore.httpCall(watermarkRequest) { code ->
            if (code == 200) {
//                toast("Successfully submit request!")
                val intent = Intent(this, PopUpWindow::class.java)
                intent.putExtra("popuptitle", "Sent")
                intent.putExtra("popuptext", "Message successfully sent to image creator!")
                intent.putExtra("popupbtn", "OK")
                intent.putExtra("darkstatusbar", true)
                intent.putExtra("displayinfo", true)
                startActivity(intent)
                // overridePendingTransition(0, 0)
                // return to parent activity
                // val anotherIntent = Intent(this, DisplayInfoActivity::class.java)
                // startActivity(anotherIntent)
                // overridePendingTransition(0, 0)
            }
        }
    }

}