package edu.umich.imagician

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import edu.umich.imagician.utils.toast

class ReqDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_req_detail)
        var index = intent.getIntExtra("index", -1)
        if (index == -1) {
            toast("Error: incorrect post index!")
        }
        showDetail(index)
    }

    private fun showDetail(index: Int) {

    }
}