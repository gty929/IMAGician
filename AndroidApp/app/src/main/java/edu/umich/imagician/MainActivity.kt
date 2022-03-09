package edu.umich.imagician

import android.app.ActivityOptions
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.transition.Scene
import android.transition.Transition
import android.transition.TransitionInflater
import android.transition.TransitionManager
import android.view.View
import android.view.ViewGroup
import edu.umich.imagician.RequestStore.fakeRequests
import edu.umich.imagician.RequestStore.requests
import edu.umich.imagician.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var view: ActivityMainBinding
    private lateinit var requestListAdapter: RequestListAdapter
//    private lateinit var scene1: Scene
//    private lateinit var trans: Transition

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        view = ActivityMainBinding.inflate(layoutInflater)
        setContentView(view.root)

        requestListAdapter = RequestListAdapter(this, requests)
        view.requests.adapter = requestListAdapter

        fakeRequests()
//        scene1 = Scene.getSceneForLayout(view.root, R.layout.activity_request_status, this)

//        trans = TransitionInflater.from(this).inflateTransition(R.transition.slide)
    }

    /**
     * Jump to the ImportImageActivity
     *
     * @param view
     */
    fun startImportImageForCreation(view: View?) {
        val intent = Intent(this, ImportImageActivity::class.java)
        intent.putExtra("isCreate", true)
        startActivity(intent)
        overridePendingTransition(0, 0)
    }

    fun startImportImageForExamine(view: View?) {
        val intent = Intent(this, ImportImageActivity::class.java)
        intent.putExtra("isCreate", false)
        startActivity(intent)
        overridePendingTransition(0, 0)
    }

    fun onClickRequest(view: View?) {
        startActivity(Intent(this, RequestStatusActivity::class.java), ActivityOptions.makeSceneTransitionAnimation(this).toBundle())
    }

}