package edu.umich.imagician


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.*
import android.widget.ImageButton
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import android.view.View
import edu.umich.imagician.ItemStore.fakeItems
import edu.umich.imagician.ItemStore.posts
import edu.umich.imagician.ItemStore.requests

import edu.umich.imagician.databinding.ActivityMainBinding
import edu.umich.imagician.utils.initPython
import edu.umich.imagician.utils.toast

class MainActivity : AppCompatActivity() {
    private lateinit var view: ActivityMainBinding
    private lateinit var requestListAdapter: RequestListAdapter
    private lateinit var postListAdapter: PostListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        view = ActivityMainBinding.inflate(layoutInflater)
        setContentView(view.root)
        if (!LoginManager.isLoggedIn) {
            LoginManager.open(this)
            findViewById<ImageButton>(R.id.newWatermarkButton).alpha = 0.2F // cannot create new watermark
        }
        requestListAdapter = RequestListAdapter(this, requests)
        postListAdapter = PostListAdapter(this, posts)
        view.requests.adapter = requestListAdapter
        view.creations.adapter = postListAdapter

        // setup refreshContainer
        view.refreshRequests.setOnRefreshListener {
            refreshReq()
        }
        view.refreshPosts.setOnRefreshListener {
            refreshPos()
        }

        // start python plugin
        initPython(this)
        refreshPos()
        refreshReq()

        view.requests.setOnItemClickListener { parent, view, position, id ->
            onClickRequest(view, position)
        }
        view.creations.setOnItemClickListener { parent, view, position, id ->
            onClickCreation(view, position)
        }

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

    fun onClickRequest(view: View?, index: Int) {
        val intent = Intent(this, RequestStatusActivity::class.java)
        intent.putExtra("index", index)
        startActivity(intent)
        overridePendingTransition(0, 0)
    }

    fun onClickCreation(view: View?, index: Int) {
        val intent = Intent(this, UploadHistoryActivity::class.java)
        intent.putExtra("index", index)
        startActivity(intent)
        overridePendingTransition(0, 0)
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

    private fun refreshReq() {
//        getRequests(applicationContext) {
//            runOnUiThread {
//                // inform the list adapter that data set has changed
//                // so that it can redraw the screen.
//                requestListAdapter.notifyDataSetChanged()
//            }
//            // stop the refreshing animation upon completion:
//            Handler().postDelayed(Runnable {
//                view.swipe.isRefreshing = false
//            }, 4000)
//        }
        fakeItems()
        view.refreshRequests.isRefreshing = false
    }

    private fun refreshPos() {
//        getPosts(applicationContext) {
//            runOnUiThread {
//                // inform the list adapter that data set has changed
//                // so that it can redraw the screen.
//                postListAdapter.notifyDataSetChanged()
//            }
//            // stop the refreshing animation upon completion:
//            Handler().postDelayed(Runnable {
//                view.swipe.isRefreshing = false
//            }, 4000)
//        }
        fakeItems()
        view.refreshPosts.isRefreshing = false
    }


}