package edu.umich.imagician

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import edu.umich.imagician.databinding.ActivityRequestStatusBinding

class RequestStatusActivity : AppCompatActivity() {
    private lateinit var view: ActivityRequestStatusBinding
    private lateinit var watermarkRequest: WatermarkRequest
    private lateinit var watermarkPost: WatermarkPost
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        view = ActivityRequestStatusBinding.inflate(layoutInflater)
        setContentView(view.root)
    }

    fun showRequest(context: Context, watermarkRequest: WatermarkRequest) {
        view.jpg.text = watermarkPost.filename
    }

}