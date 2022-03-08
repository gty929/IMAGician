package edu.umich.imagician

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import edu.umich.imagician.RequestStore.fakeRequests
import edu.umich.imagician.RequestStore.requests
import edu.umich.imagician.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var view: ActivityMainBinding
    private lateinit var requestListAdapter: RequestListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        view = ActivityMainBinding.inflate(layoutInflater)
        setContentView(view.root)

        requestListAdapter = RequestListAdapter(this, requests)
        view.requests.adapter = requestListAdapter

        fakeRequests()
    }

    /**
     * Jump to the ImportImageActivity
     *
     * @param view
     */
    fun startImportImage(view: View?) = startActivity(Intent(this, ImportImageActivity::class.java))

}