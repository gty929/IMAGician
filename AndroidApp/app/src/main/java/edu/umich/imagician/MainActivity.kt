package edu.umich.imagician

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    /**
     * Jump to the ImportImageActivity
     *
     * @param view
     */
    fun startImportImage(view: View?) = startActivity(Intent(this, ImportImageActivity::class.java))
}