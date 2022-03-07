package edu.umich.imagician

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View

class UploadHistoryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload_history)
    }

    fun seeMoreInfo(view: View?) = startActivity(Intent(this, ReqDetailActivity::class.java))

}