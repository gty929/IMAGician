package edu.umich.imagician

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import edu.umich.imagician.databinding.ActivityUploadHistoryBinding
import edu.umich.imagician.utils.toast

class UploadHistoryActivity : AppCompatActivity() {
    private lateinit var view: ActivityUploadHistoryBinding
    private lateinit var watermarkPost: WatermarkPost
    private lateinit var historyListAdapter: HistoryListAdapter
    private lateinit var watermarkRequest: WatermarkRequest
    private var reqIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        view = ActivityUploadHistoryBinding.inflate(layoutInflater)
        setContentView(view.root)
        var index = intent.getIntExtra("index", -1)
        if (index == -1) {
            toast("Error: incorrect post index!")
        }
        view.buttonBack.setOnClickListener {
            view.reqInfoPad.isVisible = false
            view.refreshReqs.isVisible = true
        }
        view.buttonCancel.setOnClickListener {
            view.reqInfoPad.isVisible = false
            view.refreshReqs.isVisible = true
        }
        view.buttonGrant.setOnClickListener {
            watermarkRequest.status = "GRANTED"
            postUpdateStatus()
            showStatus(reqIndex)
        }
        view.buttonReject.setOnClickListener {
            watermarkRequest.status = "REJECTED"
            postUpdateStatus()
            showStatus(reqIndex)
        }

        view.refreshReqs.setOnRefreshListener {
            showPost(index)
        }

        showPost(index)
    }

    fun seeMore(idx: Int) {
//        toast("index clicked: $idx")
        reqIndex = idx
        watermarkRequest = watermarkPost.pendingRequestList[reqIndex]!!
        // Request Info Fields
        view.textView17.text = watermarkRequest.sender
        view.textView15.text = watermarkRequest.message

        view.refreshReqs.isVisible = false
        view.reqInfoPad.isVisible = true
        if (watermarkRequest.status != "PENDING") {
            showStatus(idx)
        } else {
            showOpts()
        }
    }

    private fun showPost(index: Int) {
        ItemStore.getPostDetail(index)
        watermarkPost = ItemStore.watermarkPosts.posts[index]!!
        historyListAdapter = HistoryListAdapter(this, watermarkPost.pendingRequestList, this::seeMore)

        // required
        view.jpg.text = watermarkPost.title

        // optionals
        watermarkPost.username?.let { view.cname.text = it } ?:
        view.imageInfo.removeView(view.cnameRow)
        watermarkPost.fullname?.let { view.rname.text = it } ?:
        view.imageInfo.removeView(view.rnameRow)
        watermarkPost.email?.let { view.eaddr.text = it } ?:
        view.imageInfo.removeView(view.eaddrRow)
        watermarkPost.phoneNumber?.let { view.pnumber.text = it } ?:
        view.imageInfo.removeView(view.pnumRow)
        watermarkPost.timestamp?.let { view.tstamp.text = it } ?:
        view.imageInfo.removeView(view.tsRow)
        watermarkPost.message?.let { view.msg.text = it } ?:
        view.imageInfo.removeView(view.msgRow)

        // history requests
        view.reqList.adapter = historyListAdapter

        view.refreshReqs.isRefreshing = false
    }

    private fun showOpts() {
        view.ops.isVisible = true
        view.status.isVisible = false
        view.buttonBack.isVisible = false
    }

    private fun showStatus(idx: Int) {
        view.ops.isVisible = false
        view.status.text = watermarkRequest.status
        val color = when (watermarkRequest.status) {
            "GRANTED" -> R.color.granted
            else -> R.color.rejected
        }
        view.status.setBackgroundColor(ContextCompat.getColor(this, color))
        view.status.isVisible = true
        view.buttonBack.isVisible = true
    }

    private fun postUpdateStatus() {
        // post change to server
        toast("Update status to be ${watermarkRequest.status}")
    }
}