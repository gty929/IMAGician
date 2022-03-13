package edu.umich.imagician

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import edu.umich.imagician.databinding.ActivityUploadHistoryBinding
import edu.umich.imagician.utils.toast

class UploadHistoryActivity : AppCompatActivity() {
    private lateinit var view: ActivityUploadHistoryBinding
    private lateinit var watermarkPost: WatermarkPost
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        view = ActivityUploadHistoryBinding.inflate(layoutInflater)
        setContentView(view.root)
        var index = intent.getIntExtra("index", -1)
        if (index == -1) {
            toast("Error: incorrect post index!")
        }
        showPost(index)
    }

    fun seeMoreInfo(view: View?) = startActivity(Intent(this, ReqDetailActivity::class.java))

    private fun showPost(index: Int) {
        ItemStore.getRequestDetail(index)
//        LoginManager.info.email?.let { findViewById<CheckBox>(R.id.emailCheckBox).text = it } ?:
//        findViewById<TableLayout>(R.id.infoTable).removeView(findViewById<TableRow>(R.id.emailRow))
        watermarkPost = ItemStore.posts[index]!!
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
    }
}