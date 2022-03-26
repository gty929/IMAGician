package edu.umich.imagician

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.core.view.isVisible
import com.google.gson.Gson
import edu.umich.imagician.databinding.ActivityDisplayInfoBinding
import edu.umich.imagician.utils.toast

class DisplayInfoActivity : AppCompatActivity() {
    private lateinit var view: ActivityDisplayInfoBinding
    private lateinit var watermarkPost: WatermarkPost
    private var isModified = false
    private var imageUri: Uri?= null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        view = ActivityDisplayInfoBinding.inflate(layoutInflater)
        setContentView(view.root)
        isModified = intent.getBooleanExtra("isModified", false)
        imageUri = intent.getParcelableExtra("IMAGE_URI")
        view.imageShow.setImageURI(imageUri)
        view.chipEnter.text = "Encrypted, click to enter the password"
        view.chipDl.text = "Download"
        showEmbeddedInfo()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (LoginManager.isLoggedIn.value == true) {
//            val inflater: MenuInflater = menuInflater
//            inflater.inflate(R.menu.contact_menu, menu)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // edit request to author
        return when (item.itemId) {
            R.id.contactMenu -> {
                val intent = Intent(this, SendRequestActivity::class.java)
                intent.putExtra("IMAGE_URI", imageUri)
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
                view.locked.isVisible = false
                view.unlocked.isVisible = true
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
        Log.d("DisplayInfo", "watermarkPost = ${Gson().toJson(watermarkPost).toString()}")
        // required
        view.jpg.text = watermarkPost.filename

        // BOOL check
        view.warning.isVisible = isModified
        if (LoginManager.isLoggedIn.value == true) {
            view.uright.text = watermarkPost.authorized.toString()
        } else {
            view.imageInfo.removeView(view.urRow)
        }
        if (!watermarkPost.msg_encrypted) {
            view.chipEnter.isVisible = false
            view.locked.isVisible = false
            view.msg.isVisible = true
        }


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
        watermarkPost.folder?.let { view.zip.text = it } ?:
        view.imageInfo.removeView(view.zipRow)
        watermarkPost.message?.let { view.msg.text = it } ?:
        view.imageInfo.removeView(view.msgRow)

    }
}