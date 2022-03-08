package edu.umich.imagician

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import edu.umich.imagician.databinding.ActivityRequestStatusBinding

class RequestStatusActivity : AppCompatActivity() {
    private lateinit var view: ActivityRequestStatusBinding
    private lateinit var imgRequest: ImgRequest
    private lateinit var imgPost: ImgPost
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        view = ActivityRequestStatusBinding.inflate(layoutInflater)
        setContentView(view.root)
    }

    fun showRequest(context: Context, imgRequest: ImgRequest) {
        view.jpg.text = imgPost.filename
    }

}