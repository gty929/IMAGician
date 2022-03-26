package edu.umich.imagician

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import edu.umich.imagician.utils.editToStr
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by Tianyao Gu on 2022/3/6.
 * TODO 3/26: File upload
 *            Encrypt message feature (put Cypto Sdk to Extensions.kt)
 */
@ExperimentalCoroutinesApi
class InputInfoActivity: AppCompatActivity()  {
    var imageUri: Uri? = null
    lateinit var timestampCheckBox: CheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_input_info)
        imageUri = intent.getParcelableExtra("IMAGE_URI")
        findViewById<ImageView>(R.id.imagePreview).setImageURI(imageUri)
        timestampCheckBox = findViewById(R.id.timestampCheckBox)

        findViewById<CheckBox>(R.id.usernameCheckBox).text = LoginManager.info.username

        val infoTable = findViewById<TableLayout>(R.id.infoTable)

        // remove the row if the user has not set it in personal info page
        LoginManager.info.fullname?.let { findViewById<CheckBox>(R.id.fullnameCheckBox).text = it } ?:
        infoTable.removeView(findViewById<TableRow>(R.id.fullnameRow))

        LoginManager.info.email?.let { findViewById<CheckBox>(R.id.emailCheckBox).text = it } ?:
        infoTable.removeView(findViewById<TableRow>(R.id.emailRow))

        LoginManager.info.phoneNumber?.let { findViewById<CheckBox>(R.id.phoneCheckBox).text = it } ?:
        infoTable.removeView(findViewById<TableRow>(R.id.phoneRow))

        startTimestampThread()

    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.confirm_menu, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // finish selection
        setWatermarkPost()
        // Handle item selection
        return when (item.itemId) {
            R.id.confirmMenu -> {
                val intent = Intent(this, ExportImageActivity::class.java)
                intent.putExtra("IMAGE_URI", imageUri)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setWatermarkPost() {
        WatermarkPost.post = WatermarkPost(
            username = LoginManager.info.username,
            fullname = LoginManager.info.fullname,
            title = editToStr(findViewById<EditText>(R.id.editTitle).text),
            message = editToStr(findViewById<EditText>(R.id.editMessage).text),
            timestampFlag = timestampCheckBox.isChecked,
            usernameFlag = findViewById<CheckBox>(R.id.usernameCheckBox).isChecked,
            fullnameFlag = findViewById<CheckBox>(R.id.fullnameCheckBox)?.isChecked ?: false, // it's possible that the row has been removed
            emailFlag = findViewById<CheckBox>(R.id.emailCheckBox)?.isChecked ?: false,
            phoneFlag = findViewById<CheckBox>(R.id.phoneCheckBox)?.isChecked ?: false
        )
    }

    private fun startTimestampThread(){
        object : Thread() {
            override fun run() {
                do {
                    val message= Message()
                    message.what=1
                    handler.sendMessage(message)
                    sleep(2000)
                }while (true)
            }
        }.start()
    }
    val handler: Handler = @SuppressLint("HandlerLeak")
    object :Handler() {
        @SuppressLint("SimpleDateFormat")
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
                1 -> {
                    val pattern = "yyyy-MM-dd hh:mm";
                    val simpleDateFormat = SimpleDateFormat(pattern)
                    val date = simpleDateFormat.format(Date())
                    timestampCheckBox.text= date
                }
                else -> {
                }
            }
        }
    }
}