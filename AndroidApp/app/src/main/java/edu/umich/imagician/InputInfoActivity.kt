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
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by Tianyao Gu on 2022/3/6.
 */
class InputInfoActivity: AppCompatActivity()  {
    var imageUri: Uri? = null
    lateinit var timestampCheckBox: CheckBox
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_input_info)
        imageUri = intent.getParcelableExtra("IMAGE_URI")
        findViewById<ImageView>(R.id.imagePreview).setImageURI(imageUri)
        timestampCheckBox = findViewById(R.id.timestampCheckBox)
        findViewById<CheckBox>(R.id.usernameCheckBox).text = LoginManager.currUsername
        startTimestampThread()

    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.confirm_menu, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // finish selection
        val watermarkPost = getWatermarkPost()
        // Handle item selection
        return when (item.itemId) {
            R.id.confirmMenu -> {
                val intent = Intent(this, ExportImageActivity::class.java)
                intent.putExtra("IMAGE_URI", imageUri)
                intent.putExtra("FILENAME_STR", watermarkPost.filename)
                intent.putExtra("WATERMARK_POST_JSON_STR", Gson().toJson(watermarkPost).toString())
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun getWatermarkPost(): WatermarkPost {
        val post = WatermarkPost()
        post.username = LoginManager.currUsername
        post.filename = findViewById<EditText>(R.id.editFileName).text.toString()
        post.message = findViewById<EditText>(R.id.editMessage).text.toString()
        post.timestampFlag = timestampCheckBox.isChecked
        return post
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