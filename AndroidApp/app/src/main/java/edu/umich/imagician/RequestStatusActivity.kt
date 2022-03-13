package edu.umich.imagician

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import edu.umich.imagician.ItemStore.getRequestDetail
import edu.umich.imagician.ItemStore.requests
import edu.umich.imagician.databinding.ActivityRequestStatusBinding
import edu.umich.imagician.utils.toast

class RequestStatusActivity : AppCompatActivity() {
    private lateinit var view: ActivityRequestStatusBinding
    private lateinit var watermarkRequest: WatermarkRequest
    private lateinit var watermarkPost: WatermarkPost
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        view = ActivityRequestStatusBinding.inflate(layoutInflater)
        setContentView(view.root)
        var index = intent.getIntExtra("index", -1)
        if (index == -1) {
            toast("Error: incorrect request index!")
        }
//        showRequest(index)
    }

    private fun showRequest(index: Int) {
        getRequestDetail(index)
        watermarkRequest = requests[index]!!
        watermarkPost = watermarkRequest.watermarkPost!!
        view.jpg.text = watermarkPost.filename
        view.cname.text = watermarkPost.username
        view.pnumber.text = watermarkPost.phoneNumber
        view.tstamp.text = watermarkPost.timestamp
        view.textView4.text = watermarkPost.message
    }

}