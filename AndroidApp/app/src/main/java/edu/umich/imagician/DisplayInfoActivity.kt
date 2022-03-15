package edu.umich.imagician

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.*
import androidx.core.view.isVisible
import edu.umich.imagician.databinding.ActivityDisplayInfoBinding
import edu.umich.imagician.utils.toast

class DisplayInfoActivity : AppCompatActivity() {
    private lateinit var view: ActivityDisplayInfoBinding
    private lateinit var watermarkPost: WatermarkPost
    private var isModified = false
    private var isAuthorized = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        view = ActivityDisplayInfoBinding.inflate(layoutInflater)
        setContentView(view.root)
        isModified = intent.getBooleanExtra("isModified", false)
        isAuthorized = intent.getBooleanExtra("isAuthorized", false)
        showEmbeddedInfo()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (LoginManager.isLoggedIn.value == true) {
            val inflater: MenuInflater = menuInflater
            inflater.inflate(R.menu.contact_menu, menu)
        }
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // send request to author
        return when (item.itemId) {
            R.id.contactMenu -> {
                val intent = Intent(this, SendRequestActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun onClickEnter(chip: View?) {
        toast("Please enter the password")
        view.chipEnter.isVisible = false
        view.editTextTextPassword.isVisible = true
        view.editTextTextPassword.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                // call crypto lib to decrypt
                //
                // skip this process
                toast("Password correct!")
                view.editTextTextPassword.isVisible = false
                view.msg.text = "Never gonna give you up"
                view.msg.isVisible = true
                return@OnKeyListener true
            }
            false
        })
    }

    fun onClickDownload(chip: View?) {
        // http.GET
        toast("The attachment has been saved...")
    }

    private fun showEmbeddedInfo() {
        watermarkPost = WatermarkPost.post

        // required
        view.jpg.text = watermarkPost.filename

        // BOOL check
        view.warning.isVisible = isModified
        view.uright.text = isAuthorized.toString()

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
        watermarkPost.folder?.let { view.zip.text = it } ?:
        view.imageInfo.removeView(view.zipRow)
        watermarkPost.message?.let { view.msg.text = it } ?:
        view.imageInfo.removeView(view.msgRow)

    }
}