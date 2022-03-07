package edu.umich.imagician

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import edu.umich.imagician.databinding.ActivityRequestStatusBinding

class RequestStatusActivity : AppCompatActivity() {
    private lateinit var view: ActivityRequestStatusBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(view.root)
    }

}