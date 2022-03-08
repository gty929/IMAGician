package edu.umich.imagician

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson

/**
 * Created by Tianyao Gu on 2022/3/6.
 */
class InputInfoActivity: AppCompatActivity()  {
    var imageUri: Uri? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_input_info)
        imageUri = intent.getParcelableExtra("IMAGE_URI")
        findViewById<ImageView>(R.id.imagePreview).setImageURI(imageUri)
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.confirm_menu, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.confirmMenu -> {
                val intent = Intent(this, ExportImageActivity::class.java)
                intent.putExtra("IMAGE_URI", imageUri)
                intent.putExtra("WATERMARK_POST_JSON_STR", Gson().toJson(getWatermarkPost()).toString())
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun getWatermarkPost(): WatermarkPost {
        val post = WatermarkPost()
        post.filename = findViewById<EditText>(R.id.editFileName).text.toString()
        post.message = findViewById<EditText>(R.id.editMessage).text.toString()
        return post
    }
}