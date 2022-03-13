package edu.umich.imagician

import android.app.ActivityOptions
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.transition.Scene
import android.transition.Transition
import android.transition.TransitionInflater
import android.transition.TransitionManager
import android.view.*
import android.widget.ImageButton
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.google.gson.Gson
import edu.umich.imagician.RequestStore.fakeRequests
import edu.umich.imagician.RequestStore.requests
import edu.umich.imagician.databinding.ActivityMainBinding
import edu.umich.imagician.utils.toast

class MainActivity : AppCompatActivity() {
    private lateinit var view: ActivityMainBinding
    private lateinit var requestListAdapter: RequestListAdapter
//    private lateinit var scene1: Scene
//    private lateinit var trans: Transition

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        view = ActivityMainBinding.inflate(layoutInflater)
        setContentView(view.root)
        if (!LoginManager.isLoggedIn) {
            LoginManager.open(this)
            findViewById<ImageButton>(R.id.newWatermarkButton).alpha = 0.2F // cannot create new watermark
        }
        requestListAdapter = RequestListAdapter(this, requests)
        view.requests.adapter = requestListAdapter

        fakeRequests()

        // start python plugin
        initPython()


//        scene1 = Scene.getSceneForLayout(view.root, R.layout.activity_request_status, this)

//        trans = TransitionInflater.from(this).inflateTransition(R.transition.slide)
    }

    /**
     * Jump to the ImportImageActivity
     *
     * @param view
     */
    fun startImportImageForCreation(view: View?) {
        if (LoginManager.isLoggedIn) {
            val intent = Intent(this, ImportImageActivity::class.java)
            intent.putExtra("isCreate", true)
            startActivity(intent)
            overridePendingTransition(0, 0)
        } else {
            toast("You need to login first.")
        }

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

    private fun initPython(){
        if (! Python.isStarted()) {
            Python.start(AndroidPlatform(this));
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.login_menu, menu)
        if (LoginManager.isLoggedIn) {
            menu.findItem(R.id.loginMenu).title = LoginManager.currUsername
        }

        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.loginMenu -> {

                val intent = if (LoginManager.isLoggedIn)
                    Intent(this, UserInfoActivity::class.java)
                else Intent(this, LoginActivity::class.java)

                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}