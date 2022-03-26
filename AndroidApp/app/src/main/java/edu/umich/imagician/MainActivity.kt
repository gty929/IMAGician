package edu.umich.imagician


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageButton
import android.view.View
import androidx.lifecycle.Observer
import edu.umich.imagician.ItemStore.watermarkPosts
import edu.umich.imagician.ItemStore.watermarkRequests

import edu.umich.imagician.databinding.ActivityMainBinding
import edu.umich.imagician.utils.initPython
import edu.umich.imagician.utils.toast
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
class MainActivity : AppCompatActivity() {
    private lateinit var view: ActivityMainBinding
    private lateinit var requestListAdapter: RequestListAdapter
    private lateinit var postListAdapter: PostListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        view = ActivityMainBinding.inflate(layoutInflater)
        setContentView(view.root)


        val loginObserver = Observer<Boolean> { isLoggedIn ->
            // Update the UI, in this case, a TextView.
            if (isLoggedIn) {
                refreshPos()
                refreshReq()
                findViewById<ImageButton>(R.id.newWatermarkButton).alpha =
                    1F // can create new watermark
            } else {
                findViewById<ImageButton>(R.id.newWatermarkButton).alpha =
                    0.2F // cannot create new watermark
            }
        }
        LoginManager.isLoggedIn.observe(this, loginObserver)

        requestListAdapter = RequestListAdapter(this, watermarkRequests.requests)
        postListAdapter = PostListAdapter(this, watermarkPosts.posts)
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
//        refreshPos()
//        refreshReq()

        if (LoginManager.isLoggedIn.value != true) { // not logged in
            LoginManager.open(this)
        }

        view.requests.setOnItemClickListener { parent, view, position, id ->
            onClickRequest(view, position)
        }
        view.creations.setOnItemClickListener { parent, view, position, id ->
            onClickCreation(view, position)
        }

    }

    /**
     * Jump to the ImportImageActivityR
     *
     * @param view
     */
    fun startImportImageForCreation(view: View?) {
        if (LoginManager.isLoggedIn.value == true) {
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
        val loginObserver = Observer<Boolean> { isLoggedIn ->
            // Update the UI, in this case, a TextView.
            if (isLoggedIn) {
                menu.findItem(R.id.loginMenuItem).title = LoginManager.info.username
            } else {
                menu.findItem(R.id.loginMenuItem).title = "LOGIN"
            }
        }
        LoginManager.isLoggedIn.observe(this, loginObserver)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.loginMenuItem -> {

                val intent = if (LoginManager.isLoggedIn.value == true)
                    Intent(this, UserInfoActivity::class.java)
                else Intent(this, LoginActivity::class.java)

                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


    private fun refreshReq() {
        if (LoginManager.isLoggedIn.value != true) {
            toast("You need to first login")
        }
        watermarkRequests.clear()
        view.refreshRequests.post { view.refreshRequests.isRefreshing = true }
        ItemStore.refreshWatermarkRequests({
            Log.d("refresh", "refresh request done with ${watermarkRequests.requests.size} requests")
            view.refreshRequests.isRefreshing = false
        })

    }

    private fun refreshPos() {
        if (LoginManager.isLoggedIn.value != true) {
            toast("You need to first login")
        }
        watermarkPosts.clear()
        view.refreshPosts.post { view.refreshPosts.isRefreshing = true }
        ItemStore.refreshWatermarkPosts({
            Log.d("refresh", "refresh post done with ${watermarkPosts.posts.size} posts")
            view.refreshPosts.isRefreshing = false
        })

    }


}