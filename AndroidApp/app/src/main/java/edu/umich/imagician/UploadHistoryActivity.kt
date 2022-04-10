package edu.umich.imagician

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import edu.umich.imagician.databinding.ActivityUploadHistoryBinding

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
        val index = intent.getIntExtra("index", -1)
//        if (index == -1) {
//            toast("Error: incorrect post index!")
//        }
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
//            showStatus(reqIndex)
        }
        view.buttonReject.setOnClickListener {
            watermarkRequest.status = "REJECTED"
            postUpdateStatus()
//            showStatus(reqIndex)
        }

        watermarkPost = ItemStore.watermarkPosts.posts[index]!!
        showPost()

        view.refreshReqs.setOnRefreshListener {
            showHistory(index)
        }

        showHistory(index)
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
            showStatus()
        } else {
            showOpts()
        }
    }

    private fun showPost() {
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
        if (!watermarkPost.msg_encrypted) {
            watermarkPost.message?.let { view.msg.text = it } ?:
            view.imageInfo.removeView(view.msgRow)
        } else {
            watermarkPost.message?.let { view.msg.text = "Message is encrypted." } ?:
            view.imageInfo.removeView(view.msgRow)
        }

        // history requests
        historyListAdapter = HistoryListAdapter(this, watermarkPost.pendingRequestList, this::seeMore)
        view.reqList.adapter = historyListAdapter
    }

    private fun showHistory(index: Int) {
//        if (LoginManager.isLoggedIn.value != true) {
//            toast("You need to first login")
//        }
        watermarkPost.pendingRequestList.clear()

        ItemStore.getPostDetail(index, {
            Log.d("refresh", "refresh request done with ${watermarkPost.pendingRequestList.size} requests")
            view.refreshReqs.isRefreshing = false
            historyListAdapter.notifyDataSetChanged()
        })
    }

    private fun showOpts() {
        view.ops.isVisible = true
        view.status.isVisible = false
        view.buttonBack.isVisible = false
    }

    private fun showStatus() {
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
//        toast("Update status of req_id: ${watermarkRequest.id}")
        WatermarkRequest.request = watermarkRequest
        ItemStore.handleRequest({
            Log.d("update status", "send action ${watermarkRequest.status}")
            showStatus()
        }, {
            Log.e("update status", "send action fails")
            watermarkRequest.status = "PENDING"
        })
    }
}