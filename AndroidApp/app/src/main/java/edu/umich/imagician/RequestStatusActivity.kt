package edu.umich.imagician

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat
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
        showRequest(index)
    }

    private fun showRequest(index: Int) {
        getRequestDetail(index)
        watermarkRequest = requests[index]!!
        watermarkPost = watermarkRequest.watermarkPost!!

        // Image Info Fields

        // required
        view.jpg.text = watermarkPost.filename

        // optionals
        watermarkPost.username?.let { view.cname.text = it } ?:
        view.imageInfo.removeView(view.cnameRow)
        watermarkPost.realName?.let { view.rname.text = it } ?:
        view.imageInfo.removeView(view.rnameRow)
        watermarkPost.email?.let { view.eaddr.text = it } ?:
        view.imageInfo.removeView(view.eaddrRow)
        watermarkPost.phoneNumber?.let { view.pnumber.text = it } ?:
        view.imageInfo.removeView(view.pnumRow)
        watermarkPost.timestamp?.let { view.tstamp.text = it } ?:
        view.imageInfo.removeView(view.tsRow)
        watermarkPost.message?.let { view.msg.text = it } ?:
        view.imageInfo.removeView(view.msgRow)

        // Request Info Fields
        view.textView17.text = watermarkPost.username ?: "Anonymous"
        view.textView15.text = watermarkRequest.message
        view.status.text = watermarkRequest.status
        val color = when (watermarkRequest.status) {
            "GRANTED" -> R.color.granted
            "PENDING" -> R.color.pending
            else -> R.color.rejected
        }
        view.status.setBackgroundColor(ContextCompat.getColor(this, color))
    }

}